package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;

import java.util.Map;

import static graphql.Scalars.GraphQLID;

public class SameReferenceType extends SchemaInstanceField {

    SameReferenceType(SchemaDescription parent, Map<String, Object> initValues) {
        super(InstanceFieldType.SAME_REFERENCE, initValues, parent);
    }

    SameReferenceType(SchemaDescription parent, SchemaInstanceField baseField) {
        super(parent, baseField);
    }

    @Override
    public GraphQLOutputType buildInstanceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                     InstanceOutputTypeService instanceOutputTypeService) {
        return new GraphQLTypeReference(getParent().getName());
    }

    @Override
    public GraphQLInputType buildInstanceInputType(InstanceFieldBuilderContext instanceFieldBuilderContext) {
        return GraphQLID;
    }

    @Override
    protected GraphQLFieldDefinition.Builder getGraphQLFieldDefinitionBuilder(InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                                              InstanceOutputTypeService instanceOutputTypeService) {
        return super.getGraphQLFieldDefinitionBuilder(instanceFieldBuilderContext, instanceOutputTypeService)
                .dataFetcher(instanceOutputTypeService.getDataFetcherFactory().getReferenceDataFetcher(instanceOutputTypeService, getParent().getName(), getMemberFieldName()));
    }

    @Override
    public void validateInstance(Object fieldValue) {
        if (fieldValue.toString().isEmpty()) {
            throw new IllegalArgumentException("Empty instance reference");
        }
    }

    @Override
    public boolean hasReferencesToInstanceID(Object fieldValue, SchemaDescription targetSchemaDescription, String targetId) {
        return (getParent().getName().equals(targetSchemaDescription.getName()) && targetId.equals(fieldValue));
    }
}