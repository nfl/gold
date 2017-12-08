package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;

import java.util.Map;

import static graphql.Scalars.GraphQLInt;

public class IntegerType extends SchemaInstanceField {

    // For Testing Only
    @SuppressWarnings("unused")
    IntegerType() {
    }

    IntegerType(SchemaDescription parent, Map<String, Object> initValues) {
        super(InstanceFieldType.INTEGER, initValues, parent);
    }

    IntegerType(SchemaDescription parent, SchemaInstanceField baseField) {
        super(parent, baseField);
    }

    @Override
    public GraphQLOutputType buildInstanceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                     InstanceOutputTypeService instanceOutputTypeService) {
        return GraphQLInt;
    }

    @Override
    public GraphQLInputType buildInstanceInputType(InstanceFieldBuilderContext instanceFieldBuilderContext) {
        return GraphQLInt;
    }
}
