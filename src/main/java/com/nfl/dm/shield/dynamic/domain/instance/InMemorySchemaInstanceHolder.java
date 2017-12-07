package com.nfl.dm.shield.dynamic.domain.instance;

import java.util.HashMap;
import java.util.Map;

import static com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance.SCHEMA_INSTANCE_KEY_FIELD;

@SuppressWarnings("unused")
public class InMemorySchemaInstanceHolder extends SchemaInstanceHolder {

    private final SchemaInstanceKey schemaInstanceKey;

    public InMemorySchemaInstanceHolder(SchemaInstanceKey schemaInstanceKey, Map<String, Object> fieldValues) {
        this.schemaInstanceKey = schemaInstanceKey;
        // schemaInstanceKey may not have been added to the field clob.
        Map<String, Object> tmpMap = new HashMap<>(fieldValues);
        tmpMap.put(SCHEMA_INSTANCE_KEY_FIELD, this.schemaInstanceKey);
        setInstanceClob(toClob(tmpMap));
    }

    @Override
    protected SchemaInstanceKey getSchemaInstanceKey() {
        return schemaInstanceKey;
    }

}
