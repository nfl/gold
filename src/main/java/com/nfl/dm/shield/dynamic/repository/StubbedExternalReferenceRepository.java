package com.nfl.dm.shield.dynamic.repository;

import graphql.language.SelectionSet;
import graphql.schema.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.nfl.dm.shield.dynamic.config.HashConfig.DEFAULT_HASH_TABLE_SIZE;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.AbstractReferenceType.REFERENCE_ID;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.AbstractReferenceType.REFERENCE_TYPE;
import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

@SuppressWarnings("unused")
@Service("stubbedExternal")
@Primary
public class StubbedExternalReferenceRepository implements ExternalReferenceRepository {

    private final Map<String, Map<String, Map<String, Object>>> externalInstances
            = new ConcurrentHashMap<>(DEFAULT_HASH_TABLE_SIZE);

    private final Map<String, GraphQLOutputType> outputTypeMap = new ConcurrentHashMap<>(89);

    private final GraphQLOutputType emptyType = buildType("empty", Collections.emptyMap());

    public StubbedExternalReferenceRepository()
    {
    }

    @Override
    public GraphQLOutputType deriveFromExternalTypeName(String typeName, String authHeader) {
        GraphQLOutputType retType = outputTypeMap.get(typeName);
        if (retType != null) {
            return  retType;
        }

        return emptyType;
    }

    @Override
    public Map<String, Object> findById(SelectionSet selections, Map<String, String> id, String authHeader) {

        String typeName = id.get(REFERENCE_TYPE);
        if (!externalInstances.containsKey(typeName)) {
            return null;
        }

        return externalInstances.get(typeName).get(id.get(REFERENCE_ID));
    }

    public void loadExternalInstance(String typeName, String id, Map<String, Object> instance) {
        if (!externalInstances.containsKey(typeName)) {
            externalInstances.put(typeName, new HashMap<>(DEFAULT_HASH_TABLE_SIZE));
        }
        externalInstances.get(typeName).put(id, instance);

        if (outputTypeMap.containsKey(typeName)) {
            return; // Type already there
        }

        outputTypeMap.put(typeName, buildType(typeName, instance));
    }

    private GraphQLObjectType buildType(String typeName, Map<String, Object> instance) {
        List<GraphQLFieldDefinition> schemaDefFields = new LinkedList<>();
        GraphQLFieldDefinition idField = newFieldDefinition()
                .type(GraphQLID)
                .name("id")
                .build();
        schemaDefFields.add(idField);
        instance.keySet().stream().filter(key -> !"id".equals(key)).forEach(key -> buildField(key, schemaDefFields));

        return GraphQLObjectType.newObject()
                .name(typeName)
                .description("Example External: " + typeName)
                .fields(schemaDefFields)
                .build();
    }

    private void buildField(String key, List<GraphQLFieldDefinition> schemaDefFields) {
        GraphQLFieldDefinition currentField = newFieldDefinition()
                .type(GraphQLString)
                .name(key)
                .build();
        schemaDefFields.add(currentField);
    }

    public void clearForExternalTesting() {
        externalInstances.clear();
    }
}
