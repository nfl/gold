package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import com.nfl.dm.shield.dynamic.InstanceBaseBeanTest;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.*;

@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class MutualReferenceTest  extends InstanceBaseBeanTest {

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private GraphQLSchemaService schemaService;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private GraphQLInstanceService instanceService;

    private final String mutualRef;
    private final String minimalInstance;

    public MutualReferenceTest() {
        mutualRef = loadFromFile("graphql/add_ski_bob_mutual_reference.txt");
        minimalInstance = loadFromFile("graphql/minimal_instance.txt");
    }

    /*
     * Duplicates PLATFORM-826 with stack overflow exception.
     */
    public void mutualReferenceSchemas() {
        SchemaWriteAccess writeAccess = new SchemaWriteAccess();
        writeAccess.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);
        writeAccess.addPermission(INSTANCE_NAME_SPACE, SchemaWriteAccess.INSTANCE_MODIFY);

        GraphQLResult result = schemaService.executeQuery(mutualRef, buildSchemaVariableMap(), writeAccess);
        assertTrue(result.isSuccessful());
        Map<String, Object> variableMap = buildVariableMap(INSTANCE_NAME_SPACE, "skibob");
        result = instanceService.executeQuery(
                minimalInstance, variableMap, writeAccess, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
    }
}
