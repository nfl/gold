package com.nfl.dm.shield.dynamic.service;

import graphql.ExecutionResult;

public interface GraphQLResult extends ExecutionResult {

    boolean isSuccessful();
}
