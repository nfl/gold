package com.nfl.dm.shield.dynamic.domain.instance;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance.ID;
import static com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance.SCHEMA_INSTANCE_KEY_FIELD;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.*;

@Test
public class SchemaInstanceTest {

    @Test(expectedExceptions = RuntimeException.class)
    public void hydrateClob() {
        SchemaInstanceHolder.fromClob("fizbit");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void dehydrateSchemaInstanceLackingId() {
        Map<String, Object> bogusInit = singletonMap("bogus", new Bogus());

        SchemaInstanceKey instanceKey = new SchemaInstanceKey("bogusObjectName",
                "bogusSchemaNamespace","bogusNamespace");

        SchemaInstanceHolder schemaInstanceHolder =
                new InMemorySchemaInstanceHolder(instanceKey, bogusInit);
        schemaInstanceHolder.toSchemaInstance();
    }

    @Test
    public void dehydrateSchemaInstance_instanceMustHaveSchemaInstanceKeyAmongFields() {
        SchemaInstanceKey schemaInstanceKey = new SchemaInstanceKey("someObjectName",
                "bogusSchemaNamespace","someNamespace");
        Map<String, Object> fieldValues = singletonMap("id", "someId");

        SchemaInstanceHolder schemaInstanceHolder = new InMemorySchemaInstanceHolder(schemaInstanceKey, fieldValues);
        SchemaInstance dehydratedInstance = schemaInstanceHolder.toSchemaInstance();

        assertEquals(dehydratedInstance.get(ID), "someId");
        assertEquals(dehydratedInstance.get(SCHEMA_INSTANCE_KEY_FIELD), schemaInstanceKey);
    }

    // Non-serializable class to prompt error in toClob()
    private static class Bogus {
    }

    // make sonar happy with coverage methods not covered by other tests.
    public void coverOtherMethods() {
        SchemaInstance testInstance = new SchemaInstance(new HashMap<>());
        assertEquals(testInstance.size(), 0);
        assertFalse(testInstance.containsKey("zzz"));
        assertTrue(testInstance.isEmpty());
        assertTrue(testInstance.keySet().isEmpty());
        assertTrue(testInstance.values().isEmpty());
        assertTrue(testInstance.entrySet().isEmpty());

        // Code coverage move.
        testInstance.put("zzz", "yyy");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void coverRemove() {
        SchemaInstance testInstance = new SchemaInstance(emptyMap());
        testInstance.remove("zzzz");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void coverPutAll() {
        SchemaInstance testInstance = new SchemaInstance(emptyMap());
        testInstance.putAll(emptyMap());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void coverClear() {
        SchemaInstance testInstance = new SchemaInstance(emptyMap());
        testInstance.clear();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void coverContainsValue() {
        SchemaInstance testInstance = new SchemaInstance(emptyMap());
        //noinspection ResultOfMethodCallIgnored
        testInstance.containsValue("fizbit");
    }
}
