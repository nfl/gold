package com.nfl.dm.shield.dynamic.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nfl.dm.shield.dynamic.domain.context.GraphQLInstanceRequestContext;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "WeakerAccess"})
@Service
public class CacheService {

    private Cache<SchemaInstanceRequestIdentifier, Map<SchemaInstanceKey, Set<String>>> graphs = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES).build();

    private Cache<SchemaKey, Set<SchemaKey>> schemaDescriptionIdCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES).build();


    public Cache<SchemaInstanceRequestIdentifier, Map<SchemaInstanceKey, Set<String>>> getGraphs() {
        return graphs;
    }

    public Cache<SchemaKey, Set<SchemaKey>> getSchemaDescriptionIdCache() {
        return schemaDescriptionIdCache;
    }

    public static class SchemaInstanceRequestIdentifier {
        private final SchemaInstanceKey schemaInstanceKey;
        private final List<String> ids;

        public SchemaInstanceRequestIdentifier(SchemaInstanceKey schemaInstanceKey, List<String> ids) {
            this.schemaInstanceKey = schemaInstanceKey;
            this.ids = ids;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;
            SchemaInstanceRequestIdentifier that = (SchemaInstanceRequestIdentifier) other;
            return Objects.equals(this.schemaInstanceKey, that.schemaInstanceKey) && Objects.equals(ids, that.ids);
        }

        @Override
        public int hashCode() {
            int result = schemaInstanceKey != null ? schemaInstanceKey.hashCode() : 0;
            result = 31 * result + (ids != null ? ids.hashCode() : 0);
            return result;
        }
    }

    @SuppressWarnings("unused")
    public static class SchemaInstanceIdentifier {
        private final SchemaInstanceKey schemaInstanceKey;
        private final String id;

        public SchemaInstanceIdentifier(SchemaInstanceKey schemaInstanceKey, String id) {
            this.schemaInstanceKey = schemaInstanceKey;
            this.id = id;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;
            SchemaInstanceIdentifier that = (SchemaInstanceIdentifier) other;
            return Objects.equals(this.schemaInstanceKey, that.schemaInstanceKey) && Objects.equals(this.id, that.id);
        }

        @Override
        public int hashCode() {
            int result = schemaInstanceKey != null ? schemaInstanceKey.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }
    }

    public static SchemaInstance getSchemaInstance(SchemaInstanceKey schemaInstanceKey, String id,
                                                   GraphQLInstanceRequestContext context,
                                                   InstanceOutputTypeService instanceOutputTypeService) {
        SchemaInstance schemaInstance = context.getSchemaInstancePreloadCache().get(new CacheService.SchemaInstanceIdentifier(schemaInstanceKey, id));
        if (schemaInstance != null) {
            return schemaInstance;
        } else {

            schemaInstance = context.getSchemaInstanceRequestCache()
                    .computeIfAbsent(id, key -> instanceOutputTypeService.findSchemaInstance(schemaInstanceKey, id));

            context.addSchemaInstanceIdsToPreloadCache(schemaInstanceKey, Collections.singletonList(id));
        }

        return schemaInstance;
    }

    public static List<SchemaInstance> getSchemaInstances(SchemaInstanceKey schemaInstanceKey, List<String> ids,
                                                   GraphQLInstanceRequestContext context,
                                                   InstanceOutputTypeService instanceOutputTypeService) {

        boolean cacheHasAllValues = ids.stream().allMatch(id -> context.isPresentInAnyCache(schemaInstanceKey, id));

        if (!cacheHasAllValues) {
            loadMissingInstancesIntoCache(schemaInstanceKey, ids, context, instanceOutputTypeService);

        }

        return ids.stream()
                .map(id -> context.getFromAnyCache(schemaInstanceKey, id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    private static void loadMissingInstancesIntoCache(SchemaInstanceKey schemaInstanceKey, List<String> ids,
                                                               GraphQLInstanceRequestContext context,
                                                               InstanceOutputTypeService instanceOutputTypeService) {

        List<String> missingIds = ids.stream()
                .filter(id -> !context.isPresentInAnyCache(schemaInstanceKey, id))
                .distinct()
                .collect(Collectors.toList());

        List<SchemaInstance> missingSchemaInstances =
                instanceOutputTypeService.findSchemaInstances(schemaInstanceKey, missingIds).stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        Map<String, SchemaInstance> missingSchemaInstancesMap = missingSchemaInstances.stream().collect(Collectors.toMap(SchemaInstance::getId, Function.identity()));

        context.getSchemaInstanceRequestCache().putAll(missingSchemaInstancesMap);
        context.addSchemaInstanceIdsToPreloadCache(schemaInstanceKey, ids);

    }

}
