package com.nfl.dm.shield.dynamic.domain.instance;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance.ID;
import static com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance.SCHEMA_INSTANCE_KEY_FIELD;
import static graphql.Scalars.GraphQLID;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static java.util.Collections.emptyList;

public class SchemaInstanceReferencedBy {
    private static final String MAIN_TYPE_NAME = "schemaInstanceReferencedByOutput";
    private static final String MAIN_TYPE_DESCRIPTION = "Container for schemaInstanceReferencedByObject values";

    private static final GraphQLObjectType instanceReferencedByKeyOutputType = buildInitialInstanceReferencedByKeyOutputType();

    public static GraphQLObjectType buildSchemaOutputType() {
        return instanceReferencedByKeyOutputType;
    }

    private static GraphQLObjectType buildInitialInstanceReferencedByKeyOutputType() {
        List<GraphQLFieldDefinition> valueDefFields = new LinkedList<>();
        valueDefFields.add(newFieldDefinition()
                .type(new GraphQLNonNull(GraphQLID))
                .name(ID)
                .build());

        valueDefFields.add(newFieldDefinition()
                .type(new GraphQLNonNull(SchemaInstanceKey.buildSchemaOutputType()))
                .name(SCHEMA_INSTANCE_KEY_FIELD)
                .build());

        return new GraphQLObjectType(MAIN_TYPE_NAME, MAIN_TYPE_DESCRIPTION, valueDefFields, emptyList());
    }

    public static List<Map<String, Object>> fromSchemaInstances(List<SchemaInstance> schemaInstances) {
        return schemaInstances.stream().map(SchemaInstanceReferencedBy::fromSchemaInstance).collect(Collectors.toList());
    }

    private static Map<String, Object> fromSchemaInstance(SchemaInstance schemaInstance) {
        Map<String, Object> instanceReferenceByMap = new HashMap<>();

        SchemaInstanceKey sik = (SchemaInstanceKey) schemaInstance.get(SCHEMA_INSTANCE_KEY_FIELD);

        // Build outer component
        instanceReferenceByMap.put(ID, schemaInstance.getId());
        instanceReferenceByMap.put(SCHEMA_INSTANCE_KEY_FIELD, sik);

        return instanceReferenceByMap;
    }
}
