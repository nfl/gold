package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.EnumValueDef.NAME_FIELD;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.EnumValueDef.VALUE_FIELD;
import static graphql.schema.GraphQLEnumType.newEnum;

public class EnumType extends SchemaInstanceField {

    // For Testing Only
    @SuppressWarnings("unused")
    EnumType() {
    }

    EnumType(SchemaDescription parent, Map<String, Object> initValues) {
        super(InstanceFieldType.ENUM, initValues, parent);

        loadEnumValues(initValues);
        validateEnumValues();
    }

    EnumType(SchemaDescription parent, SchemaInstanceField baseField) {
        super(parent, baseField);
        setEnumValues(baseField.getEnumValues());
    }

    private void loadEnumValues(Map<String, Object> initValues) {
        if (!initValues.containsKey(ENUM_VALUES_FIELD)) {
            throw new IllegalArgumentException("An enum type must contain values for the ENUM.");
        }

        //noinspection unchecked
        List<Map<String, String>> inputValues = (List<Map<String, String>>) initValues.get(ENUM_VALUES_FIELD);
        setEnumValues(inputValues.stream()
                .map(enumEntry -> new EnumValueDef(enumEntry.get(NAME_FIELD), enumEntry.get(VALUE_FIELD)))
                .collect(Collectors.toList()));
    }

    private void validateEnumValues() {
        if (getEnumValues().isEmpty()) {
            throw new IllegalStateException("Must specify values for an enum");
        }
    }

    @Override
    public GraphQLOutputType buildInstanceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext, InstanceOutputTypeService instanceOutputTypeService) {
        return buildEnumType("Output");
    }

    @Override
    public GraphQLInputType buildInstanceInputType(InstanceFieldBuilderContext instanceFieldBuilderContext) {
        return buildEnumType("Input");
    }

    private GraphQLEnumType buildEnumType(String postFix) {
        GraphQLEnumType.Builder builder = newEnum().name(getMemberFieldName() + postFix);
        getEnumValues().forEach(entry -> builder.value(entry.getName(), entry.getValue()));
        return builder.build();
    }
}
