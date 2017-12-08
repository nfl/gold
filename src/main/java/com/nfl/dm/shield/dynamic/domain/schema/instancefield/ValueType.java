package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.ValueObjectField;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class ValueType extends SchemaInstanceField {

    public ValueType(SchemaDescription parent, Map<String, Object> initValues) {
        super(InstanceFieldType.VALUE_OBJECT, initValues, parent);

        loadAndValidateOtherName(initValues);
    }

    public ValueType(SchemaDescription parent, SchemaInstanceField baseField) {
        super(parent, baseField);
        setOtherTypeName(baseField.getOtherTypeName());
    }

    private void loadAndValidateOtherName(Map<String, Object> initValues) {
        if (!initValues.containsKey(OTHER_TYPE_NAME_FIELD)) {
            throw new IllegalArgumentException("Missing other type name.");
        }
        String valueTypeName = initValues.get(OTHER_TYPE_NAME_FIELD).toString();

        if (valueTypeName.isEmpty()) {
            throw new IllegalArgumentException("Empty valueTypeName.");
        }

        if (!getParent().hasValueType(valueTypeName)) {
            throw new IllegalStateException("Missing other type name: " + valueTypeName);
        }

        setOtherTypeName(valueTypeName);
    }

    @Override
    public GraphQLOutputType buildInstanceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext, InstanceOutputTypeService instanceOutputTypeService) {
        SchemaDescription holderSchema = getParent();
        ValueObjectField vof = holderSchema.findValueType(getOtherTypeName());
        List<GraphQLFieldDefinition> valueFields = vof.getValueFields().stream()
                .filter(instanceFieldBuilderContext.getFieldFilter())
                .map(instanceDef -> instanceDef.buildGraphOutputField(instanceFieldBuilderContext, instanceOutputTypeService))
                .collect(Collectors.toList());

        return new GraphQLObjectType(vof.getName(), "Value Object Instance",
                valueFields, emptyList());
    }

    @Override
    public GraphQLInputType buildInstanceInputType(InstanceFieldBuilderContext instanceFieldBuilderContext) {


        SchemaDescription holderSchema = getParent();
        ValueObjectField vof = holderSchema.findValueType(getOtherTypeName());
        List<GraphQLInputObjectField> valueFields = vof.getValueFields().stream()
                .map(instanceDef -> instanceDef.buildGraphInputField(instanceFieldBuilderContext))
                .collect(Collectors.toList());

        return new GraphQLInputObjectType(getOtherTypeName() + "ValueInput", "Value Object Input Instance", valueFields);
    }
}