package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.repository.InMemorySchemaRepository;
import com.nfl.dm.shield.dynamic.repository.StubbedExternalReferenceRepository;
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

    @Autowired
    private StubbedExternalReferenceRepository externalReferenceRepository;

    void loadSchema(String schema, String schemaResults) {
        // Set up the schema
        assertFalse(schema.isEmpty());
        GraphQLResult result = graphQLSchemaService.executeQuery(schema, buildSchemaVariableMap(), buildSchemaWriteAccess());
        assertTrue(result.isSuccessful());
        String actual = result.getData().toString();
        Assert.assertEquals(actual, schemaResults);
    }

    GraphQLResult loadErrorSchema(String errorSchema) {
        return graphQLSchemaService.executeQuery(errorSchema, buildSchemaVariableMap(), buildSchemaWriteAccess());
    }

    @BeforeMethod
    public void loadExternalData() {
        loadSchema();

        final String IMAGE_ID = "1234";
        final String VIDEO_ID = "5678";
        final String AUDIO_ID = "9";

        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("id", IMAGE_ID);
        imageMap.put("title", "Hitchhikers Guide");
        externalReferenceRepository.loadExternalInstance("Image", IMAGE_ID, imageMap);

        Map<String, Object> videoMap = new HashMap<>();
        videoMap.put("id", VIDEO_ID);
        videoMap.put("title", "Casablanca");
        videoMap.put("caption", "The greatest movie ever.");

        externalReferenceRepository.loadExternalInstance("Video", VIDEO_ID, videoMap);

        Map<String, Object> audioMap = new HashMap<>();
        audioMap.put("id", AUDIO_ID);
        audioMap.put("title", "Yesterday");
        audioMap.put("codec", "FLAC");

        externalReferenceRepository.loadExternalInstance("Audio", AUDIO_ID, audioMap);
    }


    GraphQLResult execute(String instanceNamespace, String query) {
        Map<String, Object> variableMap = buildVariableMap(INSTANCE_NAME_SPACE, instanceNamespace);
        return instanceService.executeQuery(query,
                variableMap, buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
    }

    abstract void loadSchema();

    @AfterMethod
    public void cleanup() {
        inMemorySchemaRepository.clearForInstanceTesting();
        inMemorySchemaRepository.clearForSchemaTesting();
        externalReferenceRepository.clearForExternalTesting();
    }
}
