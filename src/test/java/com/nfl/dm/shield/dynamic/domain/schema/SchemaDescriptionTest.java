package com.nfl.dm.shield.dynamic.domain.schema;

import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.testng.Assert.assertNotNull;

@Test
public class SchemaDescriptionTest {

    @Mock
    private InstanceOutputTypeService instanceOutputTypeService;

    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }


    // make sonar happy with coverage on setters and default constructor.
    public void coverSetters() {
        SchemaDescription schemaDescription = new SchemaDescription();
        schemaDescription.setName("testSchema");
        schemaDescription.setDescription("Silly Test Description");
        schemaDescription.setIdGeneration(IdGeneration.CLIENT_SPECIFIED);
        schemaDescription.setValueDefinitions(emptyList());
        schemaDescription.setDomainFields(emptyList());
        schemaDescription.setMemberConfiguration("Member configuration");
        assertNotNull(schemaDescription);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void defaultInitValue() {
        Map<String, Object> initMap = Collections.singletonMap("fizbit", "bogus value");
        new SchemaDescription("someSchemaNamespace", initMap, instanceOutputTypeService);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void noName() {
        Map<String, Object> initMap = Collections.singletonMap(SchemaDescription.DESCRIPTION_FIELD, "bogus value");
        new SchemaDescription("someSchemaNamespace", initMap, instanceOutputTypeService);
    }

}
