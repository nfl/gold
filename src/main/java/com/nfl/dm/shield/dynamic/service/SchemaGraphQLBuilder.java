package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.domain.context.GraphQLSchemaRequestContext;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.repository.SchemaInstanceRepository;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import graphql.relay.Connection;
import graphql.relay.Relay;
import graphql.schema.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.nfl.dm.shield.dynamic.domain.BaseKey.DYNAMIC_TYPE_NAMESPACE;
import static com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription.*;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Service
public class SchemaGraphQLBuilder implements RootChild {


    private final SchemaService schemaService;

    private final SchemaInstanceRepository schemaInstanceRepo;

    private final InstanceOutputTypeServiceFactory instanceOutputTypeServiceFactory;

    private final GraphQLOutputType cachedOutputType;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public SchemaGraphQLBuilder(SchemaService schemaService,
                                SchemaInstanceRepository schemaInstanceRepo,
                                InstanceOutputTypeServiceFactory instanceOutputTypeServiceFactory,
                                DataFetcherFactory dataFetcherFactory) {
        this.schemaService = schemaService;
        this.schemaInstanceRepo = schemaInstanceRepo;
        this.instanceOutputTypeServiceFactory = instanceOutputTypeServiceFactory;
        this.cachedOutputType = buildSchemaOutputType(
                dataFetcherFactory.getSchemaReferencedByDataFetcher(),
                dataFetcherFactory.getSchemaInstanceCountDataFetcher());
    }

    @Override
    public String childFieldName() {
        return "schemas";
    }

    @Override
    public GraphQLFieldDefinition buildField() {
        Relay relay = new Relay();
        List<GraphQLArgument> args = relay.getConnectionFieldArguments();
        args.add(new GraphQLArgument("names", new GraphQLList(GraphQLString)));

        GraphQLFieldDefinition totalCount = newFieldDefinition()
                .name("totalCount")
                .type(GraphQLLong)
                .description("Total number of elements in the connection.")
                .build();
        GraphQLObjectType edgeType = relay.edgeType(cachedOutputType.getName(), cachedOutputType, null, emptyList());
        GraphQLObjectType schemaCon = relay.connectionType(cachedOutputType.getName(), edgeType, singletonList(totalCount));
        @SuppressWarnings("UnnecessaryLocalVariable")
        GraphQLFieldDefinition schemas = newFieldDefinition()
                .type(schemaCon)
                .name(childFieldName())
                .argument(args)
                .dataFetcher(getSchemaFetcher())
                .build();
        return schemas;
    }

    @Override
    public List<GraphQLFieldDefinition> buildMutators() {

        List<GraphQLFieldDefinition> retList = new ArrayList<>();

        GraphQLFieldDefinition upsertSchemaDefinition = newFieldDefinition()
                .name("upsertSchemaDefinition")
                .argument(new GraphQLArgument("schemaDef", new GraphQLNonNull(buildSchemaInputType())))
                .type(cachedOutputType)
                .dataFetcher(getUpsertFetcher())
                .build();
        retList.add(upsertSchemaDefinition);

        GraphQLFieldDefinition deleteSchemaDefinition = newFieldDefinition()
                .name("removeSchemaDefinition")
                .argument(new GraphQLArgument(NAME_FIELD, new GraphQLNonNull(GraphQLString)))
                .type(cachedOutputType)
                .dataFetcher(getDeleteFetcher())
                .build();
        retList.add(deleteSchemaDefinition);

        return retList;
    }

    private DataFetcher<Connection<SchemaDescription>> getSchemaFetcher() {
        return (DataFetchingEnvironment environment) -> {
            @SuppressWarnings("unchecked")
            List<String> names = (List<String>) environment.getArguments().get("names");

            String schemaNamespace = ((GraphQLSchemaRequestContext) environment.getContext())
                    .getVariableMap().get(DYNAMIC_TYPE_NAMESPACE).toString();

            List<SchemaDescription> fetchedSchemaDescriptions;
            if (names == null || names.isEmpty()) {
                fetchedSchemaDescriptions = schemaService.findAllSchemas(schemaNamespace);
            } else {
                fetchedSchemaDescriptions = schemaService.findSchemas(names, schemaNamespace);
            }

            return ConnectionWithTotal.create(fetchedSchemaDescriptions, environment);
        };
    }

    private DataFetcher getUpsertFetcher() {
        return environment -> {
            GraphQLSchemaRequestContext graphQLSchemaRequestContext = environment.getContext();

            SchemaWriteAccess writeAccess = graphQLSchemaRequestContext.getMutablePerms();

            Object inputValue = environment.getArguments().get("schemaDef");
            if (!(inputValue instanceof Map))
                return "{}";

            @SuppressWarnings("unchecked")
            Map<String, Object> inputMap = (Map<String, Object>) inputValue;
            String nameValue = inputMap.get(NAME_FIELD).toString();

            String schemaNamespace = ((GraphQLSchemaRequestContext) environment.getContext())
                    .getVariableMap().get(DYNAMIC_TYPE_NAMESPACE).toString();
            if (!writeAccess.hasMutationWriteAccess(schemaNamespace, SchemaWriteAccess.SCHEMA_MODIFY)) {
                throw new SecurityException("No Write Access to '" + nameValue + "' in namespace " + schemaNamespace);
            }

            @SuppressWarnings("unchecked")
            SchemaDescription schemaToUpdate = new SchemaDescription(
                    schemaNamespace,
                    (Map<String, Object>) inputValue,
                    instanceOutputTypeServiceFactory.getInstance(graphQLSchemaRequestContext));

            schemaToUpdate.validateSchema();

            return schemaService.upsert(schemaToUpdate);
        };
    }

    private DataFetcher getDeleteFetcher() {
        return environment -> {
            GraphQLSchemaRequestContext graphQLSchemaRequestContext = environment.getContext();

            SchemaWriteAccess writeAccess = graphQLSchemaRequestContext.getMutablePerms();

            Object nameValue = environment.getArguments().get(NAME_FIELD);
            if (nameValue == null)
                return "{}";

            String schemaNamespace = ((GraphQLSchemaRequestContext) environment.getContext())
                    .getVariableMap().get(DYNAMIC_TYPE_NAMESPACE).toString();
            if (!writeAccess.hasMutationWriteAccess(schemaNamespace, SchemaWriteAccess.SCHEMA_MODIFY)) {
                throw new SecurityException("No Write Access to '" + nameValue + "' in namespace " + schemaNamespace);
            }

            if (schemaInstanceRepo.hasInstances(nameValue.toString())) {
                throw new IllegalStateException(nameValue.toString() + " has instances, must be empty before deletion allowed");
            }

            return schemaService.deleteSchema(nameValue.toString(), schemaNamespace);
        };
    }
}
