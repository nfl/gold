package com.nfl.dm.shield.dynamic.referencedby;

import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import com.nfl.dm.shield.dynamic.service.CacheService;
import com.nfl.dm.shield.dynamic.service.GraphQLInstanceService;
import com.nfl.dm.shield.dynamic.service.GraphQLResult;
import com.nfl.dm.shield.dynamic.service.GraphQLSchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertTrue;


@Test
public class ReferencedByTest extends BaseBeanTest {
    // Variables to contain queries for the Schema Definitions used for the tests
    private final String videoSchema;
    private final String audioSchema;
    private final String mediaSchema;
    private final String descriptionBlockSchema;
    private final String playlistSchema;
    private final String audioListSchema;
    private final String petSchema;
    private final String addVideoInstance;
    private final String addAudioInstance;
    private final String addMediaInstance;
    private final String addDescriptionBlockInstance;
    private final String addVideoWithSelfReference;
    private final String addVideoInstanceWithLinkToOtherVideo;
    private final String viewReferencedByForVideo;
    private final String addMediaInstanceNoReference;
    private final String viewReferencedByForVideoResponse;
    private final String addPlaylistInstance;
    private final String addAudioListInstance;
    private final String addPetInstance;
    private final String addPetInstanceWithAnotherInstanceReference;
    private final String viewReferencedByForDescriptionBlock;
    private final String viewReferencedByForDescriptionBlockResponse;
    private final String viewReferencedByForVideoOnPlaylistResponse;
    private final String viewReferencedByForAudio;
    private final String ViewReferencedByForAudioOnAudioListResponse;
    private final String viewReferencedByForVideoOnSequenceResponse;
    private final String viewReferencedByForVideoSelfReferenceResponse;
    private final String viewReferencedByForPetInstances;
    private final String viewReferencedByForPetInstancesResponse;

    private final String addAudioInstanceWithVideoIdOnDesc;


    private final String REFERENCEDBY_TEST_NAMESPACE = "ReferencedByNameSpace";

    private final static String VIDEO_SCHEMA_NAME = "Video";
    private final static String AUDIO_SCHEMA_NAME = "Audio";
    private final static String MEDIA_SCHEMA_NAME = "Media";
    private final static String PET_SCHEMA_NAME = "Pet";
    private final static String DESCRIPTION_BLOCK_SCHEMA_NAME = "DescriptionBlock";
    private final static String PLAYLIST_SCHEMA_NAME = "Playlist";
    private final static String AUDIOLIST_SCHEMA_NAME = "AudioList"
;
    private static final Logger log = LoggerFactory.getLogger(ReferencedByTest.class);

    @Autowired
    private GraphQLSchemaService schemaService;

    @Autowired
    private GraphQLInstanceService instanceService;

    @Autowired
    protected CacheService cacheService;

    public ReferencedByTest() {
        videoSchema = loadFromFile(
                "graphql/instance/referenced_by/add_video.txt");
        audioSchema = loadFromFile(
                "graphql/instance/referenced_by/add_audio.txt");
        mediaSchema = loadFromFile(
                "graphql/instance/referenced_by/add_media.txt");
        playlistSchema = loadFromFile(
                "graphql/instance/referenced_by/add_playlist.txt");
        descriptionBlockSchema = loadFromFile(
                "graphql/instance/referenced_by/add_description_block.txt");
        audioListSchema = loadFromFile(
                "graphql/instance/referenced_by/add_audiolist.txt");
        petSchema = loadFromFile(
                "graphql/instance/referenced_by/add_pet.txt");
        addVideoInstance = loadFromFile(
                "graphql/instance/referenced_by/add_video_instance.txt");
        addDescriptionBlockInstance = loadFromFile(
                "graphql/instance/referenced_by/add_description_block_instance.txt");
        addMediaInstance = loadFromFile(
                "graphql/instance/referenced_by/add_media_instance.txt");
        addMediaInstanceNoReference = loadFromFile(
                "graphql/instance/referenced_by/add_media_not_instance_ref.txt");
        addPlaylistInstance = loadFromFile(
                "graphql/instance/referenced_by/add_playlist_instance.txt");
        viewReferencedByForVideo = loadFromFile(
                "graphql/instance/referenced_by/view_referenced_by_for_video.txt");
        viewReferencedByForVideoOnPlaylistResponse = loadFromFile(
                "graphql/instance/referenced_by/view_referenced_by_for_video_on_playlist_response.txt");
        viewReferencedByForVideoResponse = loadFromFile(
                "graphql/instance/referenced_by/view_referenced_by_for_video_response.txt");
        viewReferencedByForDescriptionBlock = loadFromFile(
                "graphql/instance/referenced_by/view_referenced_by_for_description_block.txt");
        viewReferencedByForDescriptionBlockResponse = loadFromFile(
                "graphql/instance/referenced_by/view_referenced_by_for_description_block_response.txt");
        viewReferencedByForAudio = loadFromFile(
                "graphql/instance/referenced_by/view_referenced_by_for_audio.txt");
        ViewReferencedByForAudioOnAudioListResponse = loadFromFile(
                "graphql/instance/referenced_by/view_referenced_by_for_audio_on_audiolist_response.txt");
        addVideoInstanceWithLinkToOtherVideo = loadFromFile(
                "graphql/instance/referenced_by/add_video_instance_with_link_to_other_video.txt");
        viewReferencedByForVideoOnSequenceResponse = loadFromFile(
                "graphql/instance/referenced_by/view_referenced_by_for_video_on_sequence_response.txt");
        addVideoWithSelfReference = loadFromFile(
                "graphql/instance/referenced_by/add_video_w_self_reference.txt");
        viewReferencedByForVideoSelfReferenceResponse = loadFromFile(
                "graphql/instance/referenced_by/view_referenced_by_for_video_w_self_reference_response.txt");
        viewReferencedByForPetInstances = loadFromFile(
                "graphql/instance/referenced_by/view_referenced_by_for_pet.txt");
        viewReferencedByForPetInstancesResponse = loadFromFile(
                "graphql/instance/referenced_by/view_referenced_by_for_pet_response.txt");
        addAudioInstance = loadFromFile(
                "graphql/instance/referenced_by/add_audio_instance.txt");
        addAudioListInstance = loadFromFile(
                "graphql/instance/referenced_by/add_audiolist_instance.txt");
        addAudioInstanceWithVideoIdOnDesc = loadFromFile(
                "graphql/instance/referenced_by/add_audio_instance_with_videoid1_on_desc.txt");
        addPetInstance = loadFromFile(
                "graphql/instance/referenced_by/add_pet_instance.txt");
        addPetInstanceWithAnotherInstanceReference = loadFromFile(
                "graphql/instance/referenced_by/add_pet_instance_with_another_instance_reference.txt");
    }


    public void videoReferencedByMultiTypeDynamicReferenceMedia() {
        SchemaWriteAccess access = buildSchemaWriteAccessCacheTestNamespace();

        // Add a video instance
        GraphQLResult addVideoInstanceResult = instanceService.executeQuery(addVideoInstance,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, VIDEO_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addVideoInstanceResult.isSuccessful());

        // Add a media instance pointing to the video
        GraphQLResult addMediaInstanceResult = instanceService.executeQuery(addMediaInstance,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, MEDIA_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addMediaInstanceResult.isSuccessful());

        // Add another media instance that does not point to the video but contains text that includes
        // the id (not a real reference)
        addMediaInstanceResult = instanceService.executeQuery(addMediaInstanceNoReference,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, MEDIA_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addMediaInstanceResult.isSuccessful());

        // Verify "referencedBy" for the Video returns just the Media instance
        GraphQLResult viewReferencedByForVideoResult = instanceService.executeQuery(viewReferencedByForVideo,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, VIDEO_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);

        assertTrue(viewReferencedByForVideoResult.isSuccessful());
        assertResult(viewReferencedByForVideoResult, viewReferencedByForVideoResponse);
    }


    public void descriptionBlockReferencedByOtherDynamicReferenceMedia() {
        SchemaWriteAccess access = buildSchemaWriteAccessCacheTestNamespace();

        // Add a Block Description instance
        GraphQLResult addDescriptionBlockInstanceResult = instanceService.executeQuery(addDescriptionBlockInstance,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, DESCRIPTION_BLOCK_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addDescriptionBlockInstanceResult.isSuccessful());

        // Add a media instance pointing to the Description Block
        GraphQLResult addMediaInstanceResult = instanceService.executeQuery(addMediaInstance,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, MEDIA_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addMediaInstanceResult.isSuccessful());

        // Verify "referencedBy" for the Description Block returns just the Media instance
        GraphQLResult viewReferencedByForDescriptionBlockResult = instanceService.executeQuery(
                viewReferencedByForDescriptionBlock,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, DESCRIPTION_BLOCK_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);

        log.info("ReferenceByResponse:  {}", viewReferencedByForDescriptionBlockResult.getData().toString());

        assertTrue(viewReferencedByForDescriptionBlockResult.isSuccessful());
        assertResult(viewReferencedByForDescriptionBlockResult, viewReferencedByForDescriptionBlockResponse);
    }


    @SuppressWarnings("Duplicates")
    public void videoReferencedByMultitypeArrayPlaylist() {
        SchemaWriteAccess access = buildSchemaWriteAccessCacheTestNamespace();

        // Add a video instance
        GraphQLResult addVideoInstanceResult = instanceService.executeQuery(addVideoInstance,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, VIDEO_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addVideoInstanceResult.isSuccessful());

        // Add playlist instance pointing to a video and audio
        GraphQLResult addPlaylistInstanceResult = instanceService.executeQuery(addPlaylistInstance,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, PLAYLIST_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addPlaylistInstanceResult.isSuccessful());

        // Verify "referencedBy" on the Video returns the Playlist instance
        GraphQLResult viewReferencedByForVideoResult = instanceService.executeQuery(viewReferencedByForVideo,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, VIDEO_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);

        assertTrue(viewReferencedByForVideoResult.isSuccessful());
        assertResult(viewReferencedByForVideoResult, viewReferencedByForVideoOnPlaylistResponse);
    }

    @SuppressWarnings("Duplicates")
    public void audioReferencedByOtherDynamicArrayPlaylist() {
        SchemaWriteAccess access = buildSchemaWriteAccessCacheTestNamespace();

        // Add an Audio instance
        GraphQLResult addAudioInstanceResult = instanceService.executeQuery(addAudioInstance,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, AUDIO_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addAudioInstanceResult.isSuccessful());

        // Add audio to Audiolist
        GraphQLResult addAudioListInstanceResult = instanceService.executeQuery(addAudioListInstance,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, AUDIOLIST_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addAudioListInstanceResult.isSuccessful());

        // Verify "referencedBy" on the audio returns the Audio instance
        GraphQLResult viewReferencedByForAudioResult = instanceService.executeQuery(viewReferencedByForAudio,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, AUDIO_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);

        assertTrue(viewReferencedByForAudioResult.isSuccessful());
        assertResult(viewReferencedByForAudioResult, ViewReferencedByForAudioOnAudioListResponse);
    }

    @SuppressWarnings("Duplicates")
    public void videoReferencedBySameReferenceType() {
        SchemaWriteAccess access = buildSchemaWriteAccessCacheTestNamespace();

        // Add a video instance
        GraphQLResult addVideoInstanceResult = instanceService.executeQuery(addVideoInstance,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, VIDEO_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addVideoInstanceResult.isSuccessful());

        // Add videoid2 pointing to videoid1
        GraphQLResult addVideoInstanceSequenceResult = instanceService.executeQuery(
                addVideoInstanceWithLinkToOtherVideo,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, VIDEO_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addVideoInstanceSequenceResult.isSuccessful());

        // Add and audio that contains videoid1 on description but it is not a reference
        GraphQLResult addAudioInstanceResult = instanceService.executeQuery(addAudioInstanceWithVideoIdOnDesc,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, AUDIO_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addAudioInstanceResult.isSuccessful());

        // Verify "referencedBy" on the Video returns the Playlist instance
        GraphQLResult viewReferencedByForVideoResult = instanceService.executeQuery(viewReferencedByForVideo,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, VIDEO_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);

        assertTrue(viewReferencedByForVideoResult.isSuccessful());
        assertResult(viewReferencedByForVideoResult, viewReferencedByForVideoOnSequenceResponse);
    }

    @SuppressWarnings("Duplicates")
    public void videoWithSelfReference() {
        SchemaWriteAccess access = buildSchemaWriteAccessCacheTestNamespace();

        // Add video with link to itself
        GraphQLResult addVideoInstanceWithSelfReference = instanceService.executeQuery(addVideoWithSelfReference,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, VIDEO_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addVideoInstanceWithSelfReference.isSuccessful());

        // Verify "referencedBy" on the Video returns the Playlist instance
        GraphQLResult viewReferencedByForVideoWithSelfReferenceResult = instanceService.executeQuery(
                viewReferencedByForVideo, buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, VIDEO_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);

        assertTrue(viewReferencedByForVideoWithSelfReferenceResult.isSuccessful());
        assertResult(viewReferencedByForVideoWithSelfReferenceResult, viewReferencedByForVideoSelfReferenceResponse);

    }

    public void petWithSameInstanceReferences() {
        SchemaWriteAccess access = buildSchemaWriteAccessCacheTestNamespace();

        // Add pet instance
        GraphQLResult addPetInstance = instanceService.executeQuery(this.addPetInstance,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, PET_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addPetInstance.isSuccessful());

        // Add pet instance referencing another pet instance
        GraphQLResult addPetInstanceWithReference = instanceService.executeQuery(this.addPetInstanceWithAnotherInstanceReference,
                buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, PET_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(addPetInstanceWithReference.isSuccessful());

        // Verify "referencedBy" on Pet returns the referenced pets ids
        GraphQLResult viewReferencedByForPetInstancesResult = instanceService.executeQuery(
                this.viewReferencedByForPetInstances, buildVariableMap(REFERENCEDBY_TEST_NAMESPACE, PET_SCHEMA_NAME),
                access, DEFAULT_MAX_RECURSE_DEPTH);

        assertTrue(viewReferencedByForPetInstancesResult.isSuccessful());
        assertResult(viewReferencedByForPetInstancesResult, viewReferencedByForPetInstancesResponse);
    }


    @BeforeMethod
    private void loadSchemas() {
        SchemaWriteAccess access = buildSchemaWriteAccessCacheTestNamespace();

        Map<String, Object> variableMap = buildVariableMap(REFERENCEDBY_TEST_NAMESPACE);

        GraphQLResult resultVideoSchemaCreation = schemaService.executeQuery(videoSchema, variableMap, access);
        assertTrue(resultVideoSchemaCreation.isSuccessful());

        GraphQLResult resultAudioSchemaCreation = schemaService.executeQuery(audioSchema, variableMap, access);
        assertTrue(resultAudioSchemaCreation.isSuccessful());

        GraphQLResult resultDescriptionBlockSchemaCreation = schemaService.executeQuery(descriptionBlockSchema,
                variableMap, access);
        assertTrue(resultDescriptionBlockSchemaCreation.isSuccessful());

        GraphQLResult resultMediaSchemaCreation = schemaService.executeQuery(mediaSchema, variableMap, access);
        assertTrue(resultMediaSchemaCreation.isSuccessful());

        GraphQLResult resultPlaylistSchemaCreation = schemaService.executeQuery(playlistSchema, variableMap, access);
        assertTrue(resultPlaylistSchemaCreation.isSuccessful());

        GraphQLResult resultAudioListSchemaCreation = schemaService.executeQuery(audioListSchema, variableMap, access);
        assertTrue(resultAudioListSchemaCreation.isSuccessful());

        GraphQLResult resultPetSchemaCreation = schemaService.executeQuery(petSchema, variableMap, access);
        assertTrue(resultPetSchemaCreation.isSuccessful());
    }

    private SchemaWriteAccess buildSchemaWriteAccessCacheTestNamespace() {
        SchemaWriteAccess mutableAccess = new SchemaWriteAccess();
        mutableAccess.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);
        mutableAccess.addPermission(REFERENCEDBY_TEST_NAMESPACE, SchemaWriteAccess.INSTANCE_MODIFY);

        return mutableAccess;
    }

    @AfterMethod
    private void clearMemory() {
        cacheService.getSchemaDescriptionIdCache().invalidateAll();
        inMemorySchemaRepository.clearForInstanceTesting();
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}