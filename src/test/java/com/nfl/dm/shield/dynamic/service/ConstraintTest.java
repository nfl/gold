package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.InstanceBaseBeanTest;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Test
public class ConstraintTest extends InstanceBaseBeanTest {

    @Autowired
    private GraphQLSchemaService graphQLSchemaService;

    @Autowired
    private GraphQLInstanceService instanceService;

    private final String addSkiBob;
    private final String addSkiBobWithConstraint;
    private final String addSkiBobInstanceWithNotNullResort;
    private final String findBobQuery;
    private final String findBobQueryWithConstraint;
    private final String findBobQueryWithConstraintResult;
    private final String addSkiBobInstance;
    private final String addSkiBobInstanceWithNullResort;


    public ConstraintTest() {

        addSkiBob = loadFromFile("graphql/add_ski_bob.txt");
        addSkiBobWithConstraint = loadFromFile("graphql/constraint/add_ski_bob_with_constraint.txt");

        addSkiBobInstance = loadFromFile("graphql/add_ski_bob_instance.txt");
        addSkiBobInstanceWithNullResort = loadFromFile("graphql/constraint/add_ski_bob_instance_resort_is_null.txt");
        addSkiBobInstanceWithNotNullResort = loadFromFile("graphql/constraint/add_ski_bob_instance_resort_is_not_null.txt");

        findBobQuery  = loadFromFile("graphql/find_ski_bob.txt");
        findBobQueryWithConstraint  = loadFromFile("graphql/constraint/find_ski_bob_with_constraint.txt");
        findBobQueryWithConstraintResult = loadFromFile("graphql/constraint/find_ski_bob_with_constraint_result.txt");

    }

    private void createSkiBob(String query){
        SchemaWriteAccess mutableAccess = buildSchemaWriteAccess();
        assertFalse(addSkiBob.isEmpty());
        GraphQLResult addSkiBobResult = graphQLSchemaService.executeQuery(query, buildSchemaVariableMap(), mutableAccess);
        assertTrue(addSkiBobResult.isSuccessful());
    }

    public void createSkiBobWithConstraint_ConstraintsArrayShouldBeReturn(){
        createSkiBob(addSkiBobWithConstraint);
        GraphQLResult response =
                graphQLSchemaService.executeQuery(findBobQueryWithConstraint, buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertTrue(response.isSuccessful());
        assertEquals(response.getData().toString(), findBobQueryWithConstraintResult);
    }

    public void createSkiBobWithoutConstraintAndInstanceWithNullField_Success(){
        createSkiBob(addSkiBob);

        GraphQLResult response =
                graphQLSchemaService.executeQuery(findBobQuery, buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertTrue(response.isSuccessful());

        assertFalse(addSkiBobInstanceWithNullResort.isEmpty());
        GraphQLResult addSkiBobInstanceResult = instanceService.executeQuery(addSkiBobInstanceWithNullResort,
                buildVariableMap("skibob"), buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addSkiBobInstanceResult.isSuccessful());
    }

    public void createSkiBobWithConstraintAndInstanceWithNullField_Error(){
        createSkiBob(addSkiBobWithConstraint);

        GraphQLResult response =
                graphQLSchemaService.executeQuery(findBobQueryWithConstraint, buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertTrue(response.isSuccessful());

        assertFalse(addSkiBobInstance.isEmpty());
        GraphQLResult addSkiBobInstanceResult = instanceService.executeQuery(addSkiBobInstanceWithNullResort,
                buildVariableMap("skibob_with_constraint"), buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addSkiBobInstanceResult.getErrors() != null);
        assertTrue(addSkiBobInstanceResult.getErrors().get(0).getMessage().startsWith("Validation error of type WrongType: "));
    }

    public void createSkiBobWithConstraintAndInstanceWithNotNullField_Success(){
        createSkiBob(addSkiBobWithConstraint);

        GraphQLResult response =
                graphQLSchemaService.executeQuery(findBobQueryWithConstraint, buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertTrue(response.isSuccessful());

        assertFalse(addSkiBobInstance.isEmpty());
        GraphQLResult addSkiBobInstanceResult = instanceService.executeQuery(addSkiBobInstanceWithNotNullResort,
                buildVariableMap("skibob_with_constraint"), buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addSkiBobInstanceResult.isSuccessful());
    }
}
