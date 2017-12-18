package com.nfl.dm.shield.dynamic.service;

import com.google.common.cache.Cache;
import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Set;

import static org.testng.Assert.*;

@Test
public class SchemaDefinitionCacheTest extends BaseBeanTest {
    // Variables to contain queries for the Schema Definitions used for the tests
    private final String videoSchema;
    private final String audioSchema;
    private final String mediaSchema;
    private final String addMediaInstance;

    private final String CACHE_TEST_NAMESPACE = "CacheTestNameSpace";

    private final static String VIDEO_DOMAIN_NAME = "Video";
    private final static String AUDIO_DOMAIN_NAME = "Audio";
    private final static String MEDIA_DOMAIN_NAME = "Media";

    @Autowired
    private GraphQLSchemaService schemaService;

    @Autowired
    private GraphQLInstanceService instanceService;

    @Autowired
    private CacheService cacheService;


    public SchemaDefinitionCacheTest() {
        videoSchema = loadFromFile("graphql/cache/add_video.txt");
        audioSchema = loadFromFile("graphql/cache/add_audio.txt");
        mediaSchema = loadFromFile("graphql/cache/add_media.txt");
        addMediaInstance = loadFromFile("graphql/cache/add_media_instance.txt");
    }

    public void schemaCacheIsInitiallyEmpty() {
        Cache<SchemaKey, Set<SchemaKey>> schemaDescriptionIdCache = cacheService.getSchemaDescriptionIdCache();

        assertEquals(schemaDescriptionIdCache.size(), 0);
    }

    public void schemaCacheItemsGetAddedUponInstanceAccess() {
        // Initially there should be no entries on the schemaDescriptionIdCache
        Cache<SchemaKey, Set<SchemaKey>> schemaDescriptionIdCache = cacheService.getSchemaDescriptionIdCache();
        assertEquals(schemaDescriptionIdCache.size(), 0);

        // Creating auxiliary objects prior to execute queries
        Map<String, Object> variableMap = buildVariableMap(CACHE_TEST_NAMESPACE, MEDIA_DOMAIN_NAME);

        SchemaWriteAccess access = buildSchemaWriteAccessCacheTestNamespace();

        // Run a query on media that should populate the reference cache for it
        GraphQLResult addMediaInstanceResult = instanceService.executeQuery(addMediaInstance, variableMap, access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addMediaInstanceResult.isSuccessful());

        // Lookup for "Media" referenced types
        Set<SchemaKey> schemaKeySets = schemaDescriptionIdCache.getIfPresent(new SchemaKey(MEDIA_DOMAIN_NAME, SCHEMA_NAME_SPACE));

        // Media should have 2 referenced types (Video and Audio)
        assertNotNull(schemaKeySets);
        assertEquals(schemaKeySets.size(), 2);

        assertTrue(schemaKeySets.contains(new SchemaKey(VIDEO_DOMAIN_NAME, SCHEMA_NAME_SPACE)));
        assertTrue(schemaKeySets.contains(new SchemaKey(AUDIO_DOMAIN_NAME, SCHEMA_NAME_SPACE)));
    }

    @BeforeMethod
    private void loadSchemaAndCleanCache() {
        SchemaWriteAccess access = buildSchemaWriteAccessCacheTestNamespace();

        Map<String, Object> variableMap = buildVariableMap(CACHE_TEST_NAMESPACE);

        GraphQLResult resultVideoSchemaCreation = schemaService.executeQuery(videoSchema, variableMap, access);
        assertTrue(resultVideoSchemaCreation.isSuccessful());

        GraphQLResult resultAudioSchemaCreation = schemaService.executeQuery(audioSchema, variableMap, access);
        assertTrue(resultAudioSchemaCreation.isSuccessful());

        GraphQLResult resultMediaSchemaCreation = schemaService.executeQuery(mediaSchema, variableMap, access);
        assertTrue(resultMediaSchemaCreation.isSuccessful());

        cacheService.getSchemaDescriptionIdCache().invalidateAll();
    }

    private SchemaWriteAccess buildSchemaWriteAccessCacheTestNamespace() {
        SchemaWriteAccess mutableAccess = new SchemaWriteAccess();
        mutableAccess.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);
        mutableAccess.addPermission(CACHE_TEST_NAMESPACE, SchemaWriteAccess.INSTANCE_MODIFY);

        return mutableAccess;
    }

    @AfterMethod
    private void clearMemory() {
        inMemorySchemaRepository.clearForInstanceTesting();
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
