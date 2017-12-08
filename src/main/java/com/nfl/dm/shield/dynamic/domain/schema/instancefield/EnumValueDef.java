package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import graphql.schema.*;

import java.util.LinkedList;
import java.util.List;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static java.util.Collections.emptyList;

@SuppressWarnings("WeakerAccess")
public class EnumValueDef {

    private static final String OUR_DESCRIPTION = "Enum Value Definition";
    public static final String NAME_FIELD = "name";
    public static final String VALUE_FIELD = "value";

    private String name;

    private String value;

    public EnumValueDef() {
    }

    public EnumValueDef(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    static GraphQLInputType buildSchemaInputType() {
        List<GraphQLInputObjectField> enumDescription = new LinkedList<>();

        GraphQLInputObjectField fieldName = newInputObjectField()
                .type(new GraphQLNonNull(GraphQLString))
                .name(NAME_FIELD)
                .build();
        enumDescription.add(fieldName);

        fieldName = newInputObjectField()
                .type(new GraphQLNonNull(GraphQLString))
                .name(VALUE_FIELD)
                .build();
        enumDescription.add(fieldName);

        return new GraphQLInputObjectType("enumInput", OUR_DESCRIPTION, enumDescription);
    }

    static GraphQLObjectType buildSchemaOutputType() {
        List<GraphQLFieldDefinition> valueDefFields = new LinkedList<>();
        GraphQLFieldDefinition nameField = newFieldDefinition()
                .type(GraphQLString)
                .name(NAME_FIELD)
                .build();
        valueDefFields.add(nameField);

        GraphQLFieldDefinition valueField = newFieldDefinition()
                .type(GraphQLString)
                .name(VALUE_FIELD)
                .build();
        valueDefFields.add(valueField);
        return new GraphQLObjectType("enumDisplay", OUR_DESCRIPTION, valueDefFields, emptyList());
    }
}
