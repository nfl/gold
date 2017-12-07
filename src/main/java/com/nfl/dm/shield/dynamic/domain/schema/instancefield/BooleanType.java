package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;

import java.util.Map;

import static graphql.Scalars.GraphQLBoolean;

public class BooleanType extends SchemaInstanceField {

    // For Testing Only
    @SuppressWarnings("unused")
    BooleanType() {
    }

    BooleanType(SchemaDescription parent, Map<String, Object> initValues) {
        super(InstanceFieldType.BOOLEAN, initValues, parent);
    }

    BooleanType(SchemaDescription parent, SchemaInstanceField baseField) {
        super(parent, baseField);
    }

    @Override
    public GraphQLOutputType buildInstanceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                     InstanceOutputTypeService instanceOutputTypeService) {
        return GraphQLBoolean;
    }

    @Override
    public GraphQLInputType buildInstanceInputType(InstanceFieldBuilderContext instanceFieldBuilderContext) {
        return GraphQLBoolean;
    }
}
