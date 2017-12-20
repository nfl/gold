package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.domain.context.GraphQLSchemaRequestContext;
import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import com.nfl.dm.shield.dynamic.repository.ExternalReferenceRepository;
import com.nfl.dm.shield.dynamic.repository.SchemaInstanceRepository;
import com.nfl.dm.shield.dynamic.repository.SchemaRepository;
import graphql.schema.GraphQLOutputType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.AbstractReferenceType.REFERENCE_ID;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.AbstractReferenceType.TYPE_NAME_DECORATOR;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Service
public class InstanceOutputTypeServiceFactory {

    private final SchemaRepository schemaRepository;
    private final SchemaInstanceRepository schemaInstanceRepository;
    private final ExternalReferenceRepository externalReferenceRepository;
    private final DataFetcherFactory dataFetcherFactory;
    private final SchemaService schemaService;
    private final InstanceService instanceService;

    @Autowired
    public InstanceOutputTypeServiceFactory(SchemaRepository schemaRepository,
                                            SchemaInstanceRepository schemaInstanceRepository,
                                            ExternalReferenceRepository externalReferenceRepository,
                                            DataFetcherFactory dataFetcherFactory,
                                            SchemaService schemaService,
                                            InstanceService instanceService) {
        this.schemaRepository = schemaRepository;
        this.schemaInstanceRepository = schemaInstanceRepository;
        this.externalReferenceRepository = externalReferenceRepository;
        this.dataFetcherFactory = dataFetcherFactory;
        this.schemaService = schemaService;
        this.instanceService = instanceService;
    }

    InstanceOutputTypeService getInstance(InstanceFieldBuilderContext instanceFieldBuilderContext) {
        if (instanceFieldBuilderContext.getSchemaDefinitionPreloadCache() == null
                        || instanceFieldBuilderContext.getReferencedDefinitionIds() == null) {
            throw new IllegalArgumentException("Preload Cache must not be null");
        }
        return new InstanceOutputTypeServiceImpl(instanceFieldBuilderContext.getAuthHeader(),
                instanceFieldBuilderContext.getSchemaDescriptionRequestCache(),
                instanceFieldBuilderContext.getReferencedDefinitionIds(),
                instanceFieldBuilderContext.getSchemaDefinitionPreloadCache());
    }

    InstanceOutputTypeService getInstance(GraphQLSchemaRequestContext graphQLSchemaRequestContext) {
        return new InstanceOutputTypeServiceImpl(graphQLSchemaRequestContext.getMutablePerms().getAuthHeader(),
                graphQLSchemaRequestContext.getSchemaDescriptionRequestCache());
    }

    private class InstanceOutputTypeServiceImpl implements InstanceOutputTypeService {

        private final String authHeader;
        private final Map<SchemaKey, SchemaDescription> schemaDescriptionRequestCache;
        private final Map<SchemaKey, SchemaDescription> schemaDefinitionPreloadCache;
        private final Set<SchemaKey> referencedDefinitionIds;

        private InstanceOutputTypeServiceImpl(String authHeader, Map<SchemaKey,
                SchemaDescription> schemaDescriptionRequestCache) {
            this(authHeader, schemaDescriptionRequestCache, new HashSet<>(), new ConcurrentHashMap<>());
        }

        private InstanceOutputTypeServiceImpl(String authHeader,
                                              Map<SchemaKey, SchemaDescription> schemaDescriptionRequestCache,
                                              Set<SchemaKey> referencedDefinitionIds,
                                              Map<SchemaKey, SchemaDescription> schemaDefinitionPreloadCache) {
            this.authHeader = authHeader;
            this.schemaDescriptionRequestCache = schemaDescriptionRequestCache;
            this.referencedDefinitionIds = referencedDefinitionIds;
            this.schemaDefinitionPreloadCache = schemaDefinitionPreloadCache;
        }

        @Override
        public SchemaDescription findSchemaDescriptionByName(SchemaKey schemaKey) {
            return schemaDefinitionPreloadCache.computeIfAbsent(
                    schemaKey,
                    schemaKey1 -> {
                        referencedDefinitionIds.add(schemaKey1);
                        return schemaDescriptionRequestCache.computeIfAbsent(schemaKey, schemaRepository::findByName);
                    });
        }

        @Override
        public SchemaInstance findSchemaInstance(SchemaInstanceKey schemaInstanceKey, String id) {
            return schemaInstanceRepository.findById(schemaInstanceKey, id);
        }

        @Override
        public List<SchemaInstance> findSchemaInstances(SchemaInstanceKey schemaInstanceKey, List<String> instanceIds) {
            return schemaInstanceRepository.findInstances(schemaInstanceKey, instanceIds);
        }

        @Override
        public SchemaInstance findMultiTypeById(SchemaInstanceKey schemaInstanceKey, Map<String, Object> id) {
            SchemaInstance retMap = schemaInstanceRepository.findById(schemaInstanceKey, (String)id.get(REFERENCE_ID));
            if (retMap == null) {
                return null;
            }
            // Helper field to do type resolution in the type resolver
            retMap.put(TYPE_NAME_DECORATOR, schemaInstanceKey.getSchemaName());

            return retMap;
        }

        @Override
        public GraphQLOutputType deriveFromExternalTypeName(String typeName) {
            return externalReferenceRepository.deriveFromExternalTypeName(typeName, authHeader);
        }

        @Override
        public DataFetcherFactory getDataFetcherFactory() {
            return dataFetcherFactory;
        }

        @Override
        public List<SchemaInstance> findRefereeSchemaInstances(SchemaInstanceKey schemaInstanceKey, SchemaKey schemaKey,
                                                               String id) {
            // Get schema description for current element
            SchemaDescription schemaDescription = findSchemaDescriptionByName(schemaKey);

            return instanceService.findRefereeSchemaInstances(schemaInstanceKey, schemaDescription, id);
        }
    }
}