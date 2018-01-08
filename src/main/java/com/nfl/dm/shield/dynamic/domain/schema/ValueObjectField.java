package com.nfl.dm.shield.dynamic.domain.schema;


import com.nfl.dm.shield.dynamic.domain.schema.instancefield.SchemaInstanceField;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.nfl.dm.shield.dynamic.config.HashConfig.DEFAULT_HASH_TABLE_SIZE;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static java.util.Collections.emptyList;

@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class ValueObjectField {

    private static final String OUR_DESCRIPTION = "Value Object Description";
    private static final String NAME_FIELD = "name";
    private static final String VALUE_FIELDS = "valueFields";

    private String name;

    private List<SchemaInstanceField> valueFields = new ArrayList<>(DEFAULT_HASH_TABLE_SIZE);

    public ValueObjectField() {
        // For persistence
    }

    public ValueObjectField(SchemaDescription parent, Map<String, Object> initValues, InstanceOutputTypeService instanceOutputTypeService) {
        initFields(parent, initValues, instanceOutputTypeService);
    }

    private void initFields(SchemaDescription parent, Map<String, Object> initValues, InstanceOutputTypeService instanceOutputTypeService) {
        for (String keyValue : initValues.keySet()) {
            initValue(parent, keyValue, initValues.get(keyValue), instanceOutputTypeService);
        }
    }

    private void initValue(SchemaDescription parent, String keyValue, Object initObj, InstanceOutputTypeService instanceOutputTypeService) {
        switch (keyValue) {
            case NAME_FIELD:
                name = initObj.toString();
                break;

            case VALUE_FIELDS:
                //noinspection unchecked
                valueFields = parent.buildSchemaInstanceFields((List<Map<String, Object>>)initObj, instanceOutputTypeService);
                break;

            default:
                throw new IllegalStateException("Unknown key value:" + keyValue);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SchemaInstanceField> getValueFields() {
        return valueFields;
    }

    public void setValueFields(List<SchemaInstanceField> valueFields) {
        this.valueFields = valueFields;
    }

    public static GraphQLInputType buildSchemaInputType() {
        List<GraphQLInputObjectField> valueDescription = new LinkedList<>();
        GraphQLInputObjectField fieldName = newInputObjectField()
                .type(new GraphQLNonNull(GraphQLString))
                .name(NAME_FIELD)
                .build();
        valueDescription.add(fieldName);

        GraphQLInputType instanceType = SchemaInstanceField.buildSchemaInputType();
        GraphQLInputObjectField fieldInstance = newInputObjectField()
                .type(new GraphQLList(instanceType))
                .name(VALUE_FIELDS)
                .build();
        valueDescription.add(fieldInstance);

        return new GraphQLInputObjectType("valueInputObject", OUR_DESCRIPTION, valueDescription);
    }

    public static GraphQLObjectType buildSchemaOutputType() {
        List<GraphQLFieldDefinition> valueDefFields = new LinkedList<>();
        GraphQLFieldDefinition nameField = newFieldDefinition()
                .type(GraphQLString)
                .name(NAME_FIELD)
                .build();
        valueDefFields.add(nameField);

        GraphQLObjectType instanceType = SchemaInstanceField.buildSchemaOutputType();
        GraphQLFieldDefinition fieldInstance = newFieldDefinition()
                .type(new GraphQLList(instanceType))
                .name(VALUE_FIELDS)
                .build();
        valueDefFields.add(fieldInstance);
        return new GraphQLObjectType("valueDisplayObject", OUR_DESCRIPTION, valueDefFields, emptyList());
    }
}
