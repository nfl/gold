package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import com.nfl.dm.shield.dynamic.BaseBeanTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

@Test
@ContextConfiguration(classes = ApplicationTestConfig.class)
public class ReservedKeywordValidationTest extends BaseBeanTest {

    private final String userReservedKeywordErrorResult;
    private final String withReferencedBy;
    private final String withIdFieldName;

    @Autowired
    private GraphQLSchemaService schemaService;

    public ReservedKeywordValidationTest() {
        withIdFieldName = loadFromFile("graphql/validation/add_schema_with_reserved_id_field.txt");
        withReferencedBy = loadFromFile("graphql/validation/add_schema_with_reserved_referencedBy_field.txt");
        userReservedKeywordErrorResult = loadFromFile("graphql/validation/add_schema_with_reserved_field_name_result.txt");
    }

    public void shouldNotCreateSchemaWithDomainFieldNameId() throws Exception {
        GraphQLResult result = schemaService.executeQuery(withIdFieldName,
                buildSchemaVariableMap(), buildSchemaWriteAccess());

        assertFalse(result.isSuccessful());
        assertEquals(result.getErrors().get(0).getMessage(), userReservedKeywordErrorResult);
    }

    public void shouldNotCreateSchemaWithDomainFieldNameReferencedByKeyword() throws Exception {
        GraphQLResult result = schemaService.executeQuery(withReferencedBy,
                buildSchemaVariableMap(), buildSchemaWriteAccess());

        assertFalse(result.isSuccessful());
        assertEquals(result.getErrors().get(0).getMessage(), userReservedKeywordErrorResult);
    }

    @AfterClass
    void clearSchemaRepo() {
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
