package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.BaseKey;
import com.nfl.dm.shield.dynamic.domain.context.GraphQLInstanceRequestContext;
import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLInputObjectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nfl.dm.shield.dynamic.config.HashConfig.DEFAULT_HASH_TABLE_SIZE;
import static com.nfl.dm.shield.dynamic.domain.BaseKey.SCHEMA_NAME_FIELD;
import static com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance.SCHEMA_INSTANCE_KEY_FIELD;
import static graphql.Scalars.GraphQLID;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;

@SuppressWarnings("WeakerAccess")
public class MultiTypeDynamicReferenceType extends AbstractReferenceType {
    public MultiTypeDynamicReferenceType(SchemaDescription parent, Map<String, Object> initValues) {
        super(InstanceFieldType.MULTI_TYPE_DYNAMIC_REFERENCE, initValues, parent);
        loadAndValidate(initValues);
    }

    public MultiTypeDynamicReferenceType(SchemaDescription parent, SchemaInstanceField baseField) {
        super(parent, baseField);
        setPossibleTypes(baseField.getPossibleTypes());
    }

    private void loadAndValidate(Map<String, Object> initValues) {
        if (!initValues.containsKey(POSSIBLE_TYPES_FIELD)) {
            throw new IllegalArgumentException("Must have at least one possible type.");
        }
        @SuppressWarnings("unchecked")
        List<String> possibleTypes = (List<String>) initValues.get(POSSIBLE_TYPES_FIELD);
        if (possibleTypes.isEmpty()) {
            throw new IllegalArgumentException("Must have at least one possible type.");
        }
        setPossibleTypes(possibleTypes);
    }

    @Override
    public GraphQLOutputType buildInstanceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                     InstanceOutputTypeService instanceOutputTypeService) {
        return createReferenceOutputType(instanceFieldBuilderContext, instanceOutputTypeService,
                getParent().getName() + getMemberFieldName() + "MultiTypeReference");
    }

    @Override
    Map<String, GraphQLObjectType> buildPossibleTypeMap(List<String> possibleTypes,
                                                        InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                        InstanceOutputTypeService instanceOutputTypeService) {
        Map<String, GraphQLObjectType> retMap = new HashMap<>(DEFAULT_HASH_TABLE_SIZE);
        possibleTypes.forEach(possible -> {
            SchemaKey schemaKey = new SchemaKey(possible, instanceFieldBuilderContext.getSchemaNamespace());
            SchemaDescription schemaDescription = instanceOutputTypeService.findSchemaDescriptionByName(schemaKey);
            retMap.put(possible, schemaDescription.buildInstanceOutputType(instanceFieldBuilderContext,
                    instanceOutputTypeService));
        });
        return retMap;
    }

    @Override
    protected GraphQLFieldDefinition.Builder getGraphQLFieldDefinitionBuilder(
            InstanceFieldBuilderContext instanceFieldBuilderContext,
            InstanceOutputTypeService instanceOutputTypeService) {
        return super.getGraphQLFieldDefinitionBuilder(instanceFieldBuilderContext,
                instanceOutputTypeService).dataFetcher(environment -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> id = (Map<String, Object>)
                    ((Map<String, Object>) environment.getSource()).get(getMemberFieldName());
            if (id == null) {
                return null;
            }

            SchemaInstanceKey parentSchemaInstanceKey =
                    ((GraphQLInstanceRequestContext) environment.getContext()).getSchemaInstanceKey();
            @SuppressWarnings("unchecked")
            String childSchemaName = ((Map<String, String>)id.get(SCHEMA_INSTANCE_KEY_FIELD)).get(SCHEMA_NAME_FIELD);
            SchemaInstanceKey schemaInstanceKey = parentSchemaInstanceKey.getChildSchemaInstanceKey(childSchemaName);

            return instanceOutputTypeService.findMultiTypeById(schemaInstanceKey, id);
        });
    }

    @Override
    public GraphQLInputType buildInstanceInputType(InstanceFieldBuilderContext instanceFieldBuilderContext) {
        GraphQLInputObjectField idField = newInputObjectField()
                .type(new GraphQLNonNull(GraphQLID))
                .name(REFERENCE_ID)
                .build();

        GraphQLInputObjectField schemaInstanceKeyField = newInputObjectField()
                .type(new GraphQLNonNull(SchemaInstanceKey.buildSchemaInputType()))
                .name(SCHEMA_INSTANCE_KEY_FIELD)
                .build();

        return GraphQLInputObjectType.newInputObject()
                .name(getParent().getName() + getMemberFieldName() + "MultiTypeDynamicReference")
                .field(idField)
                .field(schemaInstanceKeyField)
                .build();
    }

    @Override
    public boolean hasReferencesToInstanceID(Object fieldValue, SchemaDescription targetSchemaDescription,
                                             String targetId) {
        if (!(fieldValue instanceof Map)) {
            throw new IllegalArgumentException("Field value is not an Map: " + fieldValue.getClass());
        }

        Map valuesMap = (Map)fieldValue;
        Object idValue = valuesMap.get(REFERENCE_ID);

        @SuppressWarnings("unchecked")
        String schemaName = ((Map<String, String>)valuesMap.get(SCHEMA_INSTANCE_KEY_FIELD)).get(SCHEMA_NAME_FIELD);

        return targetId.equals(idValue) && targetSchemaDescription.getName().equals(schemaName);
    }
}
