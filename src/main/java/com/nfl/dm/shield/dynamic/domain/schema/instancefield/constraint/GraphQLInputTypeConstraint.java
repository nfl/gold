package com.nfl.dm.shield.dynamic.domain.schema.instancefield.constraint;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLNonNull;

import java.util.function.Function;

import static graphql.schema.GraphQLEnumType.newEnum;

public enum GraphQLInputTypeConstraint {

    @SuppressWarnings("unused")
    REQUIRED(GraphQLNonNull::new),

    NONE(graphQLType -> graphQLType);

    private final Function<GraphQLInputType, GraphQLInputType> graphQLWrapFunction;

    private static final GraphQLEnumType graphQLEnumType = createEnumType();

    GraphQLInputTypeConstraint(Function<GraphQLInputType, GraphQLInputType> graphQLWrapFunction) {
        this.graphQLWrapFunction = graphQLWrapFunction;
    }

    public Function<GraphQLInputType, GraphQLInputType> getGraphQLWrapFunction() {
        return graphQLWrapFunction;
    }

    public static GraphQLEnumType getEnumType() {
        return graphQLEnumType;
    }

    private static GraphQLEnumType createEnumType() {
        GraphQLEnumType.Builder builder = newEnum().name(Constraint.CONSTRAINT_FIELD);

        for (GraphQLInputTypeConstraint value : GraphQLInputTypeConstraint.values()) {
            builder.value(value.name(), value);
        }

        return builder.build();
    }

}
