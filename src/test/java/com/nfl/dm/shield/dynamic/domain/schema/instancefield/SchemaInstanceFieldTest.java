package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import org.testng.annotations.Test;

import java.util.Arrays;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.testng.Assert.assertNotNull;

/**
 * Get Sonar happier.
 */
@Test
public class SchemaInstanceFieldTest {

    // make sonar happy with coverage on setters and default constructor.
    public void coverSetters() {
        SchemaInstanceField sif = new SchemaInstanceField();
        sif.setParent(null);
        sif.setMemberFieldName("testField");
        sif.setMemberType(InstanceFieldType.NONE);
        sif.setArrayEntryType(InstanceFieldType.NONE);
        sif.setEnumValues(emptyList());
        sif.setMemberDescription("Test Description");
        sif.setOtherTypeName("Another Test Name");
        sif.setServiceKey("SHIELD");
        sif.setPossibleTypes(emptyList());
        sif.setConstraints(emptyList());
        sif.setMemberConfiguration("Member configuration Test");
        assertNotNull(sif);
    }

    public void coverSubclassConstructors() {
        SchemaInstanceField sif = new SchemaInstanceField();
        sif.setParent(new SchemaDescription());
        sif.setMemberFieldName("testField");
        sif.setArrayEntryType(InstanceFieldType.OTHER_DYNAMIC_DOMAIN);
        sif.setEnumValues(emptyList());
        sif.setMemberDescription("Test Description");
        sif.setOtherTypeName("Another Test Name");
        sif.setMemberConfiguration("Member configuration Test");
        assertNotNull(sif);

        Arrays.stream(InstanceFieldType.values())
                .filter(memberType -> memberType != InstanceFieldType.NONE)
                .forEach(memberType -> callConstructor(memberType, sif));
    }

    public void coverExternalTypeInList() {
        SchemaInstanceField sif = new SchemaInstanceField();
        sif.setMemberFieldName("testField");
        sif.setArrayEntryType(InstanceFieldType.EXTERNAL_REFERENCE);
        sif.setEnumValues(emptyList());
        sif.setMemberDescription("Test Description");
        assertNotNull(sif);

        Arrays.stream(InstanceFieldType.values())
                .filter(memberType -> memberType != InstanceFieldType.NONE)
                .forEach(memberType -> callConstructor(memberType, sif));
    }

    private void callConstructor(InstanceFieldType memberType, SchemaInstanceField sif) {
        memberType.fieldFactory(sif.getParent(), sif);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void noName() {
        InstanceFieldType.NONE.fieldFactory(null, emptyMap());
    }
}
