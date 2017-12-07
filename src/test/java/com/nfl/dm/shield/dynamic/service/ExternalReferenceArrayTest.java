package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class ExternalReferenceArrayTest extends BaseExternalReferenceTest {

    private static final String SIMON = "Simon";

    private final String addSimonArray;
    private final String addSimonArrayResults;
    private final String simonInstanceArrayInstance;
    private final String addSimonArrayInstanceResult;
    private final String viewSimonArray;
    private final String viewSimonArrayResults;

    public ExternalReferenceArrayTest() {
        addSimonArray = loadFromFile("graphql/external_reference/add_simon_array.txt");
        addSimonArrayResults = loadFromFile("graphql/external_reference/add_simon_array_results.txt");
        simonInstanceArrayInstance = loadFromFile("graphql/external_reference/add_simon_array_instance.txt");
        addSimonArrayInstanceResult = loadFromFile("graphql/external_reference/add_simon_array_instance_results.txt");
        viewSimonArray = loadFromFile("graphql/external_reference/view_simon_array.txt");
        viewSimonArrayResults = loadFromFile("graphql/external_reference/view_simon_array_results.txt");
    }

    public void resolveArrayOfImages() {
        // Add an external reference
        GraphQLResult result = upsert(SIMON, simonInstanceArrayInstance);
        assertResult(result, addSimonArrayInstanceResult);

        // Do a normal instance query
        result = upsert(SIMON, viewSimonArray);
        assertResult(result, viewSimonArrayResults);
    }

    void loadSchema() {
        loadSchema(addSimonArray, addSimonArrayResults);
    }
}
