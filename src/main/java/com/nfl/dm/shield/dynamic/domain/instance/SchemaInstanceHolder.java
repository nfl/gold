package com.nfl.dm.shield.dynamic.domain.instance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

import static com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance.SCHEMA_INSTANCE_KEY_FIELD;
import static com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance.UPDATE_DATE_FIELD;

@SuppressWarnings("unused")
public abstract class SchemaInstanceHolder {

    private String instanceClob;

    protected abstract SchemaInstanceKey getSchemaInstanceKey();

    public String getInstanceClob() {
        return instanceClob;
    }

    public void setInstanceClob(String instanceClob) {
        this.instanceClob = instanceClob;
    }

    public SchemaInstance toSchemaInstance() {
        Map<String, Object> fieldValues = fromClob(getInstanceClob());
        /* Older data does not have SchemaInstanceKey persisted as part of the clob.
           Ignore what is stored and just compute it here. */
        fieldValues.put(SCHEMA_INSTANCE_KEY_FIELD, getSchemaInstanceKey());

        // in case there is no updateDate field, return the Epoch
        fieldValues.putIfAbsent(UPDATE_DATE_FIELD, 0L);

        return new SchemaInstance(fieldValues);
    }

    private static ObjectMapper objectMapper = new ObjectMapper();

    static Map<String, Object> fromClob(String rawString) {
        Map<String, Object> parsedJson;
        try {
            //noinspection unchecked
            parsedJson = objectMapper.readValue(rawString, Map.class);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return parsedJson;
    }

    protected static String toClob(Map<String, Object> tmpMap) {
        try {
            return objectMapper.writeValueAsString(tmpMap);
        } catch (JsonProcessingException jpe) {
            throw new RuntimeException(jpe);
        }
    }
}
