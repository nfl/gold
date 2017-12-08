package com.nfl.dm.shield.dynamic.domain.instance;

import com.nfl.dm.shield.dynamic.domain.BaseKey;
import graphql.schema.*;

import java.util.LinkedList;
import java.util.List;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static java.util.Collections.emptyList;

public class SchemaInstanceKey extends BaseKey {

    private static final String OUR_DESCRIPTION = "SchemaInstanceKey Definition";
    private static final String LABEL_FIELD = "label";
    private static final String INSTANCE_NAME_SPACE = "instanceNamespace";

    private static final GraphQLObjectType schemaInstanceKeyOutputType = buildInitialSchemaInstanceKeyOutputType();
    private static final GraphQLInputType schemaInstanceKeyInputType = buildInitialSchemaInstanceKeyInputType();

    private final String label;
    private final String instanceNamespace;

    public SchemaInstanceKey(String schemaName, String schemaNamespace, String instanceNamespace) {
        super(schemaName, schemaNamespace);
        this.instanceNamespace = instanceNamespace;
        label = "PUBLISHED";
    }

    public SchemaInstanceKey(String schemaName, String schemaNamespace, String instanceNamespace, String label) {
        super(schemaName, schemaNamespace);
        this.instanceNamespace = instanceNamespace;
        this.label = label;
    }

    public String getInstanceNamespace() {
        return instanceNamespace;
    }

    @SuppressWarnings("WeakerAccess")
    public String getLabel() {
        return label;
    }

    private static GraphQLObjectType buildInitialSchemaInstanceKeyOutputType() {
        List<GraphQLFieldDefinition> schemaKeyDefinition = new LinkedList<>();

        GraphQLFieldDefinition schemaNamespaceFieldField = newFieldDefinition()
                .type(GraphQLString)
                .name(SCHEMA_NAMESPACE_FIELD)
                .build();
        schemaKeyDefinition.add(schemaNamespaceFieldField);

        GraphQLFieldDefinition schemaNameField = newFieldDefinition()
                .type(GraphQLString)
                .name(SCHEMA_NAME_FIELD)
                .build();
        schemaKeyDefinition.add(schemaNameField);

        GraphQLFieldDefinition instanceNamespaceField = newFieldDefinition()
                .type(GraphQLString)
                .name(INSTANCE_NAME_SPACE)
                .build();
        schemaKeyDefinition.add(instanceNamespaceField);

        GraphQLFieldDefinition labelField = newFieldDefinition()
                .type(GraphQLString)
                .name(LABEL_FIELD)
                .build();
        schemaKeyDefinition.add(labelField);

        return new GraphQLObjectType("SchemaInstanceKeyOutput", OUR_DESCRIPTION, schemaKeyDefinition, emptyList());
    }

    private static GraphQLInputType buildInitialSchemaInstanceKeyInputType() {
        List<GraphQLInputObjectField> schemaKeyDefinition = new LinkedList<>();

        GraphQLInputObjectField schemaNamespaceFieldField = newInputObjectField()
                .type(GraphQLString)
                .name(SCHEMA_NAMESPACE_FIELD)
                .build();
        schemaKeyDefinition.add(schemaNamespaceFieldField);

        GraphQLInputObjectField schemaNameField = newInputObjectField()
                .type(GraphQLString)
                .name(SCHEMA_NAME_FIELD)
                .build();
        schemaKeyDefinition.add(schemaNameField);

        GraphQLInputObjectField instanceNamespaceField = newInputObjectField()
                .type(GraphQLString)
                .name(INSTANCE_NAME_SPACE)
                .build();
        schemaKeyDefinition.add(instanceNamespaceField);

        GraphQLInputObjectField labelField = newInputObjectField()
                .type(GraphQLString)
                .name(LABEL_FIELD)
                .build();
        schemaKeyDefinition.add(labelField);

        return new GraphQLInputObjectType("SchemaInstanceKeyInput", OUR_DESCRIPTION, schemaKeyDefinition);
    }


    public static GraphQLObjectType buildSchemaOutputType() {
        return schemaInstanceKeyOutputType;
    }

    public static GraphQLInputType buildSchemaInputType() { return schemaInstanceKeyInputType; }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof SchemaInstanceKey))
            return false;

        SchemaInstanceKey that = (SchemaInstanceKey) other;

        return super.equals(that) &&
                label.equals(that.getLabel()) &&
                instanceNamespace.equals(that.getInstanceNamespace());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + label.hashCode();
        result = 31 * result + instanceNamespace.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "{" +
                "schemaName='" + getSchemaName() + '\'' +
                ", schemaNamespace='" + getSchemaNamespace() + '\'' +
                ", label='" + label + '\'' +
                ", instanceNamespace='" + instanceNamespace + '\'' +
                "}";
    }

    public SchemaInstanceKey getChildSchemaInstanceKey(String childSchemaName) {
        return new SchemaInstanceKey(childSchemaName, getSchemaNamespace(), getInstanceNamespace());
    }
}
