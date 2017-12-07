package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import java.util.Map;

import static org.testng.Assert.*;

@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class DepthCheckTest extends BaseBeanTest {

    private static final String SCHEMA_NAME = "Depth_Check_First";

    @Autowired
    private GraphQLSchemaService schemaService;

    @Autowired
    private GraphQLInstanceService instanceService;

    private final String[] depthSchemas = new String[5];
    private final String depthInstanceQuery;



    public DepthCheckTest() {
        for (int idx = 0; idx < depthSchemas.length; idx++) {
            depthSchemas[idx] = loadFromFile("graphql/depth/depth_check_" + (idx + 1) + ".txt");

        }
        depthInstanceQuery = loadFromFile("graphql/depth/instance_query.txt");
    }

    public void happyCase() {
        Map<String, Object> variableMap = buildVariableMap(INSTANCE_NAME_SPACE, SCHEMA_NAME);
        SchemaWriteAccess access = new SchemaWriteAccess();
        GraphQLResult result = instanceService.executeQuery(depthInstanceQuery, variableMap, access,
                depthSchemas.length -1);  // -1 => the last schema has 0 depth
        assertTrue(result.isSuccessful());
    }

    public void notEnoughDepth() {
        Map<String, Object> variableMap = buildVariableMap(INSTANCE_NAME_SPACE, SCHEMA_NAME);
        SchemaWriteAccess access = new SchemaWriteAccess();
        GraphQLResult result = instanceService.executeQuery(depthInstanceQuery, variableMap, access,
                depthSchemas.length -2);
        assertFalse(result.isSuccessful());
    }

    @BeforeClass
    private void loadSchemaAndInstance() {
        SchemaWriteAccess access = new SchemaWriteAccess();
        access.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);
        for (int idx = depthSchemas.length - 1; idx >= 0; idx--) {
            GraphQLResult result = schemaService.executeQuery(depthSchemas[idx], buildSchemaVariableMap(), access);
            assertTrue(result.isSuccessful());
        }
    }

    @AfterClass
    private void clearMemory() {
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
