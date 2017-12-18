package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import graphql.ExceptionWhileDataFetching;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

@SuppressWarnings("unused")
@Test
public class GraphQLSchemaServiceTest extends BaseBeanTest {

    @Autowired
    private GraphQLSchemaService graphQLSchemaService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private GraphQLInstanceService instanceService;

    private final String arrayTypes;
    private final String arrayTypesExpectedResult;
    private final String badArrayTypes;
    private final String addSkiBob;
    private final String addJohnCarter;
    private final String addJohnCarterExpectedResults;
    private final String badDynamicDefinition;
    private final String removeSchemaMutation;
    private final String removeNonexistentSchemaMutation;
    private final String removeSchemaSkiBobMutation;
    private final String addJohnCarterInstance;

    public GraphQLSchemaServiceTest() {
        arrayTypes = loadFromFile("graphql/array_types.txt");
        arrayTypesExpectedResult = loadFromFile("graphql/array_types_result.txt");
        badArrayTypes = loadFromFile("graphql/bad_array_types.txt");
        addSkiBob = loadFromFile("graphql/add_ski_bob.txt");
        addJohnCarter = loadFromFile("graphql/add_john_carter.txt");
        addJohnCarterExpectedResults = loadFromFile("graphql/add_john_carter_results.txt");
        badDynamicDefinition = loadFromFile("graphql/bad_dynamic_definition.txt");
        removeSchemaMutation = loadFromFile("graphql/delete_john_carter.txt");
        removeNonexistentSchemaMutation = loadFromFile("graphql/delete_nonexistent.txt");
        removeSchemaSkiBobMutation = loadFromFile("graphql/delete_ski_bob.txt");
        addJohnCarterInstance = loadFromFile("graphql/add_john_carter_instance.txt");
    }

    public void mutateAndViewResults() {
        expectedCheck(addJohnCarter, addJohnCarterExpectedResults);
    }

    public void missingDynamicTarget() {
        assertFalse(badDynamicDefinition.isEmpty());
        GraphQLResult result = graphQLSchemaService.executeQuery(badDynamicDefinition,
                buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertFalse(result.isSuccessful());
    }

    public void arrayTypeCheck() {
        expectedCheck(arrayTypes, arrayTypesExpectedResult);
    }

    private void expectedCheck(String query, String results) {
        loadSkiBob();

        assertFalse(query.isEmpty());
        GraphQLResult result = graphQLSchemaService.executeQuery(query,
                buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), results);
    }

    public void testCreatingSchemaWithoutOtherDynamicSchema() {
        GraphQLResult result = graphQLSchemaService.executeQuery(addJohnCarter,
                buildSchemaVariableMap(), buildSchemaWriteAccess());

        assertFalse(result.isSuccessful());
        ExceptionWhileDataFetching exceptionWhileDataFetching = (ExceptionWhileDataFetching) result.getErrors().get(0);
        //noinspection ThrowableResultOfMethodCallIgnored
        assertTrue(exceptionWhileDataFetching.getException() instanceof IllegalArgumentException);
    }

    public void missingPerms() {
        String query = addJohnCarter;
        String results = addJohnCarterExpectedResults;
        loadSkiBob();

        assertFalse(query.isEmpty());
        GraphQLResult result = graphQLSchemaService.executeQuery(query, buildSchemaVariableMap(), new SchemaWriteAccess());
        assertFalse(result.isSuccessful());
    }

    private void loadSkiBob() {
        assertFalse(addSkiBob.isEmpty());
        GraphQLResult result = graphQLSchemaService.executeQuery(addSkiBob,
                buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertTrue(result.isSuccessful());
    }

    public void missingArrayType() {
        assertFalse(badArrayTypes.isEmpty());
        GraphQLResult result = graphQLSchemaService.executeQuery(badArrayTypes,
                buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertFalse(result.isSuccessful());
    }

    public void removeSchema() {
        expectedCheck(addJohnCarter, addJohnCarterExpectedResults);

        GraphQLResult result = graphQLSchemaService.executeQuery(removeSchemaMutation,
                buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{removeSchemaDefinition={name=John_Carter}}");

        /* Nonexistent schema cannot be deleted, but the operation is successful. */
        result = graphQLSchemaService.executeQuery(removeNonexistentSchemaMutation,
                buildSchemaVariableMap(), buildSchemaWriteAccess());

        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{removeSchemaDefinition=null}");
    }

    public void removeAttemptOfSchemaReferencedByAnotherSchema() {
        expectedCheck(addJohnCarter, addJohnCarterExpectedResults);

        GraphQLResult result = graphQLSchemaService.executeQuery(removeSchemaSkiBobMutation,
                buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertFalse(result.isSuccessful());
        assertThat(result.getErrors().get(0).getMessage()).contains("Schema skibob has relation with: John_Carter");
    }

    public void removeAttemptWithData() {
        expectedCheck(addJohnCarter, addJohnCarterExpectedResults);

        assertFalse(addJohnCarterInstance.isEmpty());
        Map<String, Object> variableMap = buildVariableMap(INSTANCE_NAME_SPACE, JOHN_CARTER_SCHEMA);
        GraphQLResult result = instanceService.executeQuery(
                        addJohnCarterInstance, variableMap, buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());

        result = graphQLSchemaService.executeQuery(removeSchemaMutation,
                buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertFalse(result.isSuccessful());
        inMemorySchemaRepository.clearForInstanceTesting();
    }

    public void securityChecks() {
        GraphQLResult result = graphQLSchemaService.executeQuery(addSkiBob,
                buildSchemaVariableMap(), new SchemaWriteAccess());
        assertFalse(result.isSuccessful());
    }

    public void deleteSecurityCheck() {
        GraphQLResult result = graphQLSchemaService.executeQuery(addSkiBob,
                buildSchemaVariableMap(), new SchemaWriteAccess());
        assertFalse(result.isSuccessful());
        result = graphQLSchemaService.executeQuery(removeSchemaMutation,
                buildSchemaVariableMap(), new SchemaWriteAccess());
        assertFalse(result.isSuccessful());
    }

    @AfterMethod
    protected void clearRepo() {
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
