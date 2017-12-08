package com.nfl.dm.shield.dynamic.config;

import com.nfl.dm.shield.dynamic.service.GraphQLRelayBuilder;
import com.nfl.dm.shield.dynamic.service.RootChild;
import com.nfl.dm.shield.dynamic.service.SchemaGraphQLBuilder;
import graphql.GraphQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@Configuration
public class GraphQLConfig {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SchemaGraphQLBuilder schemaGraphQLBuilderService;

    @Autowired
    private GraphQLRelayBuilder relayGraphQLBuilder;

    @Bean
    @Qualifier("schema")
    public GraphQL graphQL() {
        List<RootChild> serviceList = new ArrayList<>();
        serviceList.add(schemaGraphQLBuilderService);
        // add other services here (originally the version policy idea.
        return relayGraphQLBuilder.buildGraphQL(serviceList);
    }
}
