package com.nfl.dm.shield.dynamic.repository;

import graphql.language.SelectionSet;
import graphql.schema.GraphQLOutputType;

import java.util.Map;

public interface ExternalReferenceRepository {

    GraphQLOutputType deriveFromExternalTypeName(String typeName, String authHeader);

    Map<String, Object> findById(SelectionSet selections, Map<String, String> id, String authHeader);
}
