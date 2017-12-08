package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.InstanceBaseBeanTest;
import com.nfl.dm.shield.dynamic.domain.context.GraphQLInstanceRequestContext;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import com.nfl.dm.shield.dynamic.service.CacheService.SchemaInstanceIdentifier;
import com.nfl.dm.shield.dynamic.service.CacheService.SchemaInstanceRequestIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

@Test
public class GraphQLInstanceServiceWithCacheTest extends InstanceBaseBeanTest {

    @Autowired
    private GraphQLInstanceService instanceService;

    @Autowired
    private CacheService cacheService;

    private final String addJohnCarterInstance;
    private final String addJohnCarterInstanceResults;

    public GraphQLInstanceServiceWithCacheTest() {
        addJohnCarterInstance = loadFromFile("graphql/add_john_carter_instance.txt");
        addJohnCarterInstanceResults = loadFromFile("graphql/add_john_carter_instance_results.txt");
    }

    public void forceReloadReferencedInstancesOnCacheForArrayField() {
        GraphQLResult result = instanceService.executeQuery(
                addJohnCarterInstance,
                buildVariableMap(INSTANCE_NAME_SPACE, JOHN_CARTER_SCHEMA),
                buildSchemaWriteAccess(),
                DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
        assertEquals(result.getData().toString(), addJohnCarterInstanceResults);

        // The cache should be cleaned before executing the queries
        cacheService.getGraphs().invalidateAll();
        assertEquals(cacheService.getGraphs().size(), 0);

        // Query an Array member field (memberArrayFour)
        String english = "query{viewer{instances(ids:[\"english\"]){edges{node{id, memberArrayFour{id}}}}}}";
        result = instanceService.executeQuery(
                english,
                buildVariableMap(INSTANCE_NAME_SPACE, JOHN_CARTER_SCHEMA),
                new SchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);

        assertTrue(result.isSuccessful());
    }

    public void graphQLInstanceRequestContextCachesValues() {
        SchemaInstanceKey schemaInstanceKey =
                new SchemaInstanceKey(JOHN_CARTER_SCHEMA, SCHEMA_NAME_SPACE, INSTANCE_NAME_SPACE);
        SchemaKey schemaKey = new SchemaKey(JOHN_CARTER_SCHEMA, SCHEMA_NAME_SPACE);

        GraphQLInstanceRequestContext instanceRequestContext =
                new GraphQLInstanceRequestContext(schemaInstanceKey, schemaKey);

        Map<SchemaInstanceKey, Set<String>> referencedInstanceIdsMap = new HashMap<>();
        referencedInstanceIdsMap.put(schemaInstanceKey, null);

        instanceRequestContext.setReferencedInstanceIdsMap(referencedInstanceIdsMap);

        instanceRequestContext.addSchemaInstanceIdsToPreloadCache(schemaInstanceKey, asList("id1", "id2"));

        assertEquals(instanceRequestContext.getReferencedInstanceIdsMap().get(schemaInstanceKey).size(), 2);
    }

    @Test
    public void schemaInstanceIdentifierEquality() throws Exception {
        SchemaInstanceKey key = new SchemaInstanceKey("aSchema", "aSchemaNS", "anInstanceNS");
        String id = "anId";
        SchemaInstanceIdentifier identifier1 = new SchemaInstanceIdentifier(key, id);
        SchemaInstanceIdentifier identifier2 = new SchemaInstanceIdentifier(key, id);

        /* Equal if same */
        assertEquals(identifier1, identifier1);
        /* Equal if content is equal */
        assertEquals(identifier1, identifier2);
        assertNotEquals(identifier1, null);
        /* An instance of a subclass cannot be equal to an instance of a base class: */
        assertNotEquals(identifier1, new SchemaInstanceIdentifier(key, id) {});
        /* Not equal if content differs */
        assertNotEquals(
                new SchemaInstanceIdentifier(key, "id1"),
                new SchemaInstanceIdentifier(key, "id2"));
        assertNotEquals(
                new SchemaInstanceIdentifier(new SchemaInstanceKey("aSchema", "aSchemaNS", "anInstanceNS"), id),
                new SchemaInstanceIdentifier(new SchemaInstanceKey("DIFFERS", "aSchemaNS", "anInstanceNS"), id));
    }

    @Test
    public void schemaInstanceRequestIdentifierEquality() throws Exception {
        SchemaInstanceKey key = new SchemaInstanceKey("aSchema", "aSchemaNS", "anInstanceNS");
        List<String> ids = singletonList("anId");
        SchemaInstanceRequestIdentifier identifier1 = new SchemaInstanceRequestIdentifier(key, ids);
        SchemaInstanceRequestIdentifier identifier2 = new SchemaInstanceRequestIdentifier(key, ids);

        /* Equal if same */
        assertEquals(identifier1, identifier1);
        /* Equal if content is equal */
        assertEquals(identifier1, identifier2);
        assertNotEquals(identifier1, null);
        /* An instance of a subclass cannot be equal to an instance of a base class: */
        assertNotEquals(identifier1, new SchemaInstanceRequestIdentifier(key, ids) {});
        /* Not equal if content differs */
        assertNotEquals(
                new SchemaInstanceRequestIdentifier(key, asList("id1", "id2")),
                new SchemaInstanceRequestIdentifier(key, asList("id1", "id3")));
        assertNotEquals(
                new SchemaInstanceRequestIdentifier(new SchemaInstanceKey("aSchema", "aSchemaNS", "anInstanceNS"), ids),
                new SchemaInstanceRequestIdentifier(new SchemaInstanceKey("DIFFERS", "aSchemaNS", "anInstanceNS"), ids));
    }

}
