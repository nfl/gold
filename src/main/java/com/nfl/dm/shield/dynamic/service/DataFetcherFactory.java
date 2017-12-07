package com.nfl.dm.shield.dynamic.service;

import com.google.common.collect.Maps;
import com.nfl.dm.shield.dynamic.domain.BaseKey;
import com.nfl.dm.shield.dynamic.domain.context.GraphQLInstanceRequestContext;
import com.nfl.dm.shield.dynamic.domain.instance.*;
import com.nfl.dm.shield.dynamic.domain.schema.FilterConfiguration;
import com.nfl.dm.shield.dynamic.domain.schema.IdGeneration;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.instancefield.SchemaInstanceField;
import com.nfl.dm.shield.dynamic.exception.UnauthorizedException;
import com.nfl.dm.shield.dynamic.repository.ExternalReferenceRepository;
import com.nfl.dm.shield.dynamic.repository.SchemaInstanceRepository;
import graphql.language.SelectionSet;
import graphql.relay.Connection;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance.ID;
import static com.nfl.dm.shield.dynamic.domain.schema.FilterConfiguration.FILTERS_ARGUMENT;
import static com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription.INSTANCE_COUNT_INSTANCE_NAMESPACE_ARGUMENT;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.AbstractReferenceType.REFERENCE_ID;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.AbstractReferenceType.REFERENCE_TYPE;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.AbstractReferenceType.TYPE_NAME_DECORATOR;
import static com.nfl.dm.shield.dynamic.service.GraphQLInstanceService.ORDER_BY_ARGUMENT;
import static com.nfl.dm.shield.dynamic.service.GraphQLInstanceService.ORDER_BY_DIRECTION_ARGUMENT;
import static java.util.Collections.*;

@SuppressWarnings({"SpringJavaAutowiringInspection", "WeakerAccess"})
@Service
public class DataFetcherFactory {

    private static final String LABEL_ARGUMENT = "label";

    private final CacheService cacheService;
    private final SchemaService schemaService;
    private final SchemaInstanceRepository schemaInstanceRepository;
    private final ExternalReferenceRepository externalReferenceRepository;

    @Autowired
    public DataFetcherFactory(CacheService cacheService,
                              SchemaService schemaService, SchemaInstanceRepository schemaInstanceRepository,
                              ExternalReferenceRepository externalReferenceRepository) {
        this.schemaService = schemaService;
        this.schemaInstanceRepository = schemaInstanceRepository;
        this.cacheService = cacheService;
        this.externalReferenceRepository = externalReferenceRepository;
    }

    public DataFetcher getReferenceDataFetcher(InstanceOutputTypeService lookup, String schemaName,
                                               String memberFieldName) {
        return new ReferenceFetcher(lookup, schemaName, memberFieldName);
    }

    public DataFetcher getArrayReferenceDataFetcher(InstanceOutputTypeService lookup, String schemaName,
                                                    String memberFieldName) {
        return new ArrayReferenceDataFetcher(lookup, schemaName, memberFieldName);
    }

    public DataFetcher getSchemaReferencedByDataFetcher() {
        return env -> {
            SchemaDescription schema = env.getSource();
            return schemaService.findDirectRelatedSchemas(schema);
        };
    }

    public DataFetcher getSchemaInstanceCountDataFetcher() {
        return env -> {
            SchemaDescription schema = env.getSource();
            return schemaInstanceRepository.countInstances(new SchemaInstanceKey(
                    schema.getName(),
                    schema.getNamespace(),
                    env.getArgument(INSTANCE_COUNT_INSTANCE_NAMESPACE_ARGUMENT)));
        };
    }

    public DataFetcher getInstanceReferencedByDataFetcher(InstanceOutputTypeService instanceOutputTypeService) {
        return new InstanceReferencedByDataFetcher(instanceOutputTypeService);
    }

    private class ReferenceFetcher implements DataFetcher {

        private final InstanceOutputTypeService instanceOutputTypeService;
        private final String schemaName;
        private final String memberFieldName;

        private ReferenceFetcher(InstanceOutputTypeService instanceOutputTypeService, String schemaName,
                                 String memberFieldName) {
            this.instanceOutputTypeService = instanceOutputTypeService;
            this.schemaName = schemaName;
            this.memberFieldName = memberFieldName;
        }

        @Override
        public Object get(DataFetchingEnvironment environment) {

            @SuppressWarnings("unchecked")
            Object idObject = ((Map<String, Object>) environment.getSource()).get(memberFieldName);
            String id = Optional.ofNullable(idObject).map(Object::toString).orElse(null);
            if (id == null) {
                return null;
            }

            SchemaInstanceKey parentSchemaInstanceKey =
                    ((GraphQLInstanceRequestContext) environment.getContext()).getSchemaInstanceKey();

            SchemaInstanceKey schemaInstanceKey = parentSchemaInstanceKey.getChildSchemaInstanceKey(schemaName);

            GraphQLInstanceRequestContext context = environment.getContext();

            return CacheService.getSchemaInstance(schemaInstanceKey, id, context, instanceOutputTypeService);
        }
    }

    private class ArrayReferenceDataFetcher implements DataFetcher {

        private final InstanceOutputTypeService instanceOutputTypeService;
        private final String schemaName;
        private final String memberFieldName;

        private ArrayReferenceDataFetcher(InstanceOutputTypeService instanceOutputTypeService, String schemaName,
                                          String memberFieldName) {
            this.instanceOutputTypeService = instanceOutputTypeService;
            this.schemaName = schemaName;
            this.memberFieldName = memberFieldName;
        }

        @Override
        public Object get(DataFetchingEnvironment environment) {
            @SuppressWarnings("unchecked")
            List<String> ids = (List<String>) ((Map<String, Object>) environment.getSource()).get(memberFieldName);
            if (ids == null || ids.isEmpty())
                return emptyList();

            ids = ids.stream().filter(otherId -> !otherId.isEmpty()).collect(Collectors.toList());
            if (ids.isEmpty())
                return emptyList();

            SchemaInstanceKey parentSchemaInstanceKey =
                    ((GraphQLInstanceRequestContext) environment.getContext()).getSchemaInstanceKey();

            SchemaInstanceKey schemaInstanceKey = parentSchemaInstanceKey.getChildSchemaInstanceKey(schemaName);

            GraphQLInstanceRequestContext context = environment.getContext();

            return CacheService.getSchemaInstances(schemaInstanceKey, ids, context, instanceOutputTypeService);
        }
    }

    public DataFetcher getInstanceUpsertDataFetcher(SchemaDescription schemaDescription, String instanceNamespace,
                                                    String domainName) {
        return environment -> {
            Map<String, Object> argMap = environment.getArguments();
            Object inputValue = argMap.get("schemaInstance");
            if (!(inputValue instanceof Map))
                return "{}";

            SchemaInstanceKey schemaInstanceKey = getSchemaInstanceKeyWithLabelArgument(instanceNamespace,
                    domainName, schemaDescription.getNamespace(), argMap);

            @SuppressWarnings("unchecked")
            Map<String, Object> inputMap = (Map<String, Object>) inputValue;
            schemaDescription.getDomainFields().forEach(fieldDef -> validateField(fieldDef, inputMap));
            if (inputMap.containsKey(ID)) {
                validateProvidedID(schemaInstanceKey, schemaDescription, inputMap);
            } else {
                validateAndGenerateID(schemaDescription, inputMap);
            }

            SchemaInstance currentInstance = new SchemaInstance(inputMap, schemaInstanceKey);
            currentInstance = schemaInstanceRepository.upsert(schemaInstanceKey, currentInstance);
            return currentInstance;
        };
    }

    public DataFetcher<Connection<SchemaInstance>> getInstanceFetcher(String instanceNamespace, String schemaName, String schemaNamespace,
                                                      List<FilterConfiguration> filterConfigurations) {
        return environment -> {

            Map<String, Object> argMap = environment.getArguments();
            @SuppressWarnings("unchecked")
            List<String> ids = (List<String>) argMap.get("ids");
            SchemaInstanceKey instanceKey =
                    getSchemaInstanceKeyWithLabelArgument(instanceNamespace, schemaName, schemaNamespace, argMap);

            preloadInstances(ids, instanceKey, environment.getContext());

            List<SchemaInstance> instances = retrieveInstances(ids, instanceKey);

            @SuppressWarnings("unchecked")
            Map<String, Object> filters =
                    (Map<String, Object>) argMap.getOrDefault(FILTERS_ARGUMENT, Collections.EMPTY_MAP);
            instances = filterInstances(filterConfigurations, instances, filters);

            if (environment.getArgument(ORDER_BY_ARGUMENT) != null) {
                instances = sortInstances(
                        instances,
                        environment.getArgument(ORDER_BY_ARGUMENT),
                        environment.getArgument(ORDER_BY_DIRECTION_ARGUMENT));
            }

            return ConnectionWithTotal.create(instances, environment);
        };
    }

    private List<SchemaInstance> sortInstances(List<SchemaInstance> instances, String orderBy, String orderByDir) {
        Comparator<SchemaInstance> comparator =
                OrderByDirection.resolve(orderByDir).deriveComparator(OrderBy.resolve(orderBy));
        return instances.stream().sorted(comparator).collect(Collectors.toList());
    }

    private List<SchemaInstance> filterInstances(List<FilterConfiguration> filterConfigurations,
                                                 List<SchemaInstance> instances, Map<String, Object> filterMap) {

        List<FilterConfiguration> appliedFilters = filterConfigurations.stream()
                .filter(fc -> filterMap.containsKey(fc.getOutputFilterName())).collect(Collectors.toList());

        List<Predicate<SchemaInstance>> predicates = appliedFilters.stream()
                .map(fc -> fc.buildPredicate(filterMap.get(fc.getOutputFilterName()))).collect(Collectors.toList());

        Predicate<SchemaInstance> predicate = predicates.stream().reduce(Predicate::and).orElse(instance -> true);

        return instances.stream().filter(predicate).collect(Collectors.toList());
    }

    private List<SchemaInstance> retrieveInstances(List<String> ids, SchemaInstanceKey instanceKey) {
        List<SchemaInstance> instances;
        if (ids == null || ids.isEmpty()) {
            instances = schemaInstanceRepository.listInstances(instanceKey);

        } else if (ids.size() == 1) {
            SchemaInstance desc = schemaInstanceRepository.findById(instanceKey, ids.get(0));
            instances = (desc == null) ? emptyList() : singletonList(desc);

        } else {
            List<SchemaInstance> desc = schemaInstanceRepository.findInstances(instanceKey, ids);
            instances = desc == null ? emptyList() : desc;
        }
        return instances;
    }

    private void preloadInstances(List<String> ids, SchemaInstanceKey instanceKey,
                                  GraphQLInstanceRequestContext context) {
        CacheService.SchemaInstanceRequestIdentifier rootIdentifier =
                new CacheService.SchemaInstanceRequestIdentifier(instanceKey, ids);
        Map<SchemaInstanceKey, Set<String>> referencedIdsMap = cacheService.getGraphs().getIfPresent(rootIdentifier);

        if (referencedIdsMap != null) {
            //preload
            context.getSchemaInstancePreloadCache().putAll(preloadSchemaInstances(referencedIdsMap));
        } else {
            referencedIdsMap = new ConcurrentHashMap<>();
            cacheService.getGraphs().put(rootIdentifier, referencedIdsMap);
        }
        context.setReferencedInstanceIdsMap(referencedIdsMap);
    }

    private Map<CacheService.SchemaInstanceIdentifier, SchemaInstance> preloadSchemaInstances(Map<SchemaInstanceKey,
            Set<String>> referencedIdsMap) {

        return referencedIdsMap.entrySet().stream()
                .map(entry -> getSchemaInstancesByIdentifier(entry.getKey(), entry.getValue()))
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }

    private List<Map.Entry<CacheService.SchemaInstanceIdentifier, SchemaInstance>> getSchemaInstancesByIdentifier(
            SchemaInstanceKey schemaInstanceKey, Set<String> idSet) {
        return schemaInstanceRepository.findInstances(schemaInstanceKey, new ArrayList<>(idSet))
                .stream()
                .map(schemaInstance -> Maps.immutableEntry(new CacheService.SchemaInstanceIdentifier(schemaInstanceKey,
                        schemaInstance.getId()), schemaInstance))
                .collect(Collectors.toList());
    }

    private SchemaInstanceKey getSchemaInstanceKeyWithLabelArgument(String instanceNamespace, String schemaName,
                                                                    String schemaNamespace,
                                                                    Map<String, Object> argMap) {
        Object label = argMap.get(LABEL_ARGUMENT);
        return (label == null || label.toString().isEmpty()) ?
                new SchemaInstanceKey(schemaName, schemaNamespace, instanceNamespace) :
                new SchemaInstanceKey(schemaName, schemaNamespace, instanceNamespace, label.toString());
    }

    private void validateField(SchemaInstanceField fieldDef, Map<String, Object> inputMap) {
        if (inputMap.containsKey(fieldDef.getMemberFieldName())) {
            Object fieldValue = inputMap.get(fieldDef.getMemberFieldName());
            fieldDef.validateInstance(fieldValue);
        }
    }

    private void validateAndGenerateID(SchemaDescription schemaDescription, Map<String, Object> inputMap) {
        if (schemaDescription.getIdGeneration() == IdGeneration.CLIENT_SPECIFIED)
            throw new IllegalArgumentException("Client must specify ID");

        if (schemaDescription.getIdGeneration() != IdGeneration.SERVICE_GENERATED_GUID) {
            throw new UnsupportedOperationException("Unknown ID strategy: " +
                    schemaDescription.getIdGeneration());
        }

        // Generate one for missing case
        inputMap.put(ID, UUID.randomUUID().toString());
    }

    private void validateProvidedID(SchemaInstanceKey schemaInstanceKey, SchemaDescription schemaDescription,
                                    Map<String, Object> inputMap) {
        if (schemaDescription.getIdGeneration() == IdGeneration.SERVICE_GENERATED_GUID) {
            SchemaInstance prev = schemaInstanceRepository.findById(schemaInstanceKey, inputMap.get(ID).toString());

            if (prev == null)
                throw new IllegalArgumentException("ID not found on update " + inputMap.get(ID));
        }
    }

    public DataFetcher getDeleteFetcher(final SchemaInstanceKey schemaInstanceKey) {
        return environment -> {
            Map<String, Object> argMap = environment.getArguments();
            Object nameValue = argMap.get(ID);
            if (nameValue == null)
                return "{}";

            SchemaInstanceKey schemaInstanceKeyWithLabel = getSchemaInstanceKeyWithLabelArgument(
                    schemaInstanceKey.getInstanceNamespace(), schemaInstanceKey.getSchemaName(),
                    schemaInstanceKey.getSchemaNamespace(), argMap);
            return schemaInstanceRepository.deleteInstance(schemaInstanceKeyWithLabel, nameValue.toString());
        };
    }

    public DataFetcher getDeleteAllFetcher(SchemaInstanceKey schemaInstanceKey) {
        return environment -> schemaInstanceRepository.deleteAllInstances(schemaInstanceKey);
    }

    public DataFetcher getUnauthorizedFetcher(String operationName) {
        return environment -> {
            throw new UnauthorizedException("User does not have mutation grant for: " + operationName);
        };
    }

    public DataFetcher getExternalReferenceDataFetcher(String memberFieldName, String authHeader) {
        return environment -> {
            @SuppressWarnings("unchecked")
            Map<String, String> id = (Map<String, String>)
                    ((Map<String, Object>) environment.getSource()).get(memberFieldName);
            if (id == null) {
                return null;
            }

            return findExternalById(environment, id, authHeader);
        };
    }

    private Object findExternalById(DataFetchingEnvironment environment, Map<String, String> id, String authHeader) {
        SelectionSet selections = environment.getFields().get(0).getSelectionSet();
        Map<String, Object> retMap = externalReferenceRepository.findById(selections, id, authHeader);
        if (retMap == null) {
            return null;
        }
        // Helper field to do type resolution in the type resolver
        retMap.put(TYPE_NAME_DECORATOR, id.get(REFERENCE_TYPE));
        return retMap;
    }

    public ExternalReferenceArrayDataFetcher getExternalReferenceArrayDataFetcher(SchemaInstanceField instanceField,
                                                                                  String authHeader) {
        return new ExternalReferenceArrayDataFetcher(instanceField, authHeader);
    }

    public MultiTypeDynamicArrayReferenceFetcher getMultiTypeDynamicArrayReferenceFetcher(
            SchemaInstanceField instanceField) {
        return new MultiTypeDynamicArrayReferenceFetcher(instanceField);
    }

    private abstract class AbstractReferenceArrayDataFetcher implements DataFetcher {

        final SchemaInstanceField instanceField;

        AbstractReferenceArrayDataFetcher(SchemaInstanceField instanceField) {
            this.instanceField = instanceField;
        }

        @Override
        public Object get(DataFetchingEnvironment environment) {
            List<Map<String, Object>> ids = extractIdentifiers(environment);
            return (ids == null || ids.isEmpty()) ? emptyList() : fetchData(environment, ids);
        }

        private List<Map<String, Object>> extractIdentifiers(DataFetchingEnvironment environment) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>)
                    ((Map<String, Object>) environment.getSource()).get(instanceField.getMemberFieldName());
            return result;
        }

        private Object fetchData(DataFetchingEnvironment environment, List<Map<String, Object>> ids) {
            return ids.stream().map(mapEntry -> fetchDataImpl(environment, mapEntry))
                    .filter(Objects::nonNull).collect(Collectors.toList());
        }

        abstract Object fetchDataImpl(DataFetchingEnvironment environment, Map<String, Object> mapEntry);
    }

    private class ExternalReferenceArrayDataFetcher extends AbstractReferenceArrayDataFetcher {

        private final String authHeader;

        ExternalReferenceArrayDataFetcher(SchemaInstanceField instanceField, String authHeader) {
            super(instanceField);
            this.authHeader = authHeader;
        }

        Object fetchDataImpl(DataFetchingEnvironment environment, Map<String, Object> id) {
            //ExternalReference entries are expected to be <String, String>
            @SuppressWarnings("unchecked")
            Map<String, String> idMap = (Map) id;

            return findExternalById(environment, idMap, authHeader);
        }
    }

    private class MultiTypeDynamicArrayReferenceFetcher extends AbstractReferenceArrayDataFetcher {
        MultiTypeDynamicArrayReferenceFetcher(SchemaInstanceField instanceField) {
            super(instanceField);
        }

        Object fetchDataImpl(DataFetchingEnvironment environment, Map<String, Object> mapEntry) {
            @SuppressWarnings("unchecked")
            String schemaName = ((Map<String, String>)mapEntry.get(SchemaInstance.SCHEMA_INSTANCE_KEY_FIELD))
                    .get(BaseKey.SCHEMA_NAME_FIELD);
            SchemaInstanceKey parentSchemaInstanceKey =
                    ((GraphQLInstanceRequestContext) environment.getContext()).getSchemaInstanceKey();

            SchemaInstanceKey schemaInstanceKey = parentSchemaInstanceKey.getChildSchemaInstanceKey(schemaName);
            SchemaInstance retMap = schemaInstanceRepository.findById(schemaInstanceKey,
                    (String)mapEntry.get(REFERENCE_ID));
            if (retMap == null) {
                return null;
            }
            // Helper field to do type resolution in the type resolver
            retMap.put(TYPE_NAME_DECORATOR, schemaName);
            return retMap;
        }
    }


    private static class InstanceReferencedByDataFetcher implements DataFetcher {
        private final InstanceOutputTypeService instanceOutputTypeService;

        private InstanceReferencedByDataFetcher(InstanceOutputTypeService instanceOutputTypeService) {
            this.instanceOutputTypeService = instanceOutputTypeService;
        }

        @Override
        public Object get(DataFetchingEnvironment environment) {
            SchemaInstance schemaInstance = environment.getSource();
            GraphQLInstanceRequestContext context = environment.getContext();

            return SchemaInstanceReferencedBy.fromSchemaInstances(
                    instanceOutputTypeService.findRefereeSchemaInstances(context.getSchemaInstanceKey(),
                            context.getSchemaKey(), schemaInstance.getId()));
        }
    }

}
