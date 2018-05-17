package com.nfl.dm.shield.dynamic.service;

import org.testng.annotations.Test;

@Test
public class ExternalReferenceArrayTest extends BaseExternalReferenceTest {

    private static final String SIMON = "Simon";
    private static final String MULTIPLE_EXT_REF_ARRAYS = "MultipleExtRefArrays";

    private final String addSimonArray;
    private final String addSimonArrayResults;
    private final String simonInstanceArrayInstance;
    private final String addSimonArrayInstanceResult;
    private final String viewSimonArray;
    private final String viewSimonArrayResults;

    private final String addTwoArrays;
    private final String addTwoArraysResults;
    private final String addTwoArraysInstance;
    private final String addTwoArraysInstanceResult;

    public ExternalReferenceArrayTest() {
        addSimonArray = loadFromFile("graphql/external_reference/add_simon_array.txt");
        addSimonArrayResults = loadFromFile("graphql/external_reference/add_simon_array_results.txt");
        simonInstanceArrayInstance = loadFromFile("graphql/external_reference/add_simon_array_instance.txt");
        addSimonArrayInstanceResult = loadFromFile("graphql/external_reference/add_simon_array_instance_results.txt");
        viewSimonArray = loadFromFile("graphql/external_reference/view_simon_array.txt");
        viewSimonArrayResults = loadFromFile("graphql/external_reference/view_simon_array_results.txt");


        addTwoArrays = loadFromFile("graphql/external_reference/add_two_arrays.txt");
        addTwoArraysResults = loadFromFile("graphql/external_reference/add_two_arrays_results.txt");
        addTwoArraysInstance = loadFromFile("graphql/external_reference/add_two_arrays_instance.txt");
        addTwoArraysInstanceResult = loadFromFile("graphql/external_reference/add_two_arrays_instance_results.txt");
    }

    public void resolveArrayOfImages() {
        // Add an external reference
        GraphQLResult result = upsert(SIMON, simonInstanceArrayInstance);
        assertResult(result, addSimonArrayInstanceResult);

        // Do a normal instance query
        result = upsert(SIMON, viewSimonArray);
        assertResult(result, viewSimonArrayResults);
    }

    public void moreThanOneArrayOfExternalReferencesInAGivenSchema() {
        // Add an instance with a few fields of type 'Array of External References'
        GraphQLResult result = upsert(MULTIPLE_EXT_REF_ARRAYS, addTwoArraysInstance);
        assertResult(result, addTwoArraysInstanceResult);
    }

    void loadSchema() {
        loadSchema(addSimonArray, addSimonArrayResults);
        loadSchema(addTwoArrays, addTwoArraysResults);
    }
}
