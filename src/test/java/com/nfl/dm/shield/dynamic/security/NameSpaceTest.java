package com.nfl.dm.shield.dynamic.security;

import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.service.GraphQLInstanceService;
import com.nfl.dm.shield.dynamic.service.GraphQLResult;
import com.nfl.dm.shield.dynamic.service.GraphQLSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class NameSpaceTest extends BaseBeanTest {

    private static final String PAINT_SCHEMA_NAME = "Paint";
    private static final String MUTATOR_NAME_SPACE = "MNS";

    @Autowired
    private GraphQLSchemaService graphQLSchemaService;

    @Autowired
    private GraphQLInstanceService instanceService;

    private final String paintSchema;
    private final String paintValidInstance;
    private final String minimalInstance;

    public NameSpaceTest() {
        paintSchema = loadFromFile("graphql/add_paint.txt");
        paintValidInstance = loadFromFile("graphql/add_paint_red_instance.txt");
        minimalInstance = loadFromFile("graphql/minimal_instance.txt");
    }

    public void isolation() {
        loadInstance();
        Map<String, Object> variableMap = buildVariableMap("DifferentNameSpace", PAINT_SCHEMA_NAME);
        GraphQLResult result = instanceService.executeQuery(
                minimalInstance, variableMap, new SchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);

        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{viewer={instances={edges=[]}}}");
    }

    private void loadInstance() {
        SchemaWriteAccess instancePerm = new SchemaWriteAccess();
        instancePerm.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);

        Map<String, Object> variableMap = buildVariableMap(MUTATOR_NAME_SPACE, PAINT_SCHEMA_NAME);
        GraphQLResult result = graphQLSchemaService.executeQuery(paintSchema, variableMap, instancePerm);
        assertTrue(result.isSuccessful());

        instancePerm.addPermission(MUTATOR_NAME_SPACE, SchemaWriteAccess.INSTANCE_MODIFY);
        result = instanceService.executeQuery(
                paintValidInstance, variableMap, instancePerm, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{upsertSchemaInstance={id=red, color=Red}}");
    }

    /*
    has Instance check with multiple name spaces.
     */

    @AfterMethod
    private void cleanup() {
        inMemorySchemaRepository.clearForInstanceTesting();
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
