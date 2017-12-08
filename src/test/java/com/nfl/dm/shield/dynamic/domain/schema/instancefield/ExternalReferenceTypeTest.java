package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.SchemaInstanceField.POSSIBLE_TYPES_FIELD;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.SchemaInstanceField.SERVICE_KEY_FIELD;
import static java.util.Collections.emptyList;

@SuppressWarnings("unused")
@Test
public class ExternalReferenceTypeTest {

    private static final String SERVICE_KEY_VALUE = "SHIELD";

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void missingServiceKey() {
        Map<String, Object> initFields = new HashMap<>();
        initFields.put("memberFieldName", "testField");

        InstanceFieldType.EXTERNAL_REFERENCE.fieldFactory(null, initFields);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void serviceKeyEmpty() {
        Map<String, Object> initFields = baseMapBuild();
        initFields.put(SERVICE_KEY_FIELD, "");

        InstanceFieldType.EXTERNAL_REFERENCE.fieldFactory(null, initFields);
    }

    private Map<String, Object> baseMapBuild() {
        Map<String, Object> initFields = new HashMap<>();
        initFields.put(SchemaInstanceField.MEMBER_FIELD_NAME_FIELD, "testField");
        return initFields;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void missingPossibleTypes() {
        Map<String, Object> initFields = baseMapBuild();
        initFields.put(SERVICE_KEY_FIELD, "");
        initFields.put(POSSIBLE_TYPES_FIELD, emptyList());

        InstanceFieldType.EXTERNAL_REFERENCE.fieldFactory(null, initFields);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void possibleTypesEmpty() {
        Map<String, Object> initFields = baseMapBuild();
        initFields.put(SERVICE_KEY_FIELD, SERVICE_KEY_VALUE);
        initFields.put(POSSIBLE_TYPES_FIELD, emptyList());

        InstanceFieldType.EXTERNAL_REFERENCE.fieldFactory(null, initFields);
    }
}
