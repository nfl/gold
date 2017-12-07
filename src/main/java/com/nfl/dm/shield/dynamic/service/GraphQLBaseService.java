package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.exception.UnauthorizedException;
import graphql.ErrorType;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class GraphQLBaseService {

    GraphQLResult buildResult(final ExecutionResult executionResult) {
        return new GraphQLResult() {
            @Override
            public boolean isSuccessful() {
                return executionResult.getErrors().isEmpty();
            }

            @SuppressWarnings("unchecked")
            @Override
            public Object getData() {
                return executionResult.getData();
            }

            @Override
            public List<GraphQLError> getErrors() {

                Optional<GraphQLError> unauthorizedErrors = executionResult.getErrors().stream()
                        .filter(error -> ErrorType.DataFetchingException.equals(error.getErrorType()))
                        .filter(error -> error.getMessage().contains(UnauthorizedException.class.getSimpleName()))
                        .findAny();

                if (unauthorizedErrors.isPresent()) {
                    GraphQLError error = unauthorizedErrors.get();
                    throw new UnauthorizedException(error.getMessage());
                }

                return executionResult.getErrors();
            }

            @Override
            public Map<Object, Object> getExtensions() {
                return executionResult.getExtensions();
            }

            @Override
            public Map<String, Object> toSpecification() {
                return executionResult.toSpecification();
            }

        };
    }

    GraphQLResult buildErrorResult(final String message) {
        return new GraphQLResult() {
            @Override
            public boolean isSuccessful() {
                return false;
            }

            @SuppressWarnings("unchecked")
            @Override
            public Object getData() {
                return null;
            }

            @Override
            public List<GraphQLError> getErrors() {
                return Collections.singletonList(new GraphQLError() {
                    @Override
                    public String getMessage() {
                        return message;
                    }

                    @Override
                    public List<SourceLocation> getLocations() {
                        return Collections.emptyList();
                    }

                    @Override
                    public ErrorType getErrorType() {
                        return ErrorType.ValidationError;
                    }
                });
            }

            @Override
            public Map<Object, Object> getExtensions() {
                return Collections.emptyMap();
            }

            @Override
            public Map<String, Object> toSpecification() {
                return Collections.emptyMap();
            }

        };
    }
}
