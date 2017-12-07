package com.nfl.dm.shield.dynamic.security;

import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.exception.UnauthorizedException;
import com.nfl.dm.shield.dynamic.service.GraphQLInstanceService;
import com.nfl.dm.shield.dynamic.service.GraphQLResult;
import com.nfl.dm.shield.dynamic.service.GraphQLSchemaService;
import graphql.ExceptionWhileDataFetching;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class InstanceMutatorPermissionsTest extends BaseBeanTest {

    private static final String PAINT_SCHEMA_NAME = "Paint";
    private static final String MUTATOR_NAME_SPACE = "MNS";

    @Autowired
    private GraphQLSchemaService graphQLSchemaService;

    @Autowired
    private GraphQLInstanceService instanceService;

    private final String paintSchema;
    private final String paintValidInstance;

    public InstanceMutatorPermissionsTest() {
        paintSchema = loadFromFile("graphql/add_paint.txt");
        paintValidInstance = loadFromFile("graphql/add_paint_red_instance.txt");
    }

    public void schemaPerm() {
        GraphQLResult result = buildSchema(new SchemaWriteAccess());
        assertFalse(result.isSuccessful());
        ExceptionWhileDataFetching fetchException = (ExceptionWhileDataFetching) result.getErrors().get(0);
        //noinspection ThrowableResultOfMethodCallIgnored
        assertTrue(fetchException.getException() instanceof SecurityException);
    }

    public void instancePerm() {
        SchemaWriteAccess instancePerm = new SchemaWriteAccess();
        instancePerm.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);

        Map<String, Object> variableMap = buildVariableMap(MUTATOR_NAME_SPACE, PAINT_SCHEMA_NAME);
        GraphQLResult result = graphQLSchemaService.executeQuery(paintSchema, variableMap, instancePerm);
        assertTrue(result.isSuccessful());

        assertFalse(paintValidInstance.isEmpty());
        result = instanceService.executeQuery(paintValidInstance, variableMap, instancePerm, DEFAULT_MAX_RECURSE_DEPTH);
        assertFalse(result.isSuccessful());

        instancePerm.addPermission(MUTATOR_NAME_SPACE, SchemaWriteAccess.INSTANCE_TRUNCATE);
        result = instanceService.executeQuery(paintValidInstance, variableMap, instancePerm, DEFAULT_MAX_RECURSE_DEPTH);
        assertFalse(result.isSuccessful());

        instancePerm.addPermission(MUTATOR_NAME_SPACE, SchemaWriteAccess.INSTANCE_MODIFY);
        result = instanceService.executeQuery(paintValidInstance, variableMap, instancePerm, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{upsertSchemaInstance={id=red, color=Red}}");

        final String removePaintInstance = "mutation{removeInstance(id: \"red\"){id}}";
        result = instanceService.executeQuery(removePaintInstance,
                variableMap, instancePerm, DEFAULT_MAX_RECURSE_DEPTH);
        assertFalse(result.isSuccessful());

        instancePerm.addPermission(MUTATOR_NAME_SPACE, SchemaWriteAccess.INSTANCE_DELETE);
        result = instanceService.executeQuery(removePaintInstance, variableMap, instancePerm,
                DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{removeInstance={id=red}}");
    }

    public void truncatePerm() {
        SchemaWriteAccess instancePerm = new SchemaWriteAccess();
        instancePerm.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);

        Map<String, Object> variableMap = buildVariableMap(MUTATOR_NAME_SPACE, PAINT_SCHEMA_NAME);
        GraphQLResult result = graphQLSchemaService.executeQuery(paintSchema, variableMap, instancePerm);
        assertTrue(result.isSuccessful());

        instancePerm.addPermission(MUTATOR_NAME_SPACE, SchemaWriteAccess.INSTANCE_MODIFY);
        result = instanceService.executeQuery(paintValidInstance, variableMap, instancePerm, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{upsertSchemaInstance={id=red, color=Red}}");

        final String removeAll = "mutation  { removeAllInstances { count }}";
        result = instanceService.executeQuery(removeAll, variableMap, instancePerm, DEFAULT_MAX_RECURSE_DEPTH);
        assertFalse(result.isSuccessful());

        instancePerm.addPermission(MUTATOR_NAME_SPACE, SchemaWriteAccess.INSTANCE_TRUNCATE);
        result = instanceService.executeQuery(removeAll, variableMap, instancePerm, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{removeAllInstances={count=1}}");
    }

    @Test
    public void removeExistingPaintInstanceWithoutPermissions() throws Exception {
        SchemaWriteAccess perms = new SchemaWriteAccess();
        perms.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);
        perms.addPermission(MUTATOR_NAME_SPACE, SchemaWriteAccess.INSTANCE_MODIFY);

        GraphQLResult result;

        result = buildSchema(perms);
        assertTrue(result.isSuccessful());

        result = upsertPaintInstance(perms);
        assertTrue(result.isSuccessful());

        result = removePaintInstance(perms);

        assertFalse(result.isSuccessful());
        ExceptionWhileDataFetching fetchException = (ExceptionWhileDataFetching) result.getErrors().get(0);
        assertTrue(fetchException.getException() instanceof UnauthorizedException);
        assertThat(fetchException.getMessage()).contains("removeInstance");
    }

    @Test
    public void upsertPaintInstanceWithoutPermissions() throws Exception {
        SchemaWriteAccess perms = new SchemaWriteAccess();
        perms.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);

        GraphQLResult result;

        result = buildSchema(perms);
        assertTrue(result.isSuccessful());

        result = upsertPaintInstance(perms);

        assertFalse(result.isSuccessful());
        ExceptionWhileDataFetching fetchException = (ExceptionWhileDataFetching) result.getErrors().get(0);
        assertTrue(fetchException.getException() instanceof UnauthorizedException);
        assertThat(fetchException.getMessage()).contains("upsertSchemaInstance");
    }

    @Test
    public void removeAllPaintInstancesWithoutPetitions() throws Exception {
        SchemaWriteAccess perms = new SchemaWriteAccess();
        perms.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);
        perms.addPermission(MUTATOR_NAME_SPACE, SchemaWriteAccess.INSTANCE_MODIFY);

        GraphQLResult result;

        result = buildSchema(perms);
        assertTrue(result.isSuccessful());

        result = upsertPaintInstance(perms);
        assertTrue(result.isSuccessful());

        result = removeAllPaintInstances(perms);

        assertFalse(result.isSuccessful());
        ExceptionWhileDataFetching fetchException = (ExceptionWhileDataFetching) result.getErrors().get(0);
        assertTrue(fetchException.getException() instanceof UnauthorizedException);
        assertThat(fetchException.getMessage()).contains("removeAllInstances");
    }

    private GraphQLResult upsertPaintInstance(SchemaWriteAccess perms) {
        assertFalse(paintValidInstance.isEmpty());
        Map<String, Object> variableMap = buildVariableMap(MUTATOR_NAME_SPACE, PAINT_SCHEMA_NAME);
        return instanceService.executeQuery(paintValidInstance, variableMap, perms, DEFAULT_MAX_RECURSE_DEPTH);
    }

    private GraphQLResult removePaintInstance(SchemaWriteAccess perms) {
        final String removePaintInstance = "mutation{removeInstance(id: \"red\"){id}}";

        Map<String, Object> variableMap = buildVariableMap(MUTATOR_NAME_SPACE, PAINT_SCHEMA_NAME);
        return instanceService.executeQuery(removePaintInstance, variableMap, perms, DEFAULT_MAX_RECURSE_DEPTH);
    }

    private GraphQLResult removeAllPaintInstances(SchemaWriteAccess perms) {
        final String removeAll = "mutation  { removeAllInstances { count }}";

        Map<String, Object> variableMap = buildVariableMap(MUTATOR_NAME_SPACE, PAINT_SCHEMA_NAME);
        return instanceService.executeQuery(removeAll, variableMap, perms, DEFAULT_MAX_RECURSE_DEPTH);
    }

    private GraphQLResult buildSchema(SchemaWriteAccess writeAccess) {
        assertFalse(paintSchema.isEmpty());
        Map<String, Object> variableMap = buildVariableMap(MUTATOR_NAME_SPACE, PAINT_SCHEMA_NAME);
        return graphQLSchemaService.executeQuery(paintSchema, variableMap, writeAccess);
    }

    @AfterMethod
    private void cleanup() {
        inMemorySchemaRepository.clearForInstanceTesting();
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
