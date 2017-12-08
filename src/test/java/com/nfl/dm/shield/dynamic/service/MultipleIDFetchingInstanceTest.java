package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import com.nfl.dm.shield.dynamic.InstanceBaseBeanTest;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class MultipleIDFetchingInstanceTest extends InstanceBaseBeanTest {

    @Autowired
    private GraphQLSchemaService graphQLSchemaService;

    @Autowired
    private GraphQLInstanceService instanceService;

    private final String addSkiBob;
    private final String addSkiBobInstance;
    private final String addSkiBobInstance2;
    private final String fetch;
    private final String fetchResult;


    public MultipleIDFetchingInstanceTest() {

        addSkiBob = loadFromFile("graphql/add_ski_bob.txt");
        addSkiBobInstance = loadFromFile("graphql/add_ski_bob_instance.txt");
        addSkiBobInstance2 = loadFromFile("graphql/add_ski_bob_second_instance.txt");
        fetch = loadFromFile("graphql/multiple_id_fetch.txt");
        fetchResult = loadFromFile("graphql/multiple_id_fetch_result.txt");
    }

    public void multipleIDFetching() {

        SchemaWriteAccess mutableAccess = buildSchemaWriteAccess();
        assertFalse(addSkiBob.isEmpty());
        GraphQLResult addSkiBobResult = graphQLSchemaService.executeQuery(addSkiBob, buildSchemaVariableMap(), mutableAccess);
        assertTrue(addSkiBobResult.isSuccessful());

        assertFalse(addSkiBobInstance.isEmpty());
        GraphQLResult addSkiBobInstanceResult = instanceService.executeQuery(addSkiBobInstance,
                buildVariableMap("skibob"), buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addSkiBobInstanceResult.isSuccessful());

        assertFalse(addSkiBobInstance.isEmpty());
        GraphQLResult addSecondSkiBobInstanceResult = instanceService.executeQuery(addSkiBobInstance2,
                buildVariableMap("skibob"), buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addSecondSkiBobInstanceResult.isSuccessful());

        GraphQLResult result = instanceService.executeQuery(fetch,
                buildVariableMap("skibob"), buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);

        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), fetchResult);

    }
}

