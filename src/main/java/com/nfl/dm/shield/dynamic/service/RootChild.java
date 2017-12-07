package com.nfl.dm.shield.dynamic.service;

import graphql.schema.GraphQLFieldDefinition;

import java.util.List;

public interface RootChild {

    String childFieldName();

    GraphQLFieldDefinition buildField();

    List<GraphQLFieldDefinition> buildMutators();
}
