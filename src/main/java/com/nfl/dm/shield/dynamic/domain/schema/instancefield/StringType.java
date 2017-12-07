package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;

import java.util.Map;

import static graphql.Scalars.GraphQLString;

public class StringType extends SchemaInstanceField {

    // For Testing Only
    @SuppressWarnings("unused")
    StringType() {}

    StringType(SchemaDescription parent, Map<String, Object> initValues) {
        super(InstanceFieldType.STRING, initValues, parent);
    }

    StringType(SchemaDescription parent, SchemaInstanceField baseField) {
        super(parent, baseField);
    }

    @Override
    public GraphQLOutputType buildInstanceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                     InstanceOutputTypeService instanceOutputTypeService) {
        return GraphQLString;
    }

    @Override
    public GraphQLInputType buildInstanceInputType(InstanceFieldBuilderContext instanceFieldBuilderContext) {
        return GraphQLString;
    }
}