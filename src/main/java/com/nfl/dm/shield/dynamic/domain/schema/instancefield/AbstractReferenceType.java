package com.nfl.dm.shield.dynamic.domain.schema.instancefield;


import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.*;

import java.util.List;
import java.util.Map;

import static graphql.schema.GraphQLUnionType.newUnionType;

public abstract class AbstractReferenceType extends SchemaInstanceField {

    public static final String REFERENCE_ID = "id";
    public static final String REFERENCE_TYPE = "typeDefinition";
    public static final String TYPE_NAME_DECORATOR = "dynamic-type-name";

    AbstractReferenceType(InstanceFieldType type, Map<String, Object> initValues, SchemaDescription parent) {
        super(type, initValues, parent);
    }

    AbstractReferenceType(SchemaDescription parent, SchemaInstanceField baseField) {
        super(parent, baseField);
    }

    GraphQLOutputType createReferenceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext, InstanceOutputTypeService instanceOutputTypeService, String unionTypeName) {
        Map<String, GraphQLObjectType> outputTypesMap = buildPossibleTypeMap(getPossibleTypes(), instanceFieldBuilderContext, instanceOutputTypeService);
        GraphQLUnionType.Builder unionBuilder = newUnionType().name(unionTypeName);
        outputTypesMap.values().forEach(unionBuilder::possibleType);
        return unionBuilder.typeResolver(buildTypeResolver(outputTypesMap)).build();
    }

    private TypeResolver buildTypeResolver(Map<String, GraphQLObjectType> outputTypes) {
        return object -> {
            @SuppressWarnings("unchecked")
            String typeName = ((Map<String, Object>) object.getObject()).get(TYPE_NAME_DECORATOR).toString();
            return outputTypes.get(typeName);
        };
    }

    abstract Map<String, GraphQLObjectType> buildPossibleTypeMap(List<String> possibleTypes,
                                                                 InstanceFieldBuilderContext instanceFieldBuilderContext, InstanceOutputTypeService instanceOutputTypeService);
}
