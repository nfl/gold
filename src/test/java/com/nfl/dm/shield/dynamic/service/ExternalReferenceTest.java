package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;

@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class ExternalReferenceTest extends BaseExternalReferenceTest {

    private static final String SIMON = "Simon";

    private final String simonSchema;
    private final String simonSchemaResults;
    private final String simonInstance;
    private final String simonInstanceResults;
    private final String viewSimon;
    private final String viewSimonResults;
    private final String badBoy;


    public ExternalReferenceTest() {
        simonSchema = loadFromFile("graphql/external_reference/add_simon.txt");
        simonSchemaResults = loadFromFile("graphql/external_reference/add_simon_results.txt");
        simonInstance = loadFromFile("graphql/external_reference/add_simon_instance.txt");
        simonInstanceResults = loadFromFile("graphql/external_reference/add_simon_instance_results.txt");
        viewSimon = loadFromFile("graphql/external_reference/view_simon.txt");
        viewSimonResults = loadFromFile("graphql/external_reference/view_simon_results.txt");
        badBoy = loadFromFile("graphql/external_reference/add_with_bad_service_key.txt");
    }

    public void resolveExternals() {
        // Add an external reference
        GraphQLResult result = upsert(SIMON, simonInstance);
        assertResult(result, simonInstanceResults);

        // Do a normal instance query
        result = upsert(SIMON, viewSimon);
        assertResult(result, viewSimonResults);
    }

    public void badSchema() {
        GraphQLResult result = loadErrorSchema(badBoy);
        assertFalse(result.isSuccessful());
    }

    @Override
    void loadSchema() {
        loadSchema(simonSchema, simonSchemaResults);
    }

}
