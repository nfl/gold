package com.nfl.dm.shield.dynamic.domain.schema;

import com.nfl.dm.shield.dynamic.domain.BaseKey;
import graphql.schema.*;

import java.util.LinkedList;
import java.util.List;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.util.Collections.emptyList;

public class SchemaKey extends BaseKey {

    private static final String OUR_DESCRIPTION = "SchemaKey Definition";

    public SchemaKey(String dynamicObjectName, String schemaNamespace) {
        super(dynamicObjectName, schemaNamespace);
    }

    static GraphQLObjectType buildSchemaOutputType() {
        List<GraphQLFieldDefinition> schemaKeyDefinition = new LinkedList<>();

        GraphQLFieldDefinition nameField = newFieldDefinition()
                .type(GraphQLString)
                .name(SCHEMA_NAME_FIELD)
                .build();
        schemaKeyDefinition.add(nameField);

        GraphQLFieldDefinition namespaceField = newFieldDefinition()
                .type(GraphQLString)
                .name(SCHEMA_NAMESPACE_FIELD)
                .build();
        schemaKeyDefinition.add(namespaceField);

        return new GraphQLObjectType("SchemaKeyOutput", OUR_DESCRIPTION, schemaKeyDefinition, emptyList());
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "{" +
                "schemaNamespace='" + getSchemaNamespace() + '\'' +
                ", schemaName='" + getSchemaName() + '\'' +
                '}';
    }
}
