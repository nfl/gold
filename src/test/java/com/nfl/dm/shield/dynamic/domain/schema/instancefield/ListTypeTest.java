package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.google.common.collect.ImmutableMap;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription.NAME_FIELD;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.InstanceFieldType.MULTI_TYPE_DYNAMIC_REFERENCE;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.SchemaInstanceField.LIST_TARGET_FIELD;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.SchemaInstanceField.MEMBER_FIELD_NAME_FIELD;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.SchemaInstanceField.POSSIBLE_TYPES_FIELD;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Get Sonar happier.
 */
@Test
public class ListTypeTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void noListOfLists() {
        Map<String, Object> initFields = new HashMap<>();
        initFields.put(MEMBER_FIELD_NAME_FIELD, "testField");
        initFields.put(LIST_TARGET_FIELD, InstanceFieldType.LIST.name());

        InstanceFieldType.LIST.fieldFactory(null, initFields);
    }

    @Test
    public void testMultitypeArrayRelationCheck() throws Exception {
        String schemaNamespace = "testSchemaNS";
        SchemaDescription skibobSchema = new SchemaDescription(schemaNamespace, singletonMap(NAME_FIELD, "skibob"), null);
        SchemaDescription referringSchema = new SchemaDescription(schemaNamespace, singletonMap(NAME_FIELD, "referring"), null);
        SchemaInstanceField referringArrayField = InstanceFieldType.LIST.fieldFactory(
                referringSchema,
                ImmutableMap.of(
                        MEMBER_FIELD_NAME_FIELD, "multiField",
                        LIST_TARGET_FIELD, MULTI_TYPE_DYNAMIC_REFERENCE,
                        POSSIBLE_TYPES_FIELD, singletonList(skibobSchema.getName())));

        /* Just to have test coverage for the overloaded version of LIST.fieldFactory(): */
        SchemaInstanceField referringArrayFieldCopy = InstanceFieldType.LIST.fieldFactory(referringSchema, referringArrayField);

        assertEquals(referringArrayFieldCopy.getPossibleTypes(), referringArrayField.getPossibleTypes());

        /* Test hasRelation() explicitly... */
        assertTrue(InstanceFieldType.LIST.hasRelation(referringArrayFieldCopy, skibobSchema));
        /* ...and  implicitly */
        assertTrue(referringArrayField.getMemberType().hasRelation(referringArrayField, skibobSchema));
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Field value is not an array.*")
    public void instanceValidationShallFailForNonArrayFields() throws Exception {
        SchemaInstanceField arrayOfStrings = InstanceFieldType.LIST.fieldFactory(
                new SchemaDescription("someSchemaNS", singletonMap(NAME_FIELD, "skibob"), null),
                ImmutableMap.of(MEMBER_FIELD_NAME_FIELD, "strings", LIST_TARGET_FIELD, InstanceFieldType.STRING.name()));
        arrayOfStrings.validateInstance("any string");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Field value is not an array.*")
    public void instanceReferenceValidationShallFailForNonArrayFields() throws Exception {
        SchemaInstanceField arrayOfStrings = InstanceFieldType.LIST.fieldFactory(
                new SchemaDescription("someSchemaNS", singletonMap(NAME_FIELD, "skibob"), null),
                ImmutableMap.of(MEMBER_FIELD_NAME_FIELD, "strings", LIST_TARGET_FIELD, InstanceFieldType.STRING.name()));
        arrayOfStrings.hasReferencesToInstanceID("any string", null, null);
    }

}
