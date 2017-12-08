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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class DepthCheckSelfTest extends BaseBeanTest {

    private static final String SCHEMA_NAME = "Deep_Thought";

    @Autowired
    private GraphQLSchemaService schemaService;
    @Autowired
    private GraphQLInstanceService instanceService;

    private final String depthSchema;
    private final String depthSchemaResults;
    private final String depthInstanceQuery;


    public DepthCheckSelfTest() {
        depthSchema = loadFromFile("graphql/depth_check_self.txt");
        depthSchemaResults = loadFromFile("graphql/depth_check_self_results.txt");
        depthInstanceQuery = loadFromFile("graphql/depth_check_self_instance_query.txt");
    }

    public void happyCase() {
        SchemaWriteAccess access = new SchemaWriteAccess();
        Map<String, Object> variableMap = buildVariableMap(INSTANCE_NAME_SPACE, SCHEMA_NAME);
        GraphQLResult result = instanceService.executeQuery(depthInstanceQuery, variableMap, access, 1);
        assertTrue(result.isSuccessful());
    }

    @BeforeClass
    private void loadSchemaAndInstance() {
        SchemaWriteAccess access = new SchemaWriteAccess();
        access.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);
        GraphQLResult result = schemaService.executeQuery(depthSchema, buildSchemaVariableMap(), access);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), depthSchemaResults);
    }

    @AfterClass
    private void clearMemory() {
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
