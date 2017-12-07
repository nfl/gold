package com.nfl.dm.shield.dynamic.domain.context;

import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import com.nfl.dm.shield.dynamic.service.CacheService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GraphQLInstanceRequestContext {

    private Map<SchemaInstanceKey, Set<String>> referencedInstanceIdsMap;

    private Map<String, SchemaInstance> schemaInstanceRequestCache = new ConcurrentHashMap<>();

    private Map<CacheService.SchemaInstanceIdentifier, SchemaInstance> schemaInstancePreloadCache =
            new ConcurrentHashMap<>();

    private SchemaInstanceKey schemaInstanceKey;

    private SchemaKey schemaKey;

    public SchemaInstanceKey getSchemaInstanceKey() {
        return schemaInstanceKey;
    }

    public GraphQLInstanceRequestContext(SchemaInstanceKey schemaInstanceKey, SchemaKey schemaKey) {
        this.schemaInstanceKey = schemaInstanceKey;
        this.schemaKey = schemaKey;
    }

    public Map<String, SchemaInstance> getSchemaInstanceRequestCache() {
        return schemaInstanceRequestCache;
    }

    public Map<SchemaInstanceKey, Set<String>> getReferencedInstanceIdsMap() {
        return referencedInstanceIdsMap;
    }

    public void setReferencedInstanceIdsMap(Map<SchemaInstanceKey, Set<String>> referencedInstanceIdsMap) {
        this.referencedInstanceIdsMap = referencedInstanceIdsMap;
    }

    public Map<CacheService.SchemaInstanceIdentifier, SchemaInstance> getSchemaInstancePreloadCache() {
        return schemaInstancePreloadCache;
    }

    public SchemaKey getSchemaKey() {
        return schemaKey;
    }

    public void addSchemaInstanceIdsToPreloadCache(SchemaInstanceKey schemaInstanceKey, List<String> ids) {
        if (referencedInstanceIdsMap != null) {

            referencedInstanceIdsMap.compute(schemaInstanceKey, (instanceKey, setOfIds) -> {
                if (setOfIds == null) {
                    Set<String> newSet = new HashSet<>();
                    newSet.addAll(ids);
                    return newSet;
                } else {
                    setOfIds.addAll(ids);
                    return setOfIds;
                }
            });
        }
    }

    public SchemaInstance getFromAnyCache(SchemaInstanceKey schemaInstanceKey, String id) {
        SchemaInstance schemaInstance = getSchemaInstancePreloadCache().get(
                new CacheService.SchemaInstanceIdentifier(schemaInstanceKey, id));

        if (schemaInstance == null) {
            schemaInstance = getSchemaInstanceRequestCache().get(id);
        }

        return schemaInstance;
    }

    public boolean isPresentInAnyCache(SchemaInstanceKey schemaInstanceKey, String id) {
        return getSchemaInstancePreloadCache().containsKey(
                new CacheService.SchemaInstanceIdentifier(schemaInstanceKey, id))
                || getSchemaInstanceRequestCache().containsKey(id);

    }
}
