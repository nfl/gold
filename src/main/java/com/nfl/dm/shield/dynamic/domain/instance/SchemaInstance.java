package com.nfl.dm.shield.dynamic.domain.instance;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SchemaInstance implements Map<String, Object> {

    public static final String ID = "id";

    public static final String INSTANCE_REFERENCED_BY_FIELD = "referencedBy";

    public static final String SCHEMA_INSTANCE_KEY_FIELD = "schemaInstanceKey";

    public static final String UPDATE_DATE_FIELD = "updateDate";

    private Map<String, Object> fieldValues = new ConcurrentHashMap<>(89);

    private SchemaInstanceKey schemaInstanceKey;

    public SchemaInstance(Map<String, Object> fieldValues) {
        this.fieldValues = fieldValues;
    }

    public SchemaInstance(Map<String, Object> fieldValues, SchemaInstanceKey schemaInstanceKey) {
        this.fieldValues = fieldValues;
        this.schemaInstanceKey = schemaInstanceKey;
    }

    // MUST always have an ID, so no NPE
    public String getId() {
        return fieldValues.get(ID).toString();
    }

    // Avoid the messiness with cloning.
    public SchemaInstance makeCopy() {
        Map<String, Object> fieldCopy = new ConcurrentHashMap<>(fieldValues);
        return new SchemaInstance(fieldCopy, schemaInstanceKey);
    }

    @Override
    public int size() {
        return fieldValues.size();
    }

    @Override
    public boolean isEmpty() {
        return fieldValues.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return fieldValues.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("contains value");
    }


    /**
     * When GraphQL java sees an instance of Map, it uses Map.get() instead of calling the getters.
     * See graphql.schema.PropertyDataFetcher
     */
    @Override
    public Object get(Object key) {
        return fieldValues.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return fieldValues.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        return fieldValues.keySet();
    }

    @Override
    public Collection<Object> values() {
        return fieldValues.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return fieldValues.entrySet();
    }
}
