package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.*;

import java.util.Map;

import static graphql.Scalars.GraphQLID;

@SuppressWarnings("WeakerAccess")
public class OtherDynamicDomainType extends SchemaInstanceField {

    public OtherDynamicDomainType(SchemaDescription parent, Map<String, Object> initValues) {
        super(InstanceFieldType.OTHER_DYNAMIC_DOMAIN, initValues, parent);

        loadAndValidateOtherName(initValues);
    }

    public OtherDynamicDomainType(SchemaDescription parent, SchemaInstanceField baseField) {
        super(parent, baseField);
        setOtherTypeName(baseField.getOtherTypeName());
    }

    private void loadAndValidateOtherName(Map<String, Object> initValues) {
        if (!initValues.containsKey(OTHER_TYPE_NAME_FIELD)) {
            throw new IllegalArgumentException("Missing other type name.");
        }
        String schemaName = initValues.get(OTHER_TYPE_NAME_FIELD).toString();

        if (schemaName.isEmpty()) {
            throw new IllegalArgumentException("Empty schema name.");
        }

        setOtherTypeName(schemaName);
    }

    @Override
    public GraphQLOutputType buildInstanceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                     InstanceOutputTypeService instanceOutputTypeService) {
        SchemaKey schemaKey = new SchemaKey(getOtherTypeName(), instanceFieldBuilderContext.getSchemaNamespace());
        SchemaDescription otherDescription = instanceOutputTypeService.findSchemaDescriptionByName(schemaKey);

        if (otherDescription == null) {
            throw new IllegalStateException("Missing other type name: " + schemaKey);
        }

        if (instanceFieldBuilderContext.computeOutput(otherDescription.getName())) {
            return otherDescription.buildInstanceOutputType(instanceFieldBuilderContext.recurseDown(), instanceOutputTypeService);
        }

        return new GraphQLTypeReference(getOtherTypeName());
    }

    @Override
    public GraphQLInputType buildInstanceInputType(InstanceFieldBuilderContext instanceFieldBuilderContext) {
        return GraphQLID;
    }

    @Override
    protected GraphQLFieldDefinition.Builder getGraphQLFieldDefinitionBuilder(
                                                            InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                            InstanceOutputTypeService instanceOutputTypeService) {
        DataFetcher fetcher = instanceOutputTypeService
                .getDataFetcherFactory()
                .getReferenceDataFetcher(instanceOutputTypeService, getOtherTypeName(), getMemberFieldName());
        return super.getGraphQLFieldDefinitionBuilder(instanceFieldBuilderContext, instanceOutputTypeService)
                .dataFetcher(fetcher);
    }

    @Override
    public void validateInstance(Object fieldValue) {
        if (fieldValue.toString().isEmpty()) {
            throw new IllegalArgumentException("Empty instance reference");
        }
    }

    @Override
    public void validateSchema(String schemaNamespace, InstanceOutputTypeService instanceOutputTypeService) {
        SchemaKey schemaKey = new SchemaKey(getOtherTypeName(), schemaNamespace);
        SchemaDescription otherDescription = instanceOutputTypeService.findSchemaDescriptionByName(schemaKey);

        if (otherDescription == null) {
            throw new IllegalArgumentException("Missing other type name: " + schemaKey);
        }
    }

    @Override
    public boolean hasReferencesToInstanceID(Object fieldValue, SchemaDescription targetSchemaDescription, String targetId) {
        return (getOtherTypeName().equals(targetSchemaDescription.getName()) && targetId.equals(fieldValue));
    }
}