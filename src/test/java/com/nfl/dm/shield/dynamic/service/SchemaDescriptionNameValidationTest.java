package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.BaseBeanTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Test
public class SchemaDescriptionNameValidationTest extends BaseBeanTest {
    private final String withNotValidSchemaDescriptionName;
    private final String withNotValidSchemaDescriptionNameResult;
    private final String withValidSchemaDescriptionName;

    @Autowired
    private GraphQLSchemaService schemaService;

    public SchemaDescriptionNameValidationTest() {
        withNotValidSchemaDescriptionName = loadFromFile("graphql/validation/add_schema_with_not_valid_schema_name.txt");
        withNotValidSchemaDescriptionNameResult = loadFromFile("graphql/validation/add_schema_with_not_valid_schema_name_result.txt");
        withValidSchemaDescriptionName = loadFromFile("graphql/validation/add_schema_with_valid_schema_name.txt");
    }

    public void shouldNotCreateSchemaWithInvalidSchemaDescriptionName() {
        GraphQLResult result = schemaService.executeQuery(withNotValidSchemaDescriptionName,
                buildSchemaVariableMap(), buildSchemaWriteAccess());

        assertFalse(result.isSuccessful());
        assertEquals(result.getErrors().get(0).getMessage(), withNotValidSchemaDescriptionNameResult);
    }

    public void shouldCreateSchemaWithValidSchemaDescriptionName() {
        GraphQLResult result = schemaService.executeQuery(withValidSchemaDescriptionName,
                buildSchemaVariableMap(), buildSchemaWriteAccess());

        assertTrue(result.isSuccessful());
    }

    @AfterClass
    void clearSchemaRepo() {
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
