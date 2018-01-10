package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import graphql.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.*;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class MultiTypeDynamicReferenceTest extends BaseBeanTest {

    @Autowired
    private GraphQLSchemaService schemaService;
    @Autowired
    private GraphQLInstanceService instanceService;

    private final String addVideo;
    private final String addPhoto;
    private final String addMedia;
    private final String addLibrary;
    private final String addBook;
    private final String addPhotoResult;
    private final String addVideoResult;
    private final String addMediaResult;
    private final String addLibraryResult;
    private final String addBookResult;
    private final String addPhotoInstance;
    private final String addVideoInstance;
    private final String addLibraryInstance;
    private final String addPhotoMediaInstance;
    private final String addDogInstance;
    private final String addVideoMediaInstance;
    private final String addLibraryInstanceResult;
    private final String addPhotoInstanceResult;
    private final String addVideoInstanceResult;
    private final String addMediaInstanceResult;
    private final String addDogInstanceResult;
    private final String addBadMediaRef;
    private final String addBadMediaRefResult;
    private final String mediaInstanceWithWrongType;
    private final String deleteVideoInstance;
    private final String deleteVideoInstanceResult;
    private final String viewLibraryAfterVideoDeletedResult;
    private final String addVideoMediaInstanceResult;
    private final String viewInstance;

    private final SchemaWriteAccess writeAccess;

    public MultiTypeDynamicReferenceTest() {
        writeAccess = buildSchemaWriteAccess();
        addBook = loadFromFile("graphql/multi_type/add_dog.txt");
        addPhoto = loadFromFile("graphql/multi_type/add_photo.txt");
        addVideo = loadFromFile("graphql/multi_type/add_video.txt");
        addMedia = loadFromFile("graphql/multi_type/add_media.txt");
        addLibrary = loadFromFile("graphql/multi_type/add_library.txt");
        addDogInstance = loadFromFile("graphql/multi_type/add_dog_instance.txt");
        addPhotoInstance = loadFromFile("graphql/multi_type/add_photo_instance.txt");
        addVideoInstance = loadFromFile("graphql/multi_type/add_video_instance.txt");
        addPhotoMediaInstance = loadFromFile("graphql/multi_type/add_photo_media_instance.txt");
        addVideoMediaInstance = loadFromFile("graphql/multi_type/add_video_media_instance.txt");
        addLibraryInstance = loadFromFile("graphql/multi_type/add_library_instance.txt");
        deleteVideoInstance = loadFromFile("graphql/multi_type/delete_video_instance.txt");
        viewInstance = loadFromFile("graphql/multi_type/view_library_instance.txt");
        addBadMediaRef = loadFromFile("graphql/multi_type/error/wrong_reference_id.txt");
        addBookResult = loadFromFile("graphql/multi_type/add_dog_result.txt");
        addPhotoResult = loadFromFile("graphql/multi_type/add_photo_result.txt");
        addVideoResult = loadFromFile("graphql/multi_type/add_video_result.txt");
        addMediaResult = loadFromFile("graphql/multi_type/add_media_result.txt");
        addLibraryResult = loadFromFile("graphql/multi_type/add_library_result.txt");
        addDogInstanceResult = loadFromFile("graphql/multi_type/add_dog_instance_result.txt");
        addPhotoInstanceResult = loadFromFile("graphql/multi_type/add_photo_instance_result.txt");
        addVideoInstanceResult = loadFromFile("graphql/multi_type/add_video_instance_result.txt");
        addMediaInstanceResult = loadFromFile("graphql/multi_type/add_media_instance_result.txt");
        addLibraryInstanceResult = loadFromFile("graphql/multi_type/add_library_instance_result.txt");
        addVideoMediaInstanceResult = loadFromFile("graphql/multi_type/add_video_media_instance_result.txt");
        deleteVideoInstanceResult = loadFromFile("graphql/multi_type/delete_video_instance_result.txt");
        viewLibraryAfterVideoDeletedResult = loadFromFile("graphql/multi_type/view_library_after_video_del_result.txt");
        addBadMediaRefResult = loadFromFile("graphql/multi_type/error/missing_ref_result.txt");
        mediaInstanceWithWrongType = loadFromFile("graphql/multi_type/error/media_with_wrong_type.txt");
    }

    @BeforeClass
    private void setupSchemas() {
        Map<String, Object> schemaVariableMap = buildSchemaVariableMap();
        GraphQLResult result;
        result = schemaService.executeQuery(addPhoto, schemaVariableMap, writeAccess);
        assertResult(result, addPhotoResult);

        result = schemaService.executeQuery(addVideo, schemaVariableMap, writeAccess);
        assertResult(result, addVideoResult);

        result = schemaService.executeQuery(addMedia, schemaVariableMap, writeAccess);
        assertResult(result, addMediaResult);

        result = schemaService.executeQuery(addBook, schemaVariableMap, writeAccess);
        assertResult(result, addBookResult);

        result = schemaService.executeQuery(addLibrary, schemaVariableMap, writeAccess);
        assertResult(result, addLibraryResult);
    }

    @BeforeMethod
    private void setupInstances() {
        GraphQLResult result;

        result = upsert(DOG, addDogInstance);
        assertResult(result, addDogInstanceResult);

        result = upsert(VIDEO, addVideoInstance);
        assertResult(result, addVideoInstanceResult);

        result = upsert(PHOTO, addPhotoInstance);
        assertResult(result, addPhotoInstanceResult);
    }

    @Test
    public void addMediaInstanceWithPhotoReference() {
        GraphQLResult result = upsert(MEDIA, addPhotoMediaInstance);
        assertResult(result, addMediaInstanceResult);
    }

    @Test
    public void addPhotoMediaInstanceAndThenReplaceItToVideo() {
        GraphQLResult result = upsert(MEDIA, addPhotoMediaInstance);
        assertResult(result, addMediaInstanceResult);


        result = upsert(MEDIA, addVideoMediaInstance);
        assertResult(result, addVideoMediaInstanceResult);
    }

    @Test
    public void addLibraryInstanceWhichStoresPhotoAndVideos() {
        GraphQLResult result = upsert(LIBRARY, addLibraryInstance);
        assertResult(result, addLibraryInstanceResult);
    }

    @Test
    public void removeVideoFromLibrary() {
        GraphQLResult result = upsert(LIBRARY, addLibraryInstance);
        assertResult(result, addLibraryInstanceResult);

        result = upsert(VIDEO, deleteVideoInstance);
        assertResult(result, deleteVideoInstanceResult);

        result = upsert(LIBRARY, viewInstance);
        assertResult(result, viewLibraryAfterVideoDeletedResult);
    }

    @Test
    public void addMediaInstanceWithBadReference() {
        GraphQLResult result = upsert(MEDIA, addBadMediaRef);
        assertResult(result, addBadMediaRefResult);
    }

    @Test
    public void addMediaInstanceWithWrongTypeDefinition() {
        GraphQLResult result = upsert(MEDIA, mediaInstanceWithWrongType);

        assertFalse(result.isSuccessful());
        assertEquals(result.getErrors().get(0).getErrorType(), ErrorType.ValidationError);
        assertEquals(result.getErrors().get(0).getMessage(),
                "Validation error of type UnknownType: Unknown type Dog");
    }

    private GraphQLResult upsert(String key, String query) {
        Map<String, Object> variableMap = buildVariableMap(INSTANCE_NAME_SPACE, key);
        return instanceService.executeQuery(query, variableMap, writeAccess, DEFAULT_MAX_RECURSE_DEPTH);
    }

    @AfterMethod
    public void clearInstanceRepo() {
        inMemorySchemaRepository.clearForInstanceTesting();
    }


    @AfterClass
    public void tearDown() {
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}