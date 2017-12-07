package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

@SuppressWarnings("WeakerAccess")
@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class PaintSchemaTest extends BaseBeanTest {

    private static final String PAINT_SCHEMA_NAME = "Paint";

    @Autowired
    private GraphQLSchemaService graphQLSchemaService;

    @Autowired
    private GraphQLInstanceService instanceService;

    private final String paintSchema;
    private final String paintSchemaNoValues;
    private final String paintSchemaResults;
    private final String paintValidInstance;
    private final String paintBogusInstance;


    public PaintSchemaTest() {
        paintSchema = loadFromFile("graphql/add_paint.txt");
        paintSchemaNoValues = loadFromFile("graphql/add_paint_missing_values.txt");
        paintSchemaResults = loadFromFile("graphql/add_paint_results.txt");
        paintValidInstance = loadFromFile("graphql/add_paint_red_instance.txt");
        paintBogusInstance = loadFromFile("graphql/add_paint_purple_instance.txt");
    }

    public void testMissingSchemaValues() {
        assertFalse(paintSchemaNoValues.isEmpty());
        GraphQLResult result = graphQLSchemaService.executeQuery(paintSchemaNoValues,
                buildSchemaVariableMap(), buildPaintSchemaWriteAccess());
        assertFalse(result.isSuccessful());
    }

    public void testSchemaLoad() {

        assertFalse(paintSchema.isEmpty());
        GraphQLResult result = loadSchema();
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), paintSchemaResults);
    }

    private GraphQLResult loadSchema() {
        return graphQLSchemaService.executeQuery(paintSchema, buildSchemaVariableMap(), buildPaintSchemaWriteAccess());
    }

    public void testValidInstance() {
        loadSchema();
        assertFalse(paintValidInstance.isEmpty());
        Map<String, Object> variableMap = buildVariableMap(INSTANCE_NAME_SPACE, PAINT_SCHEMA_NAME);

        GraphQLResult result = instanceService.executeQuery(
                paintValidInstance, variableMap, buildPaintSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{upsertSchemaInstance={id=red, color=Red}}");
    }

    public void testBogusInstance() {
        loadSchema();
        assertFalse(paintBogusInstance.isEmpty());
        Map<String, Object> variableMap = buildVariableMap(INSTANCE_NAME_SPACE, PAINT_SCHEMA_NAME);
        GraphQLResult result = instanceService.executeQuery(
                paintBogusInstance, variableMap, buildPaintSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertFalse(result.isSuccessful());
        assertThat(result.getErrors().get(0).getMessage()).contains("WrongType");
    }

    private SchemaWriteAccess buildPaintSchemaWriteAccess() {
        SchemaWriteAccess mutableAccess = new SchemaWriteAccess();
        mutableAccess.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);
        mutableAccess.addPermission(INSTANCE_NAME_SPACE, SchemaWriteAccess.INSTANCE_MODIFY);
        return mutableAccess;
    }

    @AfterMethod
    void clearInstanceRepo() {
        inMemorySchemaRepository.clearForInstanceTesting();
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
