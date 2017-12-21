package com.nfl.dm.shield.dynamic.repository;

import com.nfl.graphql.mediator.GraphQLMediator;
import graphql.language.SelectionSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.nfl.dm.shield.dynamic.config.HashConfig.DEFAULT_HASH_TABLE_SIZE;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.AbstractReferenceType.REFERENCE_ID;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.AbstractReferenceType.REFERENCE_TYPE;

@SuppressWarnings("unused")
@Service("stubbedExternal")
@Primary
public class StubbedExternalReferenceRepository implements ExternalReferenceRepository {

    private final GraphQLMediator mediator;

    private final Map<String, Map<String, Map<String, Object>>> externalInstances
            = new ConcurrentHashMap<>(DEFAULT_HASH_TABLE_SIZE);

    @Autowired
    public StubbedExternalReferenceRepository(GraphQLMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public GraphQLMediator buildMediator(String authHeader) {
        return mediator;
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
    }

    public void clearForExternalTesting() {
        externalInstances.clear();
    }
}
