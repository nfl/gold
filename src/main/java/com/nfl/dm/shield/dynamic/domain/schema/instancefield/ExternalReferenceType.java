package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;

public class ExternalReferenceType extends AbstractReferenceType {

    ExternalReferenceType(SchemaDescription parent, SchemaInstanceField baseField) {
        super(parent, baseField);
        setServiceKey(baseField.getServiceKey());
        setPossibleTypes(baseField.getPossibleTypes());
    }

    ExternalReferenceType(SchemaDescription parent, Map<String, Object> initValues) {
        super(InstanceFieldType.EXTERNAL_REFERENCE, initValues, parent);

        loadAndValidate(initValues);
    }

    private void loadAndValidate(Map<String, Object> initValues) {
        if (!initValues.containsKey(SERVICE_KEY_FIELD) || !initValues.containsKey(POSSIBLE_TYPES_FIELD)) {
            throw new IllegalArgumentException("Missing service key or possible types.");
        }

        String serviceKey = initValues.get(SERVICE_KEY_FIELD).toString();

        if (serviceKey.isEmpty()) {
            throw new IllegalArgumentException("Empty service key.");
        }

        setServiceKey(serviceKey);

        @SuppressWarnings("unchecked")
        List<String> possibleTypes = (List<String>) initValues.get(POSSIBLE_TYPES_FIELD);
        if (possibleTypes.isEmpty()) {
            throw new IllegalArgumentException("Must have at least one possible type.");
        }
        setPossibleTypes(possibleTypes);
    }

    @Override
    public GraphQLOutputType buildInstanceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext, InstanceOutputTypeService instanceOutputTypeService) {
        return createReferenceOutputType(instanceFieldBuilderContext, instanceOutputTypeService, getParent().getName() + getMemberFieldName() + "ShieldReference");
    }

    @Override
    Map<String, GraphQLObjectType> buildPossibleTypeMap(List<String> possibleTypes, InstanceFieldBuilderContext instanceFieldBuilderContext, InstanceOutputTypeService instanceOutputTypeService) {
        Map<String, GraphQLObjectType> retMap = new HashMap<>(89);
        possibleTypes.forEach(possible -> retMap.put(possible,
                (GraphQLObjectType) instanceOutputTypeService.deriveFromExternalTypeName(possible)));
        return retMap;
    }

    @Override
    protected GraphQLFieldDefinition.Builder getGraphQLFieldDefinitionBuilder(InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                                              InstanceOutputTypeService instanceOutputTypeService) {

        return super.getGraphQLFieldDefinitionBuilder(instanceFieldBuilderContext, instanceOutputTypeService)
                .dataFetcher(instanceOutputTypeService.getDataFetcherFactory().getExternalReferenceDataFetcher(getMemberFieldName(), instanceFieldBuilderContext.getAuthHeader()));
    }

    @Override
    public GraphQLInputType buildInstanceInputType(InstanceFieldBuilderContext instanceFieldBuilderContext) {
        GraphQLInputObjectField idField = newInputObjectField()
                .type(new GraphQLNonNull(GraphQLID))
                .name(REFERENCE_ID)
                .build();

        GraphQLInputObjectField typeDefinition = newInputObjectField()
                .type(new GraphQLNonNull(GraphQLString))
                .name(REFERENCE_TYPE)
                .build();

        return GraphQLInputObjectType.newInputObject()
                .name(getParent().getName() + getMemberFieldName() + "ExternalReference")
                .field(idField)
                .field(typeDefinition)
                .build();
    }
}
