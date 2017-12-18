package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import graphql.ErrorType;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class GraphQLSchemaRelationTest extends BaseBeanTest {

    private final String addElementSchemaOne;
    private final String addElementSchemaTwo;
    private final String addElementSchemaThree;
    private final String withAnotherDynamicDomainReference;
    private final String withArrayOfAnotherDynamicDomainReferences;
    private final String withArrayOfMultiTypeReferences;
    private final String withMultiTypeDynamicReference;
    private final String withMixedDirectRelationReferences;
    private final String viewElements;
    private final String withArrayOfMultiTypeReferencesResult;
    private final String withAnotherDynamicDomainReferenceResult;
    private final String withMultiTypeDynamicReferenceResult;
    private final String withArrayOfAnotherDynamicDomainReferencesResult;
    private final String withMixedDirectRelationReferencesResult;
    private final String withArraysOrPrimitives;
    private final String withArraysOrPrimitivesResult;
    private final String withAnotherInstanceReferenceOne;
    private final String withAnotherInstanceReferenceTwo;
    private final String withAnotherInstanceReferenceOneResult;
    private final String withTwoTypesWithAnotherInstanceReferenceResult;

    private GraphQLResult actualGraphQLResult;

    public GraphQLSchemaRelationTest() {
        addElementSchemaOne = loadFromFile("graphql/relatedBy/init/addElementSchemaOne.graphql");
        addElementSchemaTwo = loadFromFile("graphql/relatedBy/init/addElementSchemaTwo.graphql");
        addElementSchemaThree = loadFromFile("graphql/relatedBy/init/addElementSchemaThree.graphql");
        withAnotherDynamicDomainReference = loadFromFile("graphql/relatedBy/case/with_another_dynamic_domain_reference.graphql");
        withArrayOfAnotherDynamicDomainReferences = loadFromFile("graphql/relatedBy/case/with_array_of_another_dynamic_domain_references.graphql");
        withArrayOfMultiTypeReferences = loadFromFile("graphql/relatedBy/case/with_array_of_multi_type_references.graphql");
        withMultiTypeDynamicReference = loadFromFile("graphql/relatedBy/case/with_multi_type_dynamic_reference.graphql");
        withMixedDirectRelationReferences = loadFromFile("graphql/relatedBy/case/with_all_cases.graphql");
        viewElements = loadFromFile("graphql/relatedBy/view_with_refes.graphql");
        withArrayOfMultiTypeReferencesResult = loadFromFile("graphql/relatedBy/result/with_array_of_multi_type_references.txt");
        withAnotherDynamicDomainReferenceResult = loadFromFile("graphql/relatedBy/result/with_another_dynamic_domain_reference.txt");
        withMultiTypeDynamicReferenceResult = loadFromFile("graphql/relatedBy/result/with_multi_type_dynamic_reference.txt");
        withArrayOfAnotherDynamicDomainReferencesResult = loadFromFile("graphql/relatedBy/result/with_array_of_another_dynamic_domain_references.txt");
        withMixedDirectRelationReferencesResult = loadFromFile("graphql/relatedBy/result/with_all_cases.txt");
        withArraysOrPrimitives = loadFromFile("graphql/relatedBy/case/with_arrays_or_primitives.graphql");
        withArraysOrPrimitivesResult = loadFromFile("graphql/relatedBy/result/with_arrays_or_primitives.txt");
        withAnotherInstanceReferenceOne = loadFromFile("graphql/relatedBy/case/with_another_instance_reference_one.graphql");
        withAnotherInstanceReferenceOneResult = loadFromFile("graphql/relatedBy/result/with_another_instance_reference_one.txt");
        withAnotherInstanceReferenceTwo = loadFromFile("graphql/relatedBy/case/with_another_instance_reference_two.graphql");
        withTwoTypesWithAnotherInstanceReferenceResult =
                loadFromFile("graphql/relatedBy/result/with_two_types_with_another_instance_reference.txt");
    }

    @BeforeMethod
    public void setUp() throws Exception {
        executeSchemaQuery(addElementSchemaOne);
        executeSchemaQuery(addElementSchemaTwo);
        executeSchemaQuery(addElementSchemaThree);
    }

    @Test
    public void findRelation_withAnotherDynamicDomainReference_successCase() throws Exception {
        actualGraphQLResult = executeSchemaQuery(withAnotherDynamicDomainReference);
        assertSuccess(actualGraphQLResult);

        actualGraphQLResult = executeSchemaQuery(viewElements);
        assertResult(actualGraphQLResult, withAnotherDynamicDomainReferenceResult);
    }

    @Test
    public void findRelation_withAnotherInstanceReference_successCase() throws Exception {
        actualGraphQLResult = executeSchemaQuery(withAnotherInstanceReferenceOne);
        assertSuccess(actualGraphQLResult);

        actualGraphQLResult = executeSchemaQuery(viewElements);
        assertResult(actualGraphQLResult, withAnotherInstanceReferenceOneResult);
    }

    @Test
    public void findRelation_withTwoTypesWithAnotherInstanceReference_successCase() throws Exception {
        actualGraphQLResult = executeSchemaQuery(withAnotherInstanceReferenceOne);
        assertSuccess(actualGraphQLResult);

        actualGraphQLResult = executeSchemaQuery(withAnotherInstanceReferenceTwo);
        assertSuccess(actualGraphQLResult);

        actualGraphQLResult = executeSchemaQuery(viewElements);
        // Each type only is referred by itself
        assertResult(actualGraphQLResult, withTwoTypesWithAnotherInstanceReferenceResult);
    }

    @Test
    public void findRelation_withArrayOfAnotherDynamicDomainReferences_successCase() throws Exception {
        actualGraphQLResult = executeSchemaQuery(withArrayOfAnotherDynamicDomainReferences);
        assertSuccess(actualGraphQLResult);

        actualGraphQLResult = executeSchemaQuery(viewElements);
        assertResult(actualGraphQLResult, withArrayOfAnotherDynamicDomainReferencesResult);
    }

    @Test
    public void findRelation_withMultiTypeDynamicReference_successCase() throws Exception {
        actualGraphQLResult = executeSchemaQuery(withMultiTypeDynamicReference);
        assertSuccess(actualGraphQLResult);

        actualGraphQLResult = executeSchemaQuery(viewElements);
        assertResult(actualGraphQLResult, withMultiTypeDynamicReferenceResult);
    }

    @Test
    public void findRelation_withArrayOfMultiTypeReferences_successCase() throws Exception {
        actualGraphQLResult = executeSchemaQuery(withArrayOfMultiTypeReferences);
        assertSuccess(actualGraphQLResult);

        actualGraphQLResult = executeSchemaQuery(viewElements);
        assertResult(actualGraphQLResult, withArrayOfMultiTypeReferencesResult);
    }

    @Test
    public void findRelation_withMixedDirectRelationReferences_successCase() throws Exception {
        actualGraphQLResult = executeSchemaQuery(withMixedDirectRelationReferences);
        assertSuccess(actualGraphQLResult);

        actualGraphQLResult = executeSchemaQuery(viewElements);
        assertResult(actualGraphQLResult, withMixedDirectRelationReferencesResult);
    }

    @Test
    public void findDirectRelatedSchemas_withMixedSchemas_successCase() throws Exception {
        actualGraphQLResult = executeSchemaQuery(withAnotherDynamicDomainReference);
        assertSuccess(actualGraphQLResult);
        actualGraphQLResult = executeSchemaQuery(withArrayOfAnotherDynamicDomainReferences);
        assertSuccess(actualGraphQLResult);
        actualGraphQLResult = executeSchemaQuery(withMultiTypeDynamicReference);
        assertSuccess(actualGraphQLResult);
        actualGraphQLResult = executeSchemaQuery(withArrayOfMultiTypeReferences);
        assertSuccess(actualGraphQLResult);
        actualGraphQLResult = executeSchemaQuery(withMixedDirectRelationReferences);
        assertSuccess(actualGraphQLResult);

        calculateAndAssertRelatedSchemasCount("Element1", 5);
        calculateAndAssertRelatedSchemasCount("Element2", 3);
        calculateAndAssertRelatedSchemasCount("Element3", 3);

        calculateAndAssertRelatedSchemasCount("WithArrayOfMultiTypeReferences", 0);
        calculateAndAssertRelatedSchemasCount("WithAnotherDynamicDomainReferenceType", 0);
        calculateAndAssertRelatedSchemasCount("WithMultiTypeDynamicReference", 0);
        calculateAndAssertRelatedSchemasCount("WithAllPossibleDirectRelationLinks", 0);
        calculateAndAssertRelatedSchemasCount("WithArrayOfAnotherDynamicDomainReferences", 0);
    }

    @Test
    public void deleteSchema_withDirectRelation_causedError() throws Exception {
        actualGraphQLResult = executeSchemaQuery(withAnotherDynamicDomainReference);
        assertSuccess(actualGraphQLResult);
        actualGraphQLResult = executeSchemaQuery(withMultiTypeDynamicReference);
        assertSuccess(actualGraphQLResult);

        actualGraphQLResult = executeSchemaQuery("mutation {removeSchemaDefinition(name:\"Element1\"){name}}");
        assertFalse(actualGraphQLResult.isSuccessful());

        assertEquals(actualGraphQLResult.getErrors().get(0).getErrorType(), ErrorType.DataFetchingException);
        assertTrue(actualGraphQLResult.getErrors().get(0).getMessage().contains("WithAnotherDynamicDomainReferenceType"));
        assertTrue(actualGraphQLResult.getErrors().get(0).getMessage().contains("WithMultiTypeDynamicReference"));
    }

    @Test
    public void findDirectRelation_withArraysOrPrimitives_primitivesShouldNotProcess() throws Exception {
        actualGraphQLResult = executeSchemaQuery(withArraysOrPrimitives);
        assertSuccess(actualGraphQLResult);

        actualGraphQLResult = executeSchemaQuery(viewElements);
        assertResult(actualGraphQLResult, withArraysOrPrimitivesResult);
    }

    private void calculateAndAssertRelatedSchemasCount(String name, int count) {
        List<SchemaDescription> result = schemaService.findDirectRelatedSchemas(buildSchemaDescriptionForSearch(name));
        assertNotNull(result);
        assertEquals(result.size(), count);
    }

    private SchemaDescription buildSchemaDescriptionForSearch(String name) {
        SchemaDescription schemaDescription = new SchemaDescription();
        schemaDescription.setName(name);
        schemaDescription.setNamespace(SCHEMA_NAME_SPACE);
        return schemaDescription;
    }

    @AfterMethod
    public void tearDown() {
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}