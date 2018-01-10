package com.nfl.dm.shield.dynamic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

@Test
public class PaginationTest extends BaseBeanTest {

    private static final String PAGED_TYPE_NAME = "paged";
    private static final int SCHEMA_COUNT = 5;
    private static final int INSTANCE_COUNT = 5;

    private final String addSchema;
    private final String addInstance;
    private final String findInstances;
    private final String findSchemas;

    @Autowired
    private GraphQLInstanceService instanceService;
    @Autowired
    private ObjectMapper objectMapper;

    protected PaginationTest() {
        addSchema = loadFromFile("/graphql/pagination/add_paged.txt");
        addInstance = loadFromFile("/graphql/pagination/add_paged_instance.txt");
        findInstances = loadFromFile("/graphql/pagination/find_paged_instances.txt");
        findSchemas = loadFromFile("/graphql/pagination/find_paged_schemas.txt");
    }

    @BeforeClass
    public void createALotOfSchemasAndInstances() {
        SchemaWriteAccess mutableAccess = buildSchemaWriteAccess();
        for (int schemaIndex = 0; schemaIndex < SCHEMA_COUNT; schemaIndex++) {
            Map<String, Object> schemaVars = buildSchemaVariableMap();
            String schemaName = PAGED_TYPE_NAME + schemaIndex;
            schemaVars.put("name", schemaName);
            GraphQLResult result = schemaService.executeQuery(addSchema, schemaVars, mutableAccess);
            assertTrue(result.isSuccessful());

            for (int instanceIndex = 0; instanceIndex < INSTANCE_COUNT; instanceIndex++) {
                Map<String, Object> vars = buildVariableMap(schemaName);
                vars.put("id", "instance # " + instanceIndex);
                result = instanceService.executeQuery(addInstance, vars, buildSchemaWriteAccess(),
                        DEFAULT_MAX_RECURSE_DEPTH);
                assertTrue(result.isSuccessful());
            }
        }
    }

    @Test
    public void testSchemaPagination() throws Exception {
        GraphQLResult result = schemaService.executeQuery(findSchemas, buildSchemaVariableMap(), null);
        assertTrue(result.isSuccessful());

        String json = objectMapper.writeValueAsString(result.getData());

        Object[] names = JsonPath.<JSONArray>read(json, "$.viewer.schemas.edges[*].node.name").toArray();
        Object[] cursors = JsonPath.<JSONArray>read(json, "$.viewer.schemas.edges[*].cursor").toArray();

        /* Sanity check - in essence, no paging */
        assertEqualsNoOrder(names, new String[]{"paged0", "paged1", "paged2", "paged3", "paged4" });
        assertEquals(JsonPath.read(json, "$.viewer.schemas.totalCount"), (Integer) SCHEMA_COUNT);
        assertEquals(JsonPath.read(json, "$.viewer.schemas.pageInfo.hasNextPage"), Boolean.FALSE);
        assertEquals(JsonPath.read(json, "$.viewer.schemas.pageInfo.hasPreviousPage"), Boolean.FALSE);

        /* Moving forward with pages. */
        checkPageOfSchemas(null, cursors[cursors.length - 1], 2, null, null,
                0, false, false, SCHEMA_COUNT);
        checkPageOfSchemas(null, cursors[cursors.length - 2], 2, null, null,
                1, false, false, SCHEMA_COUNT);
        checkPageOfSchemas(null, cursors[cursors.length - 3], 2, null, null,
                2, false, false, SCHEMA_COUNT);
        checkPageOfSchemas(null, cursors[cursors.length - 4], 2, null, null,
                2, true, false, SCHEMA_COUNT);

        /* Moving backwards with pages. */
        checkPageOfSchemas(null, null, null, cursors[0], 2, 0,
                false, false, SCHEMA_COUNT);
        checkPageOfSchemas(null, null, null, cursors[1], 2, 1,
                false, false, SCHEMA_COUNT);
        checkPageOfSchemas(null, null, null, cursors[2], 2, 2,
                false, false, SCHEMA_COUNT);
        checkPageOfSchemas(null, null, null, cursors[3], 2, 2,
                false, true, SCHEMA_COUNT);

        /* Check that pagination and totalCount take filters into account. */
        List<String> schemasToFind = asList("paged0", "paged1", "paged2");
        checkPageOfSchemas(schemasToFind, cursors[0], 1, null, null, 1,
                true, false, schemasToFind.size());
        checkPageOfSchemas(schemasToFind, cursors[0], 500, null, null, 2,
                false, false, schemasToFind.size());
    }

    private void checkPageOfSchemas(
            // query params
            List<String> argSchemaNames, Object argAfter, Integer argFirst, Object argBefore, Object argLast,
            // assertion params
            int fetchedCount, boolean hasNext, boolean hasPrevious, int totalCount
    ) throws Exception {
        Map<String, Object> vars = buildSchemaVariableMap();
        vars.put("after", argAfter);
        vars.put("first", argFirst);
        vars.put("before", argBefore);
        vars.put("last", argLast);
        vars.put("names", argSchemaNames);

        GraphQLResult result = schemaService.executeQuery(findSchemas, vars, null);

        assertTrue(result.isSuccessful());
        String json = objectMapper.writeValueAsString(result.getData());
        assertEquals(JsonPath.<JSONArray>read(json, "$.viewer.schemas.edges[*]").toArray().length,
                fetchedCount);
        assertEquals(JsonPath.read(json, "$.viewer.schemas.totalCount"), (Integer) totalCount);
        assertEquals(JsonPath.read(json, "$.viewer.schemas.pageInfo.hasNextPage"), (Boolean) hasNext);
        assertEquals(JsonPath.read(json, "$.viewer.schemas.pageInfo.hasPreviousPage"), (Boolean) hasPrevious);
    }

    @Test
    public void testInstancePagination() throws Exception {
        GraphQLResult result = instanceService.executeQuery(
                findInstances, buildVariableMap("paged0"), buildSchemaWriteAccess(),
                DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());

        String json = objectMapper.writeValueAsString(result.getData());

        Object[] ids = JsonPath.<JSONArray>read(json, "$.viewer.instances.edges[*].node.id").toArray();
        Object[] cursors = JsonPath.<JSONArray>read(json, "$.viewer.instances.edges[*].cursor").toArray();

        /* Sanity check - in essence, no paging */
        assertEqualsNoOrder(ids, new String[]{"instance # 0", "instance # 1", "instance # 2", "instance # 3",
                "instance # 4" });
        assertEquals(JsonPath.read(json, "$.viewer.instances.totalCount"), (Integer) INSTANCE_COUNT);
        assertEquals(JsonPath.read(json, "$.viewer.instances.pageInfo.hasNextPage"), Boolean.FALSE);
        assertEquals(JsonPath.read(json, "$.viewer.instances.pageInfo.hasPreviousPage"), Boolean.FALSE);

        /* Moving forward with pages. */
        checkPageOfInstances(null, cursors[cursors.length - 1], 2, null, null,
                0, false, false, INSTANCE_COUNT);
        checkPageOfInstances(null, cursors[cursors.length - 2], 2, null, null,
                1, false, false, INSTANCE_COUNT);
        checkPageOfInstances(null, cursors[cursors.length - 3], 2, null, null,
                2, false, false, INSTANCE_COUNT);
        checkPageOfInstances(null, cursors[cursors.length - 4], 2, null, null,
                2, true, false, INSTANCE_COUNT);

        /* Moving backwards with pages. */
        checkPageOfInstances(null, null, null, cursors[0], 2, 0,
                false, false, INSTANCE_COUNT);
        checkPageOfInstances(null, null, null, cursors[1], 2, 1,
                false, false, INSTANCE_COUNT);
        checkPageOfInstances(null, null, null, cursors[2], 2, 2,
                false, false, INSTANCE_COUNT);
        checkPageOfInstances(null, null, null, cursors[3], 2, 2,
                false, true, INSTANCE_COUNT);

        /* Check that pagination and totalCount take filters into account. */
        List<String> instancesToFind = asList("instance # 0", "instance # 1", "instance # 2");
        checkPageOfInstances(instancesToFind, cursors[0], 1, null, null, 1,
                true, false, instancesToFind.size());
        checkPageOfInstances(instancesToFind, cursors[0], 500, null, null, 2,
                false, false, instancesToFind.size());
    }

    private void checkPageOfInstances(
            // query params
            List<String> argInstanceIds, Object argAfter, Integer argFirst, Object argBefore, Object argLast,
            // assertion params
            int fetchedCount, boolean hasNext, boolean hasPrevious, int totalCount
    ) throws Exception {
        Map<String, Object> vars = buildVariableMap("paged0");
        vars.put("after", argAfter);
        vars.put("first", argFirst);
        vars.put("before", argBefore);
        vars.put("last", argLast);
        vars.put("ids", argInstanceIds);

        GraphQLResult result =
                instanceService.executeQuery(findInstances, vars, buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);

        assertTrue(result.isSuccessful());
        String json = objectMapper.writeValueAsString(result.getData());
        assertEquals(JsonPath.<JSONArray>read(json, "$.viewer.instances.edges[*]").toArray().length,
                fetchedCount);
        assertEquals(JsonPath.read(json, "$.viewer.instances.totalCount"), (Integer) totalCount);
        assertEquals(JsonPath.read(json, "$.viewer.instances.pageInfo.hasNextPage"), (Boolean) hasNext);
        assertEquals(JsonPath.read(json, "$.viewer.instances.pageInfo.hasPreviousPage"),
                (Boolean) hasPrevious);
    }

    @AfterClass
    private void clearMemory() {
        inMemorySchemaRepository.clearForInstanceTesting();
        inMemorySchemaRepository.clearForSchemaTesting();
    }

}