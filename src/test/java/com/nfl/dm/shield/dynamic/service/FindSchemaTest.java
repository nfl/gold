package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import com.nfl.dm.shield.dynamic.InstanceBaseBeanTest;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.*;

@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class FindSchemaTest extends InstanceBaseBeanTest {

    @Autowired
    private GraphQLSchemaService graphQLSchemaService;

    private final String findWithSchemaKeyAndSchemaInstanceKeyQueryResult;

    private String findBobQuery;

    private String findBobAndJohnQuery;

    private String findWithSchemaKeyAndSchemaInstanceKeyQuery;

    private String findSchemasWithInstanceCount;

    public FindSchemaTest() {
        findBobQuery =
                loadFromFile("graphql/find_ski_bob.txt");

        findBobAndJohnQuery =
                loadFromFile("graphql/find_ski_bob_and_john_carter.txt");

        findWithSchemaKeyAndSchemaInstanceKeyQuery =
                loadFromFile("graphql/schemaKey/find_ski_bob_with_schemaKey.txt");

        findWithSchemaKeyAndSchemaInstanceKeyQueryResult =
                loadFromFile("graphql/schemaKey/find_schema_with_schemaKey_result.json");

        findSchemasWithInstanceCount =
                loadFromFile("graphql/find_schemas_with_instance_counts.txt");
    }

    public void minimalFindBob() {
        assertFalse(findBobQuery.isEmpty());
        GraphQLResult response =
                graphQLSchemaService.executeQuery(findBobQuery, buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertTrue(response.isSuccessful());
        assertEquals(response.getData().toString(), "{viewer={schemas={edges=[{node={name=skibob}}]}}}");
    }

    public void findSchemaWithSchemaKey() throws Exception {
        assertFalse(findBobAndJohnQuery.isEmpty());
        GraphQLResult response = graphQLSchemaService.executeQuery(findWithSchemaKeyAndSchemaInstanceKeyQuery,
                buildSchemaVariableMap(), buildSchemaWriteAccess());

        assertTrue(response.isSuccessful());
        assertNotNull(response.getData());
        JSONAssert.assertEquals(findWithSchemaKeyAndSchemaInstanceKeyQueryResult, response.getData().toString(),true);
    }

    public void findSchemasAndCountInstances() {
        Map<String, Object> vars = buildSchemaVariableMap();
        vars.put("instanceNamespace", INSTANCE_NAME_SPACE);

        GraphQLResult response = graphQLSchemaService.executeQuery(findSchemasWithInstanceCount, vars, buildSchemaWriteAccess());

        assertTrue(response.isSuccessful());
        /* Base test class inserted one instance of skibob and no instances of John_Carter, hence the counters: */
        assertEquals(response.getData().toString(),
                "{viewer={schemas={edges=[" +
                        "{node={name=skibob, instanceCount=1}}, " +
                        "{node={name=John_Carter, instanceCount=0}}" +
                        "]}}}");

        vars.put("instanceNamespace", "instance_namespace_without_instances_of_skibob");

        response = graphQLSchemaService.executeQuery(findSchemasWithInstanceCount, vars, buildSchemaWriteAccess());

        assertTrue(response.isSuccessful());
        /* When counting instances in a namespace without instances, we get zeros: */
        assertEquals(response.getData().toString(),
                "{viewer={schemas={edges=[" +
                        "{node={name=skibob, instanceCount=0}}, " +
                        "{node={name=John_Carter, instanceCount=0}}" +
                        "]}}}");
    }

}
