package com.nfl.dm.shield.dynamic.repository;

import com.nfl.graphql.mediator.GraphQLMediator;
import graphql.language.SelectionSet;

import java.util.Map;

public interface ExternalReferenceRepository {

    GraphQLMediator buildMediator(String authHeader);

    Map<String, Object> findById(SelectionSet selections, Map<String, String> id, String authHeader);
}
