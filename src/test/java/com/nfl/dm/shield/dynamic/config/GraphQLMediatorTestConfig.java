package com.nfl.dm.shield.dynamic.config;

import com.nfl.dm.shield.dynamic.BaseBeanTest;
import com.nfl.graphql.mediator.GraphQLMediator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphQLMediatorTestConfig extends BaseBeanTest {

    @Bean
    public GraphQLMediator mediatorFactory() {
        return new GraphQLMediator(loadFromFile("schema_example.json"));
    }
}
