package com.nfl.dm.shield.dynamic.repository;

import com.nfl.dm.shield.dynamic.domain.Count;
import com.nfl.dm.shield.dynamic.domain.instance.InMemorySchemaInstanceHolder;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceHolder;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.schema.InMemorySchemaDescriptionHolder;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.transform;
import static com.nfl.dm.shield.dynamic.config.HashConfig.DEFAULT_HASH_TABLE_SIZE;
import static com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance.UPDATE_DATE_FIELD;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

@Service
public class InMemorySchemaRepository extends BaseRepositoryImpl
        implements SchemaRepository, SchemaInstanceRepository {

    private final Map<SchemaKey, InMemorySchemaDescriptionHolder> allTheSchemas = new ConcurrentHashMap<>(DEFAULT_HASH_TABLE_SIZE);

    private final Map<SchemaInstanceKey, Map<String, InMemorySchemaInstanceHolder>> instances
            = new ConcurrentHashMap<>(DEFAULT_HASH_TABLE_SIZE);

    public InMemorySchemaRepository() {
    }

    @Override
    public List<SchemaDescription> list(String schemaNamespace) {
        return transform(new ArrayList<>(allTheSchemas.values()), InMemorySchemaDescriptionHolder::toSchemaDescription);
    }

    @Override
    public SchemaDescription findByName(SchemaKey schemaKey) {
        InMemorySchemaDescriptionHolder schemaHolder = allTheSchemas.get(schemaKey);
        return schemaHolder == null ? null : schemaHolder.toSchemaDescription();
    }

    @Override
    public List<SchemaDescription> findByNames(List<SchemaKey> schemaKeys) {
        return schemaKeys.stream()
                .map(allTheSchemas::get)
                .map(InMemorySchemaDescriptionHolder::toSchemaDescription)
                .collect(toList());
    }

    @Override
    public SchemaDescription upsert(SchemaDescription changedSchema) {
        SchemaKey schemaKey = changedSchema.getSchemaKey();
        allTheSchemas.put(schemaKey, new InMemorySchemaDescriptionHolder(changedSchema));
        return allTheSchemas.get(schemaKey).toSchemaDescription();
    }

    @Override
    public SchemaDescription delete(SchemaKey schemaKey) {
        InMemorySchemaDescriptionHolder schemaHolder = allTheSchemas.remove(schemaKey);
        return schemaHolder == null ? null : schemaHolder.toSchemaDescription();
    }

    public void clearForSchemaTesting() {
        allTheSchemas.clear();
    }

    public void clearForInstanceTesting() {
        instances.clear();
    }


    @Override
    public List<SchemaInstance> listInstances(SchemaInstanceKey schemaInstanceKey) {
        if (!instances.containsKey(schemaInstanceKey)) {
            return emptyList();
        }

        return instances.get(schemaInstanceKey).values().stream()
                .map(SchemaInstanceHolder::toSchemaInstance)
                .map(SchemaInstance::makeCopy)
                .collect(toList());
    }

    @Override
    public SchemaInstance findById(SchemaInstanceKey schemaInstanceKey, String id) {
        if (!instances.containsKey(schemaInstanceKey)) {
            return null;
        }

        Map<String, InMemorySchemaInstanceHolder> instanceMap = instances.get(schemaInstanceKey);

        if (instanceMap.containsKey(id)) {
            return instanceMap.get(id).toSchemaInstance().makeCopy();
        }

        // Not found
        return null;
    }

    @Override
    public List<SchemaInstance> findInstances(SchemaInstanceKey schemaInstanceKey, List<String> instanceIds) {
        if (!instances.containsKey(schemaInstanceKey)) {
            return emptyList();
        }

        Map<String, InMemorySchemaInstanceHolder> instanceMap = instances.get(schemaInstanceKey);

        Set<SchemaInstance> instances = instanceIds.stream()
                .distinct().filter(instanceMap::containsKey)
                .map(id -> instanceMap.get(id).toSchemaInstance().makeCopy())
                .collect(Collectors.toSet());

        Map<String, SchemaInstance> store = instances.stream()
                .collect(Collectors.toMap(SchemaInstance::getId, sih -> sih));

        return buildListByIds(store, instanceIds);
    }

    @Override
    public SchemaInstance upsert(SchemaInstanceKey schemaInstanceKey, SchemaInstance changedInstance) {

        Map<String, InMemorySchemaInstanceHolder> instanceMap = instances.containsKey(schemaInstanceKey) ?
                instances.get(schemaInstanceKey) : initInstance(schemaInstanceKey);

        // Set updateDate
        changedInstance.put(UPDATE_DATE_FIELD, System.currentTimeMillis());

        InMemorySchemaInstanceHolder sih = new InMemorySchemaInstanceHolder(schemaInstanceKey, changedInstance);
        SchemaInstance roundTripInstance = sih.toSchemaInstance();
        instanceMap.put(roundTripInstance.getId(), sih);
        return instanceMap.get(roundTripInstance.getId()).toSchemaInstance().makeCopy();
    }

    private Map<String, InMemorySchemaInstanceHolder> initInstance(SchemaInstanceKey schemaInstanceKey) {
        Map<String, InMemorySchemaInstanceHolder> retMap = new ConcurrentHashMap<>(DEFAULT_HASH_TABLE_SIZE);
        instances.put(schemaInstanceKey, retMap);
        return retMap;
    }

    @Override
    public SchemaInstance deleteInstance(SchemaInstanceKey schemaInstanceKey, String id) {
        if (!instances.containsKey(schemaInstanceKey)) {
            return null;
        }

        Map<String, InMemorySchemaInstanceHolder> instanceMap = instances.get(schemaInstanceKey);
        InMemorySchemaInstanceHolder instanceHolder = instanceMap.remove(id);
        return instanceHolder == null ? null : instanceHolder.toSchemaInstance();
    }

    @Override
    public boolean hasInstances(String schemaName) {
        return instances.keySet().stream().anyMatch(schemaKey -> schemaKey.getSchemaName().equals(schemaName));
    }

    public long countInstances(SchemaInstanceKey schemaInstanceKey) {
        return instances.getOrDefault(schemaInstanceKey, emptyMap()).size();
    }

    @Override
    public Count deleteAllInstances(SchemaInstanceKey schemaInstanceKey) {
        if (!instances.containsKey(schemaInstanceKey)) {
            return new Count(-1);
        }

        long retCount = countInstances(schemaInstanceKey);
        initInstance(schemaInstanceKey);
        return new Count(retCount);
    }

    @Override
    public Stream<InMemorySchemaInstanceHolder> findInstancesBySchemaKeyAndStringInClob(
            SchemaInstanceKey lookupSchemaInstanceKey,
            SchemaKey lookupSchemaKey, String searchString) {
        // Get instances for the specified related schema keys
        // Converting to instance holder to apply same filtering logic than the one used for other repositories
        List<InMemorySchemaInstanceHolder> instanceHoldersForRelatedSchemas = instances.keySet().stream()
                .filter(schemaInstanceKey ->
                        schemaInstanceKey.getInstanceNamespace().equals(lookupSchemaInstanceKey.getInstanceNamespace())
                                && schemaInstanceKey.getSchemaName().equals(lookupSchemaKey.getSchemaName()))
                //Subset of Instance keys that match the lookup schemaKey and instance namespace
                .map(instanceKeyInSchemaAndNamespace -> instances.get(instanceKeyInSchemaAndNamespace).values())
                .flatMap(Collection::stream)
                .collect(toList());

        return instanceHoldersForRelatedSchemas.stream().filter(instanceHolder ->
                instanceHolder.getInstanceClob().contains(searchString));
    }
}
