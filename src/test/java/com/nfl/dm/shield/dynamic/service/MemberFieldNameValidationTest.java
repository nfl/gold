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
public class MemberFieldNameValidationTest extends BaseBeanTest {
    private final String withEmptyMemberFieldName;
    private final String withEmptyMemberFieldNameResult;
    private final String withNullMemberFileName;
    private final String withNullMemberFileNameResult;
    private final String withPaddingSpacesOnMemberFieldName;
    private final String withPaddingSpacesOnMemberFieldNameResponse;

    @Autowired
    private GraphQLSchemaService schemaService;

    public MemberFieldNameValidationTest() {
        withEmptyMemberFieldName = loadFromFile("graphql/validation/add_schema_with_empty_member_field_name.txt");
        withEmptyMemberFieldNameResult = loadFromFile("graphql/validation/add_schema_with_empty_member_field_name_result.txt");
        withNullMemberFileName = loadFromFile("graphql/validation/add_schema_with_null_member_field_name.txt");
        withNullMemberFileNameResult = loadFromFile("graphql/validation/add_schema_with_null_member_field_name_result.txt");
        withPaddingSpacesOnMemberFieldName = loadFromFile("graphql/validation/add_schema_with_padding_spaces_on_member_field_name.txt");
        withPaddingSpacesOnMemberFieldNameResponse = loadFromFile("graphql/validation/add_schema_with_padding_spaces_on_member_field_name_response.txt");
    }

    public void shouldNotCreateSchemaWithEmptyMemberFieldName() {
        GraphQLResult result = schemaService.executeQuery(withEmptyMemberFieldName,
                buildSchemaVariableMap(), buildSchemaWriteAccess());

        assertFalse(result.isSuccessful());
        assertEquals(result.getErrors().get(0).getMessage(), withEmptyMemberFieldNameResult);
    }

    public void shouldNotCreateSchemaWithNullMemberFieldName() {
        GraphQLResult result = schemaService.executeQuery(withNullMemberFileName,
                buildSchemaVariableMap(), buildSchemaWriteAccess());

        assertFalse(result.isSuccessful());
        assertEquals(result.getErrors().get(0).getMessage(), withNullMemberFileNameResult);
    }

    public void shouldNotCreateSchemaWithPaddingSpacesOnMemberFieldName() {
        GraphQLResult result = schemaService.executeQuery(withPaddingSpacesOnMemberFieldName,
                buildSchemaVariableMap(), buildSchemaWriteAccess());

        assertFalse(result.isSuccessful());
        assertEquals(result.getErrors().get(0).getMessage(), withPaddingSpacesOnMemberFieldNameResponse);
    }

    @AfterClass
    void clearSchemaRepo() {
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
