package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.domain.context.GraphQLInstanceRequestContext;
import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.instance.OrderByDirection;
import com.nfl.dm.shield.dynamic.domain.instance.OrderBy;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.schema.FilterConfiguration;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import com.nfl.dm.shield.dynamic.domain.schema.instancefield.SchemaInstanceField;
import com.nfl.dm.shield.dynamic.repository.SchemaRepository;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.relay.Relay;
import graphql.schema.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.nfl.dm.shield.dynamic.domain.BaseKey.*;
import static com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance.ID;
import static com.nfl.dm.shield.dynamic.domain.schema.FilterConfiguration.FILTERS_ARGUMENT;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;


@SuppressWarnings({"WeakerAccess", "SpringJavaAutowiringInspection"})
@Service
public class GraphQLInstanceService extends GraphQLBaseService {

    private static final String LABEL_ARGUMENT = "label";
    public static final String ORDER_BY_ARGUMENT = "orderBy";
    public static final String ORDER_BY_DIRECTION_ARGUMENT = "orderByDirection";
    private final SchemaRepository schemaRepo;

    private GraphQLRelayBuilder relayGraphQLBuilder;

    private final InstanceOutputTypeServiceFactory instanceOutputTypeServiceFactory;

    private final CacheService cacheService;

    private final DataFetcherFactory dataFetcherFactory;

    @Autowired
    public GraphQLInstanceService(SchemaRepository schemaRepo,
                                  GraphQLRelayBuilder relayGraphQLBuilder,
                                  InstanceOutputTypeServiceFactory instanceOutputTypeServiceFactory,
                                  CacheService cacheService,
                                  DataFetcherFactory dataFetcherFactory) {
        this.schemaRepo = schemaRepo;
        this.relayGraphQLBuilder = relayGraphQLBuilder;
        this.instanceOutputTypeServiceFactory = instanceOutputTypeServiceFactory;
        this.cacheService = cacheService;
        this.dataFetcherFactory = dataFetcherFactory;
    }

    @SuppressWarnings("WeakerAccess")
    public GraphQLResult executeQuery(String query,
                                      Map<String, Object> variableMap, SchemaWriteAccess mutablePerms,
                                      int maxDepth) {

        String schemaNamespace = variableMap.get(DYNAMIC_TYPE_NAMESPACE).toString();
        String instanceNamespace = variableMap.get(DYNAMIC_INSTANCE_NAMESPACE).toString();
        String schemaName = variableMap.get(DYNAMIC_TYPE_NAME).toString();
        SchemaKey schemaKey = new SchemaKey(schemaName, schemaNamespace);
        SchemaInstanceKey schemaInstanceKey = new SchemaInstanceKey(schemaName, schemaNamespace, instanceNamespace);

        SchemaDescription schemaDescription = schemaRepo.findByName(schemaKey);
        if (schemaDescription == null) {
            return buildErrorResult(schemaInstanceKey.getSchemaName() + " not found.");
        }

        GraphQLInstanceRequestContext instanceContext = new GraphQLInstanceRequestContext(schemaInstanceKey, schemaKey);

        RootChild rootChild = buildRootChild(schemaInstanceKey, schemaKey, schemaDescription, mutablePerms, maxDepth,
                mutablePerms.getAuthHeader());
        GraphQL graphQL = relayGraphQLBuilder.buildGraphQL(singletonList(rootChild));


        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .context(instanceContext)
                .root(instanceContext)
                .variables(variableMap)
                .build();
        ExecutionResult executionResult = graphQL.execute(executionInput);
        return buildResult(executionResult);
    }

    RootChild buildRootChild(SchemaInstanceKey schemaInstanceKey, SchemaKey schemaKey,
                             SchemaDescription schemaDescription, SchemaWriteAccess mutablePerms,
                             int maxDepth, String authHeader) {

        InstanceFieldBuilderContext.InstanceFieldBuilderContextBuilder builder
                = InstanceFieldBuilderContext.InstanceFieldBuilderContextBuilder.getInstance();
        builder.setSchemaNamespace(schemaDescription.getNamespace()).setMaxDepth(maxDepth).setAuthHeader(authHeader);

        builder = constructBuilderForPreload(builder, schemaKey);

        InstanceFieldBuilderContext instanceFieldBuilderContext = builder.build();

        InstanceOutputTypeService instanceOutputTypeService
                = instanceOutputTypeServiceFactory.getInstance(instanceFieldBuilderContext);

        GraphQLObjectType rootType = schemaDescription.buildInstanceOutputType(instanceFieldBuilderContext,
                instanceOutputTypeService);


        return new RootChild() {
            @Override
            public String childFieldName() {
                return "instances";
            }

            @Override
            public GraphQLFieldDefinition buildField() {
                Relay relay = new Relay();
                GraphQLObjectType edgeType = relay.edgeType("instance", rootType, null, emptyList());
                GraphQLFieldDefinition totalCount = newFieldDefinition()
                        .name("totalCount")
                        .type(GraphQLLong)
                        .description("Total number of elements in the connection.")
                        .build();
                GraphQLObjectType instanceCon = relay.connectionType("instance", edgeType, singletonList(totalCount));

                @SuppressWarnings("UnnecessaryLocalVariable")
                GraphQLFieldDefinition instances = newFieldDefinition()
                        .type(instanceCon)
                        .name(childFieldName())
                        .argument(buildArguments(relay))
                        .dataFetcher(dataFetcherFactory.getInstanceFetcher(
                                schemaInstanceKey.getInstanceNamespace(),
                                schemaInstanceKey.getSchemaName(),
                                schemaInstanceKey.getSchemaNamespace(),
                                schemaDescription.getFilterConfigurations())
                        )
                        .build();
                return instances;
            }

            @Override
            public List<GraphQLFieldDefinition> buildMutators() {
                GraphQLInputType inputRoot = schemaDescription.buildInstanceInputType(instanceFieldBuilderContext);
                return buildMutatorList(rootType, inputRoot, schemaDescription, mutablePerms,
                        schemaInstanceKey.getInstanceNamespace());
            }

            private List<GraphQLArgument> buildArguments(Relay relay) {
                List<GraphQLArgument> args = relay.getConnectionFieldArguments();
                args.add(new GraphQLArgument("ids", new GraphQLList(GraphQLString)));
                args.add(new GraphQLArgument(LABEL_ARGUMENT, GraphQLString));

                List<GraphQLInputObjectField> filterFields = schemaDescription.getFilterConfigurations().stream()
                        .map(this::buildFilterConfigurationInputField).collect(Collectors.toList());

                args.add(new GraphQLArgument(FILTERS_ARGUMENT, new GraphQLInputObjectType("filtersInputType",
                        null, filterFields)));

                args.add(new GraphQLArgument(
                        ORDER_BY_ARGUMENT,
                        "The field to sort instances by.",
                        /* If we later need a dynamic approach, we can do it more like "filters". */
                        newEnum().name(OrderBy.class.getSimpleName()).value(OrderBy.UPDATE_DATE.getFieldName()).build(),
                        null));
                args.add(new GraphQLArgument(
                        ORDER_BY_DIRECTION_ARGUMENT,
                        "Sorting order used only when '" + ORDER_BY_ARGUMENT + "' is specified",
                        newEnum().name(OrderByDirection.class.getSimpleName())
                                .value(OrderByDirection.ASC.name())
                                .value(OrderByDirection.DESC.name())
                                .build(),
                        OrderByDirection.ASC.name()));
                return args;
            }

            private GraphQLInputObjectField buildFilterConfigurationInputField(
                    FilterConfiguration filterConfiguration) {
                SchemaInstanceField field = schemaDescription.getDomainFields().stream()
                        .filter(df -> df.getMemberFieldName().equals(filterConfiguration.getFieldName())).findAny()
                        .orElseThrow(() -> new IllegalStateException(
                                "Filter Configuration's field does not match any existing field"));

                GraphQLInputType graphQLInputType = field.getMemberType().fieldFactory(schemaDescription, field)
                        .buildInstanceInputType(null);

                return new GraphQLInputObjectField(filterConfiguration.getOutputFilterName(), graphQLInputType);
            }
        };
    }

    private InstanceFieldBuilderContext.InstanceFieldBuilderContextBuilder constructBuilderForPreload(
            InstanceFieldBuilderContext.InstanceFieldBuilderContextBuilder builder,
            SchemaKey schemaKey) {
        Set<SchemaKey> referencedDefinitionIds;

        referencedDefinitionIds = cacheService.getSchemaDescriptionIdCache().getIfPresent(schemaKey);

        Map<SchemaKey, SchemaDescription> schemaDefinitionPreloadCache;

        if (referencedDefinitionIds != null) {
            //preload
            schemaDefinitionPreloadCache = schemaRepo.findByNames(new ArrayList<>(referencedDefinitionIds)).stream()
                    .collect(Collectors.toMap(SchemaDescription::getSchemaKey, Function.identity()));
        } else {
            referencedDefinitionIds = new HashSet<>();
            schemaDefinitionPreloadCache = new ConcurrentHashMap<>();
            cacheService.getSchemaDescriptionIdCache().put(schemaKey, referencedDefinitionIds);
        }

        return builder.setReferencedDefinitionIds(referencedDefinitionIds)
                .setSchemaDefinitionPreloadCache(schemaDefinitionPreloadCache);
    }

    private List<GraphQLFieldDefinition> buildMutatorList(GraphQLObjectType rootType, GraphQLInputType inputRoot,
                                                          SchemaDescription schemaDescription,
                                                          SchemaWriteAccess mutablePerms, String instanceNamespace) {

        List<GraphQLFieldDefinition> mutatorList = new ArrayList<>();

        SchemaInstanceKey schemaInstanceKey = new SchemaInstanceKey(schemaDescription.getName(),
                schemaDescription.getNamespace(), instanceNamespace);

        DataFetcher upsertSchemaInstanceDataFetcher =
                mutablePerms.hasMutationWriteAccess(instanceNamespace, SchemaWriteAccess.INSTANCE_MODIFY) ?
                        dataFetcherFactory.getInstanceUpsertDataFetcher(schemaDescription,
                                schemaInstanceKey.getInstanceNamespace(), schemaInstanceKey.getSchemaName()) :
                        dataFetcherFactory.getUnauthorizedFetcher("upsertSchemaInstance");
        GraphQLFieldDefinition upsertSchemaInstance = newFieldDefinition()
                .name("upsertSchemaInstance")
                .argument(new GraphQLArgument(LABEL_ARGUMENT, GraphQLString))
                .argument(new GraphQLArgument("schemaInstance", new GraphQLNonNull(inputRoot)))
                .type(rootType)
                .dataFetcher(upsertSchemaInstanceDataFetcher)
                .build();
        mutatorList.add(upsertSchemaInstance);

        DataFetcher removeInstanceDataFetcher =
                mutablePerms.hasMutationWriteAccess(instanceNamespace, SchemaWriteAccess.INSTANCE_DELETE) ?
                        dataFetcherFactory.getDeleteFetcher(schemaInstanceKey) :
                        dataFetcherFactory.getUnauthorizedFetcher("removeInstance");
        GraphQLFieldDefinition deleteSchemaDefinition = newFieldDefinition()
                .name("removeInstance")
                .argument(new GraphQLArgument(LABEL_ARGUMENT, GraphQLString))
                .argument(new GraphQLArgument(ID, new GraphQLNonNull(GraphQLString)))
                .type(rootType)
                .dataFetcher(removeInstanceDataFetcher)
                .build();
        mutatorList.add(deleteSchemaDefinition);

        DataFetcher removeAllInstancesDataFetcher = mutablePerms.hasMutationWriteAccess(instanceNamespace,
                SchemaWriteAccess.INSTANCE_TRUNCATE) ?
                dataFetcherFactory.getDeleteAllFetcher(schemaInstanceKey) :
                dataFetcherFactory.getUnauthorizedFetcher("removeAllInstances");

        GraphQLFieldDefinition countField = newFieldDefinition()
                .type(GraphQLLong)
                .name("count")
                .build();
        GraphQLObjectType countType = new GraphQLObjectType("Count", "Remove All Result",
                singletonList(countField), emptyList());
        GraphQLFieldDefinition deleteAllInstancesDefinition = newFieldDefinition()
                .name("removeAllInstances")
                .type(countType)
                .dataFetcher(removeAllInstancesDataFetcher)
                .build();
        mutatorList.add(countField);
        mutatorList.add(deleteAllInstancesDefinition);

        return mutatorList;
    }
}
