package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import graphql.schema.GraphQLOutputType;

import java.util.List;
import java.util.Map;

public interface InstanceOutputTypeService {

    List<SchemaDescription> findRelatedSchemas(SchemaDescription schemaDescription);

    SchemaDescription findSchemaDescriptionByName(SchemaKey schemaKey);

    SchemaInstance findSchemaInstance(SchemaInstanceKey schemaInstanceKey, String id);

    List<SchemaInstance> findSchemaInstances(SchemaInstanceKey schemaInstanceKey, List<String> instanceIds);

    SchemaInstance findMultiTypeById(SchemaInstanceKey schemaInstanceKey, Map<String, Object> id);

    GraphQLOutputType deriveFromExternalTypeName(String typeName);

    DataFetcherFactory getDataFetcherFactory();

    List<SchemaInstance> findRefereeSchemaInstances(SchemaInstanceKey schemaInstanceKey, SchemaKey schemaKey, String id);
}
