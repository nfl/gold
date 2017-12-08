package com.nfl.dm.shield.dynamic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.domain.instance.OrderBy;
import com.nfl.dm.shield.dynamic.domain.instance.OrderByDirection;
import graphql.schema.CoercingParseValueException;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class OrderByTest extends BaseBeanTest {

    private static final String ORDERED_TYPE_NAME = "ordered";
    private static final int INSTANCE_COUNT = 10;

    private final String addSchema;
    private final String addInstance;
    private final String findInstances;

    @Autowired
    private GraphQLInstanceService instanceService;
    @Autowired
    private ObjectMapper objectMapper;

    protected OrderByTest() {
        addSchema = loadFromFile("/graphql/add_ordered.txt");
        addInstance = loadFromFile("/graphql/add_ordered_instance.txt");
        findInstances = loadFromFile("/graphql/find_ordered_instances.txt");
    }

    @BeforeClass
    public void createALotOfInstancesWithDifferentUpdateDate() throws Exception {
        GraphQLResult result = executeSchemaQuery(addSchema);
        assertTrue(result.isSuccessful());

        for (int i = 0; i < INSTANCE_COUNT; i++) {
            result = instanceService.executeQuery(
                    addInstance, buildVariableMap(ORDERED_TYPE_NAME), buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
            assertTrue(result.isSuccessful());
            /* Sleep 1 millisecond, just to have a slight spread of 'updateDate'. */
            TimeUnit.MILLISECONDS.sleep(1);
        }

        /* Prerequisites for ordering tests: 'updateDate' has unique values and is of type Long. */
        result = instanceService.executeQuery(
                findInstances, buildVariableMap(ORDERED_TYPE_NAME), buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful(), result.getErrors().toString());
        String json = objectMapper.writeValueAsString(result.getData());
        Object[] dates = JsonPath.<JSONArray>read(json, "$.viewer.instances.edges[*].node.updateDate").toArray();
        assertTrue(dates[0] instanceof Long, "Expected updateDate-s to be instances of Long");
        assertEquals(new HashSet<>(asList(dates)).size(), INSTANCE_COUNT, "All updateDate-s should have been different.");
    }

    @Test
    public void testOrderByDate() throws Exception {
        Map<String, Object> vars = buildVariableMap(ORDERED_TYPE_NAME);

        /* Check orderBy with implicit ASC/DESC direction. */
        vars.put("orderBy", OrderBy.UPDATE_DATE.getFieldName());

        List<Long> updateDates = fetchUpdateDates(vars);

        for (int i = 0; i < INSTANCE_COUNT - 1; i++) {
            assertTrue(updateDates.get(i) < updateDates.get(i + 1));
        }

        /* Check orderBy with explicit default direction. */
        vars.put("orderBy", OrderBy.UPDATE_DATE.getFieldName());
        vars.put("orderByDirection", OrderByDirection.ASC);

        updateDates = fetchUpdateDates(vars);

        for (int i = 0; i < INSTANCE_COUNT - 1; i++) {
            assertTrue(updateDates.get(i) < updateDates.get(i + 1));
        }

        /* Check orderBy with explicit ASC direction */
        vars.put("orderBy", OrderBy.UPDATE_DATE.getFieldName());
        vars.put("orderByDirection", OrderByDirection.ASC);

        updateDates = fetchUpdateDates(vars);

        for (int i = 0; i < INSTANCE_COUNT - 1; i++) {
            assertTrue(updateDates.get(i) < updateDates.get(i + 1));
        }

        /* Check orderBy with explicit DESC direction */
        vars.put("orderBy", OrderBy.UPDATE_DATE.getFieldName());
        vars.put("orderByDirection", OrderByDirection.DESC);

        updateDates = fetchUpdateDates(vars);

        for (int i = 0; i < INSTANCE_COUNT - 1; i++) {
            assertTrue(updateDates.get(i) > updateDates.get(i + 1));
        }

    }

    @Test(expectedExceptions = CoercingParseValueException.class, expectedExceptionsMessageRegExp = "Invalid input for Enum 'OrderBy'.*")
    public void testOrderByInvalidEnumValue() throws Exception {
        Map<String, Object> vars = buildVariableMap(ORDERED_TYPE_NAME);

        vars.put("orderBy", "");

        fetchUpdateDates(vars);
    }

    @Test(expectedExceptions = CoercingParseValueException.class, expectedExceptionsMessageRegExp = "Invalid input for Enum 'OrderByDirection'.*")
    public void testOrderByDirectionInvalidEnumValue() throws Exception {
        Map<String, Object> vars = buildVariableMap(ORDERED_TYPE_NAME);

        vars.put("orderByDirection", "bad value");

        fetchUpdateDates(vars);
    }

    @Test
    public void testOrderByReturnsResultsInStableOrder() throws Exception {
        Map<String, Object> vars = buildVariableMap(ORDERED_TYPE_NAME);
        vars.put("orderBy", OrderBy.UPDATE_DATE.getFieldName());
        vars.put("orderByDirection", OrderByDirection.DESC);

        List<Long> updateDates1 = fetchUpdateDates(vars);
        List<Long> updateDates2 = fetchUpdateDates(vars);

        assertEquals(updateDates1, updateDates2);
    }

    private List<Long> fetchUpdateDates(Map<String, Object> vars) throws JsonProcessingException {
        GraphQLResult result = instanceService.executeQuery(findInstances, vars, buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        String json = objectMapper.writeValueAsString(result.getData());
        return JsonPath.<JSONArray>read(json, "$.viewer.instances.edges[*].node.updateDate").stream()
                .map(Long.class::cast)
                .collect(Collectors.toList());
    }

    @AfterClass
    private void clearMemory() {
        inMemorySchemaRepository.clearForInstanceTesting();
        inMemorySchemaRepository.clearForSchemaTesting();
    }

}