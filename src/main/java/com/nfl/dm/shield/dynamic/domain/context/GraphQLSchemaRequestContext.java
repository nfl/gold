package com.nfl.dm.shield.dynamic.domain.context;

import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;

import java.util.Map;

public class GraphQLSchemaRequestContext {

    private Map<String, Object> variableMap;
    private SchemaWriteAccess mutablePerms;
    private Map<SchemaKey, SchemaDescription> schemaDescriptionRequestCache;

    public GraphQLSchemaRequestContext(Map<String, Object> variableMap, SchemaWriteAccess mutablePerms,
                                       Map<SchemaKey, SchemaDescription> schemaDescriptionRequestCache) {
        this.variableMap = variableMap;
        this.mutablePerms = mutablePerms;
        this.schemaDescriptionRequestCache = schemaDescriptionRequestCache;
    }

    public Map<String, Object> getVariableMap() {
        return variableMap;
    }

    public SchemaWriteAccess getMutablePerms() {
        return mutablePerms;
    }

    public Map<SchemaKey, SchemaDescription> getSchemaDescriptionRequestCache() {
        return schemaDescriptionRequestCache;
    }
}
