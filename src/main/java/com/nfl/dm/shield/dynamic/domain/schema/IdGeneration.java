package com.nfl.dm.shield.dynamic.domain.schema;


import graphql.schema.GraphQLEnumType;

import static graphql.schema.GraphQLEnumType.newEnum;

public enum IdGeneration {
    CLIENT_SPECIFIED("Client"), SERVICE_GENERATED_GUID("ServiceGeneratedGUID");

    private static final GraphQLEnumType graphQLEnumType;
    private String display;

    IdGeneration(String display) {
        this.display = display;
    }

    private String getDisplay() {
        return display;
    }

    static {
        GraphQLEnumType.Builder builder = newEnum().name("IdGeneration");

        for (IdGeneration value : IdGeneration.values()) {
            builder.value(value.getDisplay(), value.name());
        }

        graphQLEnumType = builder.build();

    }
    public static GraphQLEnumType createEnumType() {
        return graphQLEnumType;
    }
}
