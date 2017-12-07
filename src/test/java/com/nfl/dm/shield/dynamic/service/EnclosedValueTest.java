package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import com.nfl.dm.shield.dynamic.BaseBeanTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.*;

@SuppressWarnings("unused")
@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class EnclosedValueTest extends BaseBeanTest {


    @Autowired
    private GraphQLSchemaService schemaService;

    @Autowired
    private GraphQLInstanceService instanceService;

    private final String addValueEncloser;
    private final String addValueEncloserResults;
    private final String addValueEncloserInstance;
    private final String addValueEncloserInstanceResults;


    public EnclosedValueTest() {
        addValueEncloser = loadFromFile("graphql/enclosed_value.txt");
        addValueEncloserResults = loadFromFile("graphql/enclosed_value_results.txt");
        addValueEncloserInstance = loadFromFile("graphql/enclosed_value_instance.txt");
        addValueEncloserInstanceResults = loadFromFile("graphql/enclosed_value_instance_results.txt");
    }

    public void enclosedValues() {
        assertFalse(addValueEncloser.isEmpty());
        GraphQLResult result = schemaService.executeQuery(addValueEncloser, buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), addValueEncloserResults);

        assertFalse(addValueEncloserInstance.isEmpty());
        Map<String, Object> variableMap = buildVariableMap(INSTANCE_NAME_SPACE, "ValueEncloser");
        result = instanceService.executeQuery(
                addValueEncloserInstance, variableMap, buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), addValueEncloserInstanceResults);
    }

    @AfterMethod
    void clearRepo() {
        inMemorySchemaRepository.clearForInstanceTesting();
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
