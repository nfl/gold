package com.nfl.dm.shield.dynamic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import com.nfl.dm.shield.dynamic.InstanceBaseBeanTest;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import graphql.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;

import static org.testng.Assert.*;

@SuppressWarnings("unused")
@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class GraphQLInstanceServiceTest extends InstanceBaseBeanTest {

    private static final Map<String, Object> VARIABLE_MAP = buildVariableMap(INSTANCE_NAME_SPACE, JOHN_CARTER_SCHEMA);

    @Autowired
    private GraphQLSchemaService schemaService;

    @Autowired
    private GraphQLInstanceService instanceService;

    private final String addJohnCarterInstance;
    private final String addJohnCarterInstanceResults;
    private final String instanceQuery;
    private final String addDynamicInstanceServerId;
    private final String deleteQuery;
    private final String addServerIdSchema;
    private final String addServerIdSchemaResults;
    private final String addOfMarsInstance;
    private final String addOfMarsBogusInstance;
    private final String addJohnCarterInstanceReferringJohnCarter;
    private final String mergeInstance;
    private final String mergeResult;
    private final String addLabeledSkiBob;
    private final String viewSkibob;
    private final String viewSkibobWithUpdateDate;
    private final String viewSkibobWithSchemaInstanceKey;
    private final String viewSkibobWithSchemaInstanceKeyResult;
    private final String viewSkiBobLabel;
    private final String viewJohnCarterInstanceReferringJohnCarter;
    private final String deleteSkiBobLabel;

    public GraphQLInstanceServiceTest() {
        addJohnCarterInstance = loadFromFile("graphql/add_john_carter_instance.txt");
        addJohnCarterInstanceResults = loadFromFile("graphql/add_john_carter_instance_results.txt");
        instanceQuery = loadFromFile("graphql/view_instance.txt");
        addDynamicInstanceServerId = loadFromFile("graphql/add_dynamic_domain_instance_no_id.txt");
        deleteQuery = loadFromFile("graphql/delete_john_carter_instance.txt");
        addServerIdSchema = loadFromFile("graphql/add_server_id_schema.txt");
        addServerIdSchemaResults = loadFromFile("graphql/add_server_id_schema_results.txt");
        addOfMarsInstance = loadFromFile("graphql/add_of_mars_instance.txt");
        addOfMarsBogusInstance = loadFromFile("graphql/add_of_mars_bogus_instance.txt");
        addJohnCarterInstanceReferringJohnCarter = loadFromFile("graphql/add_john_carter_instance_referring_john_carter.txt");
        mergeInstance = loadFromFile("graphql/add_john_carter_merge_instance.txt");
        mergeResult = loadFromFile("graphql/add_john_carter_merge_instance_result.txt");
        addLabeledSkiBob = loadFromFile("graphql/add_ski_bob_instance_label.txt");
        viewSkibob = loadFromFile("graphql/view_ski_bob_instance.txt");
        viewSkibobWithUpdateDate = loadFromFile("graphql/view_ski_bob_instance_updatedate.txt");
        viewSkiBobLabel = loadFromFile("graphql/view_ski_bob_label_instance.txt");
        viewSkibobWithSchemaInstanceKey = loadFromFile("graphql/schemaInstanceKey/view_ski_bob_instance_with_schemaInstanceKey.txt");
        viewSkibobWithSchemaInstanceKeyResult = loadFromFile("graphql/schemaInstanceKey/view_ski_bob_instance_with_schemaInstanceKey_result.txt");
        viewJohnCarterInstanceReferringJohnCarter = loadFromFile("graphql/view_john_carter_instance_referring_john_carter.txt");
        deleteSkiBobLabel = loadFromFile("graphql/delete_ski_bob_instance_label.txt");
    }

    public void notFoundSchema() {

        GraphQLResult result = instanceService.executeQuery("",
                buildVariableMap(INSTANCE_NAME_SPACE, "fizbit"), buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertFalse(result.isSuccessful());
        assertEquals(result.getErrors().get(0).getMessage(), "fizbit not found.");

        // Further code coverage report tweaking
        assertTrue(result.getErrors().get(0).getLocations().isEmpty());
        assertEquals(result.getErrors().get(0).getErrorType(), ErrorType.ValidationError);
        assertNull(result.getData());
    }

    public void clientSpecifiedId() {
        addInstance(addJohnCarterInstance, addJohnCarterInstanceResults);

        assertFalse(instanceQuery.isEmpty());
        GraphQLResult result = instanceService.executeQuery(instanceQuery, VARIABLE_MAP,
                buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(),
                "{viewer={instances={edges=[{node={id=english, memberFive={id=ski_ski_ski}}}]}}}");

        // check search for the instance
        String french =
                "query{viewer{instances(ids:[\"french\"]){edges{node{id}}}}}";
        result = instanceService.executeQuery(french, VARIABLE_MAP,
                new SchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{viewer={instances={edges=[]}}}");
        String english = "query{viewer{instances(ids:[\"english\"]){edges{node{id}}}}}";
        result = instanceService.executeQuery(english, VARIABLE_MAP,
                new SchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{viewer={instances={edges=[{node={id=english}}]}}}");
    }

    private void addInstance(String query, String expectedResult) {
        assertFalse(query.isEmpty());
        GraphQLResult result = instanceService.executeQuery(query, VARIABLE_MAP,
                        buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), expectedResult);
    }

    public void instanceCanReferArrayOfInstancesOfTheSameType() throws Exception {
        addInstance(addJohnCarterInstance, addJohnCarterInstanceResults);
        addInstance(addJohnCarterInstanceReferringJohnCarter, viewJohnCarterInstanceReferringJohnCarter);
    }

    public void clientMissingIdError() {
        assertFalse(instanceQuery.isEmpty());
        GraphQLResult result =
                instanceService.executeQuery(addDynamicInstanceServerId,
                                             VARIABLE_MAP, buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertFalse(result.isSuccessful());
        assertEquals(result.getErrors().get(0).getMessage(),
                "Exception while fetching data (/upsertSchemaInstance) : Client must specify ID");
    }

    public void removeInstance() {
        addInstance(addJohnCarterInstance, addJohnCarterInstanceResults);
        GraphQLResult result = instanceService.executeQuery(deleteQuery, VARIABLE_MAP,
                buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{removeInstance={id=english}}");
    }

    public void serverGeneratedSchema() {
        final String OTHER_SCHEMA = "Of_Mars";
        SchemaWriteAccess writeAccess = new SchemaWriteAccess();
        writeAccess.addPermission(OTHER_SCHEMA, SchemaWriteAccess.INSTANCE_MODIFY);
        writeAccess.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);
        GraphQLResult result = schemaService.executeQuery(addServerIdSchema, buildSchemaVariableMap(), writeAccess);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), addServerIdSchemaResults);

        Map<String, Object> instanceVariableMap = buildVariableMap(OTHER_SCHEMA, OTHER_SCHEMA);
        result = instanceService.executeQuery(addOfMarsBogusInstance, instanceVariableMap,
                writeAccess, DEFAULT_MAX_RECURSE_DEPTH);
        assertFalse(result.isSuccessful());

        result = instanceService.executeQuery(addOfMarsInstance, instanceVariableMap,
                writeAccess, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertTrue(result.getData().toString().startsWith("{upsertSchemaInstance={id="));
    }

    public void merge() {
        addInstance(addJohnCarterInstance, addJohnCarterInstanceResults);

        addInstance(mergeInstance, mergeResult);
    }

    /*
     * Test multiple different combinations of empty references.  This insures that every variation is covered.
     * Otherwise, an earlier failure to detect could mask bad data later in the example.
     */
    public void emptyReference() {
        String preQuery = "mutation  { upsertSchemaInstance(schemaInstance: { id: \"english\",";
        String postQuery = "  }) { id } }";
        String[] badFields = {"memberFive: \"\"",
                "memberArrayFour: [\"\"]", "memberFour: \"\"", "memberSix: { valueMemberFour: \"\" }}"};

        Arrays.stream(badFields).forEach(badField -> testBadField(preQuery + badField + postQuery));
    }

    public void testLabeling() {
        String baldy = "{viewer={instances={edges=[{node={id=ski_ski_ski, resort=baldy}}]}}}";
        String kirkwood = "{viewer={instances={edges=[{node={id=ski_ski_ski, resort=Kirkwood}}]}}}";
        assertFalse(addLabeledSkiBob.isEmpty());
        assertFalse(viewSkibob.isEmpty());
        assertFalse(viewSkiBobLabel.isEmpty());

        Map<String, Object> variableMap = buildVariableMap("skibob");

        GraphQLResult result = instanceService.executeQuery(viewSkibob, variableMap,
                buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), baldy);

        result = instanceService.executeQuery(addLabeledSkiBob, variableMap,
                buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());

        result = instanceService.executeQuery(viewSkiBobLabel, variableMap,
                buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), kirkwood);

        result = instanceService.executeQuery(viewSkibob, variableMap,
                buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), baldy);

        result = instanceService.executeQuery(deleteSkiBobLabel, variableMap,
                buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), "{removeInstance={id=ski_ski_ski}}");
    }

    private void testBadField(String badQuery) {
        GraphQLResult result = instanceService.executeQuery(
                badQuery, VARIABLE_MAP, buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertFalse(result.isSuccessful());
    }

    public void deleteAllInstances() {
        addInstance(addJohnCarterInstance, addJohnCarterInstanceResults);

        addInstance("mutation  { removeAllInstances { count }}", "{removeAllInstances={count=1}}");
    }

    public void findSchemaInstanceWithSchemaInstanceKey() throws Exception{
       GraphQLResult result = instanceService.executeQuery(viewSkibobWithSchemaInstanceKey, buildVariableMap("skibob"),
                buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), viewSkibobWithSchemaInstanceKeyResult);
    }

    public void testUpdatedDate() throws Exception {
        String baldy = "{viewer={instances={edges=[{node={id=ski_ski_ski, resort=baldy}}]}}}";
        Map<String, Object> variableMap = buildVariableMap("skibob");

        LocalDateTime beforeUpdateDate = LocalDateTime.now();

        // Update skibob
        loadSchemaInstances();

        GraphQLResult result = instanceService.executeQuery(viewSkibobWithUpdateDate, variableMap,
                buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());

        // Extract updateDate from the result
        JsonNode jsonNode = new ObjectMapper().valueToTree(result.toSpecification());
        Long updateDateMillis = jsonNode.at("/data/viewer/instances/edges/0/node/updateDate").asLong();

        assertNotNull(updateDateMillis);

        LocalDateTime updateDate =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(updateDateMillis), ZoneId.systemDefault());

        // The date should be >= beforeUpdateDate (= for processing under milliseconds)
        assertTrue(updateDate.compareTo(beforeUpdateDate) >= 0);
    }

}
