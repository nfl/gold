package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;

import java.util.List;

public interface SchemaService {

    List<SchemaDescription> findDirectRelatedSchemas(SchemaDescription source);

    List<SchemaDescription> findAllSchemas(String schemaNamespace);

    List<SchemaDescription> findSchemas(List<String> names, String schemaNamespace);

    SchemaDescription upsert(SchemaDescription schemaDescription);

    SchemaDescription deleteSchema(String name, String namespace);
}
