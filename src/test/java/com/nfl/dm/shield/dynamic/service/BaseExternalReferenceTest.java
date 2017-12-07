package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.repository.InMemorySchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public abstract class BaseExternalReferenceTest extends BaseBeanTest {
    @Autowired
    private GraphQLInstanceService instanceService;

    @Autowired
    private GraphQLSchemaService graphQLSchemaService;

    @Autowired
    private InMemorySchemaRepository inMemorySchemaRepository;

    void loadSchema(String simonSchema, String simonSchemaResults) {
        // Set up the schema
        assertFalse(simonSchema.isEmpty());
        GraphQLResult result = graphQLSchemaService.executeQuery(simonSchema,
                buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertTrue(result.isSuccessful());
        String actual = result.getData().toString();
        Assert.assertEquals(actual, simonSchemaResults);
    }

    GraphQLResult loadErrorSchema(String errorSchema) {
        return graphQLSchemaService.executeQuery(errorSchema, buildSchemaVariableMap(), buildSchemaWriteAccess());
    }

    @BeforeMethod
    public void loadExternalData() {
        loadSchema();

        final String IMAGE_ID = "1234";
        final String VIDEO_ID = "5678";

        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("id", IMAGE_ID);
        imageMap.put("title", "Hitchhikers Guide");
        inMemorySchemaRepository.loadExternalInstance("Image", IMAGE_ID, imageMap);

        Map<String, Object> videoMap = new HashMap<>();
        videoMap.put("id", VIDEO_ID);
        videoMap.put("title", "Casablanca");
        videoMap.put("caption", "The greatest movie ever.");

        inMemorySchemaRepository.loadExternalInstance("Video", VIDEO_ID, videoMap);
    }


    GraphQLResult upsert(String key, String query) {
        Map<String, Object> variableMap = buildVariableMap(INSTANCE_NAME_SPACE, key);
        return instanceService.executeQuery(query,
                variableMap, buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
    }

    abstract void loadSchema();

    @AfterMethod
    public void cleanup() {
        inMemorySchemaRepository.clearForInstanceTesting();
        inMemorySchemaRepository.clearForSchemaTesting();
        inMemorySchemaRepository.clearForExternalTesting();
    }
}
