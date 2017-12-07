package com.nfl.dm.shield.dynamic.repository;

import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import java.util.List;

public interface SchemaRepository {

    List<SchemaDescription> list(String schemaNamespace);

    SchemaDescription findByName(SchemaKey schemaKey);

    List<SchemaDescription> findByNames(List<SchemaKey> schemaKeys);

    SchemaDescription upsert(SchemaDescription changedSchema);

    SchemaDescription delete(SchemaKey schemaKey);
}
