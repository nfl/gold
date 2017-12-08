package com.nfl.dm.shield.dynamic.domain.context;

import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.instancefield.SchemaInstanceField;
import graphql.schema.GraphQLObjectType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;

public class InstanceFieldBuilderContext {

    private final Map<SchemaKey, SchemaDescription> schemaDescriptionRequestCache;
    private Set<SchemaKey> referencedDefinitionIds;
    private final String schemaNamespace;
    private final int maxDepth;
    private final String authHeader;
    private final Map<SchemaKey, SchemaDescription> schemaDefinitionPreloadCache;
    private final Set<String> builtOutput;
    private final Map<String, GraphQLObjectType> objectTypeMap;

    public InstanceFieldBuilderContext(String schemaNamespace,
                                       int maxDepth,
                                       String authHeader,
                                       Set<SchemaKey> referencedDefinitionIds,
                                       Map<SchemaKey, SchemaDescription> schemaDefinitionPreloadCache) {
        this(schemaNamespace,
                maxDepth,
                authHeader,
                referencedDefinitionIds,
                schemaDefinitionPreloadCache,
                new ConcurrentHashMap<>(),
                new ConcurrentSkipListSet<>(),
                new ConcurrentHashMap<>());
    }

    private InstanceFieldBuilderContext(String schemaNamespace,
                                        int maxDepth,
                                        String authHeader,
                                        Set<SchemaKey> referencedDefinitionIds,
                                        Map<SchemaKey, SchemaDescription> schemaDefinitionPreloadCache,
                                        Map<SchemaKey, SchemaDescription> schemaDescriptionRequestCache,
                                        Set<String> builtOutput,
                                        Map<String, GraphQLObjectType> objectTypeMap) {
        this.schemaNamespace = schemaNamespace;
        this.maxDepth = maxDepth;
        this.authHeader = authHeader;
        this.schemaDescriptionRequestCache = schemaDescriptionRequestCache;
        this.schemaDefinitionPreloadCache = schemaDefinitionPreloadCache;
        this.referencedDefinitionIds = referencedDefinitionIds;
        this.builtOutput = builtOutput;
        this.objectTypeMap = objectTypeMap;
    }

    public static class InstanceFieldBuilderContextBuilder {
        private String schemaNamespace;
        private int maxDepth;
        private String authHeader;
        private Set<SchemaKey> referencedDefinitionIds;
        private Map<SchemaKey, SchemaDescription> schemaDefinitionPreloadCache;

        public static InstanceFieldBuilderContextBuilder getInstance() {
            return new InstanceFieldBuilderContextBuilder();
        }

        public InstanceFieldBuilderContextBuilder setSchemaNamespace(String schemaNamespace) {
            this.schemaNamespace = schemaNamespace;
            return this;
        }

        public InstanceFieldBuilderContextBuilder setMaxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public InstanceFieldBuilderContextBuilder setAuthHeader(String authHeader) {
            this.authHeader = authHeader;
            return this;
        }

        public InstanceFieldBuilderContextBuilder setReferencedDefinitionIds(Set<SchemaKey> referencedDefinitionIds) {
            this.referencedDefinitionIds = referencedDefinitionIds;
            return this;
        }

        public InstanceFieldBuilderContextBuilder setSchemaDefinitionPreloadCache(
                Map<SchemaKey, SchemaDescription> schemaDefinitionPreloadCache) {
            this.schemaDefinitionPreloadCache = schemaDefinitionPreloadCache;
            return this;
        }

        public InstanceFieldBuilderContext build() {
            return new InstanceFieldBuilderContext(
                    schemaNamespace,
                    maxDepth,
                    authHeader,
                    referencedDefinitionIds,
                    schemaDefinitionPreloadCache);
        }
    }

    public Predicate<SchemaInstanceField> getFieldFilter() {
        return schemaInstanceField -> (maxDepth > 0) ||
                schemaInstanceField.isLiteralType(schemaInstanceField.getArrayEntryType());
    }

    public InstanceFieldBuilderContext recurseDown() {
        return new InstanceFieldBuilderContext(
                schemaNamespace,
                maxDepth - 1,
                authHeader,
                referencedDefinitionIds,
                schemaDefinitionPreloadCache,
                schemaDescriptionRequestCache,
                builtOutput,
                objectTypeMap);
    }

    public String getSchemaNamespace() {
        return schemaNamespace;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public Map<SchemaKey, SchemaDescription> getSchemaDescriptionRequestCache() {
        return schemaDescriptionRequestCache;
    }

    public Set<SchemaKey> getReferencedDefinitionIds() {
        return referencedDefinitionIds;
    }

    public Map<SchemaKey, SchemaDescription> getSchemaDefinitionPreloadCache() {
        return schemaDefinitionPreloadCache;
    }

    public boolean computeOutput(String name) {
        return builtOutput.add(name);
    }

    public boolean hasObjectType(String name) {
        return objectTypeMap.containsKey(name);
    }

    public void addObjectType(String name, GraphQLObjectType value) {
        objectTypeMap.put(name, value);
    }

    public GraphQLObjectType retrieveObjectType(String name) {
        return objectTypeMap.get(name);
    }
}
