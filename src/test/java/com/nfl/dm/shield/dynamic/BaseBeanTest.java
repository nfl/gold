package com.nfl.dm.shield.dynamic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.nfl.dm.shield.dynamic.repository.InMemorySchemaRepository;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import com.nfl.dm.shield.dynamic.service.GraphQLResult;
import com.nfl.dm.shield.dynamic.service.GraphQLSchemaService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static com.nfl.dm.shield.dynamic.domain.BaseKey.*;


@ContextConfiguration(classes = ApplicationTestConfig.class)
public abstract class BaseBeanTest extends AbstractTestNGSpringContextTests {

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected static final String SCHEMA_NAME_SPACE = "SCHEMA";
    protected static final String INSTANCE_NAME_SPACE = "MARS";
    protected static final String JOHN_CARTER_SCHEMA = "John_Carter";

    protected static final String COLOR = "Color";
    protected static final String DOG = "Dog";
    protected static final String VIDEO = "Video";
    protected static final String PHOTO = "Photo";
    protected static final String MEDIA = "Media";
    protected static final String LIBRARY = "Library";
    protected static final String PALETTE = "Palette";

    protected static final int DEFAULT_MAX_RECURSE_DEPTH = 10;

    @Autowired
    protected InMemorySchemaRepository inMemorySchemaRepository;

    @Autowired
    protected GraphQLSchemaService schemaService;

    @Autowired
    private ObjectMapper objectMapper;

    protected String loadFromFile(String resourcePath) {
        ClassPathResource cpr = new ClassPathResource(resourcePath);
        try {
            return IOUtils.toString(cpr.getInputStream(), Charset.defaultCharset());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    protected SchemaWriteAccess buildSchemaWriteAccess() {
        SchemaWriteAccess mutableAccess = new SchemaWriteAccess();
        mutableAccess.addPermission(SCHEMA_NAME_SPACE, SchemaWriteAccess.SCHEMA_MODIFY);
        mutableAccess.addPermission(INSTANCE_NAME_SPACE, SchemaWriteAccess.INSTANCE_MODIFY);
        mutableAccess.addPermission(INSTANCE_NAME_SPACE, SchemaWriteAccess.INSTANCE_DELETE);
        mutableAccess.addPermission(INSTANCE_NAME_SPACE, SchemaWriteAccess.INSTANCE_TRUNCATE);

        return mutableAccess;
    }

    protected static Map<String, Object> buildSchemaVariableMap() {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put(DYNAMIC_TYPE_NAMESPACE, SCHEMA_NAME_SPACE);
        return retMap;
    }

    protected static Map<String, Object> buildVariableMap(String schemaName) {
        return buildVariableMap(INSTANCE_NAME_SPACE, schemaName);
    }

    protected static Map<String, Object> buildVariableMap(String instanceNamespace, String schemaName) {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put(DYNAMIC_TYPE_NAMESPACE, SCHEMA_NAME_SPACE);
        retMap.put(DYNAMIC_INSTANCE_NAMESPACE, instanceNamespace);
        retMap.put(DYNAMIC_TYPE_NAME, schemaName);

        return retMap;
    }

    protected void assertResult(GraphQLResult actual, String expectedData) {
        Assert.assertNotNull(actual, "Actual GraphQLResult should not be null.");

        if (actual.isSuccessful()) {
            Assert.assertNotNull(actual.getData());
            Assert.assertEquals(actual.getData().toString(), String.valueOf(expectedData));
        } else {
            Assert.fail("Found errors GraphQLResult: " + actual.getErrors());
        }
    }

    protected void assertSuccess(GraphQLResult actual) throws Exception {
        Assert.assertNotNull(actual, "Actual GraphQLResult should not be null.");
        Assert.assertTrue(actual.isSuccessful());
    }


    protected GraphQLResult executeSchemaQuery(String query) throws Exception {
        GraphQLResult graphQLResult = schemaService.executeQuery(query,
                buildSchemaVariableMap(), buildSchemaWriteAccess());
        if (log.isDebugEnabled()) {
            log.debug("GraphQL schema query result: {}", toJson(graphQLResult));
        }
        return graphQLResult;
    }

    private String toJson(Object src) {
        try {
            ObjectWriter defaultPrettyPrinter = objectMapper.writerWithDefaultPrettyPrinter();
            return defaultPrettyPrinter.writeValueAsString(src);
        } catch (Exception e) {
            log.error("Error during converting object to JSON", e);
            return "{}";
        }
    }
}
