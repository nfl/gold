package com.nfl.dm.shield.dynamic.service;


import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("unused")
@Test
public class MemberConfigurationTest extends BaseBeanTest {

    private final String addSkiBobWithMemberConfiguration;
    private final String findSkiBobWithMemberConfiguration;
    private final String findSkiBobWithMemberConfigurationResult;
    private final String addSkiBobWithMemberConfigurationInDomainFields;
    private final String findSkiBobWithMemberConfigurationInDomainFields;
    private final String findSkiBobWithMemberConfigurationInDomainFieldsResult;

    @Autowired
    private GraphQLSchemaService graphQLSchemaService;

    public MemberConfigurationTest() {
        addSkiBobWithMemberConfiguration = loadFromFile("graphql/member_configuration/add_ski_bob_with_member_configuration.txt");
        findSkiBobWithMemberConfiguration = loadFromFile("graphql/member_configuration/find_ski_bob_with_member_configuration.txt");
        findSkiBobWithMemberConfigurationResult = loadFromFile("graphql/member_configuration/find_ski_bob_with_member_configuration_result.txt");

        addSkiBobWithMemberConfigurationInDomainFields = loadFromFile("graphql/member_configuration/add_ski_bob_with_member_configuration_in_domain_fields.txt");
        findSkiBobWithMemberConfigurationInDomainFields = loadFromFile("graphql/member_configuration/find_ski_bob_with_member_configuration_in_domain_fields.txt");
        findSkiBobWithMemberConfigurationInDomainFieldsResult = loadFromFile("graphql/member_configuration/find_ski_bob_with_member_configuration_in_domain_fields_result.txt");
    }

    public void createSkiBobWithMemberConfiguration_MemberConfigurationShouldBeInResponse(){
        createSkiBob(addSkiBobWithMemberConfiguration,
                "{upsertSchemaDefinition={memberConfiguration=test member configuration}}");
        GraphQLResult findSkiBobResult = graphQLSchemaService.executeQuery(findSkiBobWithMemberConfiguration, buildSchemaVariableMap(), null);
        assertTrue(findSkiBobResult.isSuccessful());
        assertEquals(findSkiBobResult.getData().toString(), findSkiBobWithMemberConfigurationResult);

    }

    public void createSkiBobWithMemberConfigurationInDomainFields_MemberConfigurationShouldBeInResponse(){
        createSkiBob(addSkiBobWithMemberConfigurationInDomainFields,
                "{upsertSchemaDefinition={domainFields=[{memberConfiguration=test member configuration domain fields}]}}");
        GraphQLResult findSkiBobResult = graphQLSchemaService.executeQuery(findSkiBobWithMemberConfigurationInDomainFields, buildSchemaVariableMap(), null);
        assertTrue(findSkiBobResult.isSuccessful());
        assertEquals(findSkiBobResult.getData().toString(), findSkiBobWithMemberConfigurationInDomainFieldsResult);

    }

    private void createSkiBob(String query, String expectedResponse){
        SchemaWriteAccess mutableAccess = buildSchemaWriteAccess();
        GraphQLResult addSkiBobResult = graphQLSchemaService.executeQuery(query, buildSchemaVariableMap(), mutableAccess);
        assertTrue(addSkiBobResult.isSuccessful());
        assertEquals(addSkiBobResult.getData().toString(), expectedResponse);
    }
}
