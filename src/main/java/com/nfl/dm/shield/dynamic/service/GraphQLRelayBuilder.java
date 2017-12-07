package com.nfl.dm.shield.dynamic.service;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Service
public class GraphQLRelayBuilder {

    public GraphQL buildGraphQL(List<RootChild> rootChildren) {

        GraphQLObjectType rootType = buildRootType(rootChildren);

        GraphQLFieldDefinition rootField = newFieldDefinition().type(rootType)
                .name("viewer")
                .dataFetcher(buildViewerFetcher(rootChildren))
                .build();

        GraphQLObjectType queryType = new GraphQLObjectType("Viewer", "Top Level Viewer For Relay",
                singletonList(rootField), emptyList());

        GraphQLObjectType mutationType = buildMutation(rootChildren);

        GraphQLSchema.Builder retSchema = GraphQLSchema.newSchema().query(queryType).mutation(mutationType);

        return GraphQL.newGraphQL(retSchema.build()).build();
    }

    private GraphQLObjectType buildMutation(List<RootChild> rootChildren) {

        GraphQLObjectType.Builder mutationBuilder = newObject()
                .name("mutationType")
                .description("Defines all the allowed mutation operations");

        rootChildren.forEach(child -> mutationBuilder.fields(child.buildMutators()));

        return mutationBuilder.build();
    }


    private DataFetcher buildViewerFetcher(List<RootChild> rootChildren) {
        return environment -> {
            Map<String, String> rootViewMap = new HashMap<>();
            Map<String, Object> retMap = new HashMap<>();
            rootChildren.forEach(child -> retMap.put(child.childFieldName(), singletonList(rootViewMap)));
            return retMap;
        };
    }

    private GraphQLObjectType buildRootType(List<RootChild> rootChildren) {

        return new GraphQLObjectType("RelayRootType", "Root Node Target",
                rootChildren.stream().map(RootChild::buildField).collect(Collectors.toList()), emptyList());
    }
}