package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.service.DataFetcherFactory;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLOutputType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.InstanceFieldType.*;

@SuppressWarnings("WeakerAccess")
public class ListType extends SchemaInstanceField {

    public ListType(SchemaDescription parent, Map<String, Object> initValues) {
        super(InstanceFieldType.LIST, initValues, parent);

        loadAndValidateSubtype(initValues);
    }

    public ListType(SchemaDescription parent, SchemaInstanceField baseField) {
        super(parent, baseField);
        setArrayEntryType(baseField.getArrayEntryType());
        switch (getArrayEntryType()) {
            case OTHER_DYNAMIC_DOMAIN:
            case VALUE_OBJECT:
                setOtherTypeName(baseField.getOtherTypeName());
                break;

            case EXTERNAL_REFERENCE:
                setServiceKey(baseField.getServiceKey());
                setPossibleTypes(baseField.getPossibleTypes());
                break;

            case MULTI_TYPE_DYNAMIC_REFERENCE:
                setPossibleTypes(baseField.getPossibleTypes());
                break;

            // No special processing needed
            default:
                break;
        }
    }

    private void loadAndValidateSubtype(Map<String, Object> initValues) {
        if (!initValues.containsKey(LIST_TARGET_FIELD)) {
            throw new IllegalArgumentException(LIST_TARGET_FIELD + " must be present.");
        }
        InstanceFieldType arrayEntryType = InstanceFieldType.valueOf(initValues.get(LIST_TARGET_FIELD).toString());
        if (arrayEntryType == LIST || arrayEntryType == NONE) {
            throw new IllegalArgumentException(arrayEntryType + " is not a valid target for array entries");
        }
        setArrayEntryType(arrayEntryType);

        switch (arrayEntryType) {
            case OTHER_DYNAMIC_DOMAIN:
            case VALUE_OBJECT:
                setOtherTypeName(initValues.get(OTHER_TYPE_NAME_FIELD).toString());
                break;

            case EXTERNAL_REFERENCE:
                setServiceKey(initValues.get(SERVICE_KEY_FIELD).toString());
                //noinspection unchecked
                setPossibleTypes((List<String>) initValues.get(POSSIBLE_TYPES_FIELD));
                break;

            case MULTI_TYPE_DYNAMIC_REFERENCE:
                //noinspection unchecked
                setPossibleTypes((List<String>) initValues.get(POSSIBLE_TYPES_FIELD));
                break;

            // No special processing needed
            default:
                break;
        }
    }

    @Override
    public GraphQLOutputType buildInstanceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                     InstanceOutputTypeService instanceOutputTypeService) {
        Map<String, Object> initValues = buildInitValuesMap();
        SchemaInstanceField target = getArrayEntryType().fieldFactory(getParent(), initValues);
        return new GraphQLList(target.buildInstanceOutputType(instanceFieldBuilderContext, instanceOutputTypeService));
    }

    @Override
    public GraphQLInputType buildInstanceInputType(InstanceFieldBuilderContext instanceFieldBuilderContext) {
        Map<String, Object> initValues = buildInitValuesMap();
        SchemaInstanceField target = getArrayEntryType().fieldFactory(getParent(), initValues);
        return new GraphQLList(target.buildInstanceInputType(instanceFieldBuilderContext));
    }

    private Map<String, Object> buildInitValuesMap() {
        Map<String, Object> initValues = new HashMap<>();
        initValues.put(MEMBER_FIELD_NAME_FIELD, getMemberFieldName());

        switch (getArrayEntryType()) {
            case OTHER_DYNAMIC_DOMAIN:
            case VALUE_OBJECT:
                initValues.put(OTHER_TYPE_NAME_FIELD, getOtherTypeName());
                break;

            case EXTERNAL_REFERENCE:
                initValues.put(SERVICE_KEY_FIELD, getServiceKey());
                initValues.put(POSSIBLE_TYPES_FIELD, getPossibleTypes());
                break;

            case MULTI_TYPE_DYNAMIC_REFERENCE:
                initValues.put(POSSIBLE_TYPES_FIELD, getPossibleTypes());
                break;

            // No special processing needed
            default:
                break;
        }

        return initValues;
    }

    @Override
    protected GraphQLFieldDefinition.Builder getGraphQLFieldDefinitionBuilder(
            InstanceFieldBuilderContext instanceFieldBuilderContext,
            InstanceOutputTypeService instanceOutputTypeService) {

        GraphQLFieldDefinition.Builder orig = super.getGraphQLFieldDefinitionBuilder(instanceFieldBuilderContext,
                instanceOutputTypeService);

        DataFetcherFactory dataFetcherFactory = instanceOutputTypeService.getDataFetcherFactory();

        switch (getArrayEntryType()) {
            case SAME_REFERENCE:
                return orig.dataFetcher(dataFetcherFactory.getArrayReferenceDataFetcher(instanceOutputTypeService,
                        getParent().getName(), getMemberFieldName()));

            case OTHER_DYNAMIC_DOMAIN:
                return orig.dataFetcher(dataFetcherFactory.getArrayReferenceDataFetcher(instanceOutputTypeService,
                        getOtherTypeName(), getMemberFieldName()));

            case EXTERNAL_REFERENCE:
                return orig.dataFetcher(dataFetcherFactory.getExternalReferenceArrayDataFetcher(this,
                        instanceFieldBuilderContext.getAuthHeader()));

            case MULTI_TYPE_DYNAMIC_REFERENCE:
                return orig.dataFetcher(dataFetcherFactory.getMultiTypeDynamicArrayReferenceFetcher(this));

            default:
                return orig;

        }
    }

    @Override
    public void validateInstance(Object fieldValue) {
        if (!(fieldValue instanceof List)) {
            throw new IllegalArgumentException("Field value is not an array: " + fieldValue.getClass());
        }

        @SuppressWarnings("unchecked")
        List<Object> refIds = (List<Object>) fieldValue;
        for (Object referenceId : refIds) {
            if (isReference(getArrayEntryType()) && referenceId.toString().isEmpty()) {
                throw new IllegalArgumentException("Empty instance reference");
            }
        }
    }

    private boolean isReference(InstanceFieldType fieldType) {
        return fieldType == SAME_REFERENCE || fieldType == OTHER_DYNAMIC_DOMAIN;
    }

    @Override
    public boolean hasRelation(SchemaDescription schema) {
        return getArrayEntryType().hasRelation(this, schema);
    }

    @Override
    public boolean hasReferencesToInstanceID(Object fieldValue, SchemaDescription targetSchemaDescription,
                                             String targetId) {
        if (!(fieldValue instanceof List)) {
            throw new IllegalArgumentException("Field value is not an array: " + fieldValue.getClass());
        }

        @SuppressWarnings("unchecked")
        List<Object> entries = (List<Object>) fieldValue;

        // Helper instance field that allow accessing the right hasReferencesToInstanceID method depending on the
        // base list type
        SchemaInstanceField helperSchemaInstanceField = getArrayEntryType().fieldFactory(this.getParent(), this);
        return entries.stream().anyMatch(entry ->
                helperSchemaInstanceField.hasReferencesToInstanceID(entry, targetSchemaDescription, targetId)
        );
    }
}
