package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Get Sonar happier.
 */
@Test
public class ValueTypeTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void missingOtherType() {
        Map<String, Object> initFields = new HashMap<>();
        initFields.put("memberFieldName", "testField");

        InstanceFieldType.VALUE_OBJECT.fieldFactory(null, initFields);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void otherTypeEmpty() {
        Map<String, Object> initFields = new HashMap<>();
        initFields.put(SchemaInstanceField.MEMBER_FIELD_NAME_FIELD, "testField");
        initFields.put("otherTypeName", "");

        InstanceFieldType.VALUE_OBJECT.fieldFactory(null, initFields);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void missingValueDef() {
        Map<String, Object> initFields = new HashMap<>();
        initFields.put(SchemaInstanceField.MEMBER_FIELD_NAME_FIELD, "testField");
        initFields.put("otherTypeName", "missingValueDef");

        InstanceFieldType.VALUE_OBJECT.fieldFactory(new SchemaDescription(), initFields);
    }
}
