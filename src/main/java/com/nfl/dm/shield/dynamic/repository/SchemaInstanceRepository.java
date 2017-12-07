package com.nfl.dm.shield.dynamic.repository;

import com.nfl.dm.shield.dynamic.domain.Count;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceHolder;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;

import java.util.List;
import java.util.stream.Stream;

public interface SchemaInstanceRepository {

    List<SchemaInstance> listInstances(SchemaInstanceKey schemaInstanceKey);

    SchemaInstance findById(SchemaInstanceKey schemaInstanceKey, String id);

    List<SchemaInstance> findInstances(SchemaInstanceKey schemaInstanceKey, List<String> instanceIds);

    SchemaInstance upsert(SchemaInstanceKey schemaInstanceKey, SchemaInstance changedInstance);

    SchemaInstance deleteInstance(SchemaInstanceKey schemaInstanceKey, String id);

    // Applies to all namespaces
    boolean hasInstances(String schemaName);

    long countInstances(SchemaInstanceKey schemaInstanceKey);

    Count deleteAllInstances(SchemaInstanceKey schemaInstanceKey);

    /**
     * Finds all the instances within the same namespace specified on the schemaInstanceKey for the Schemas passed on
     * schemaInstanceKey containing the searchString
     * @param schemaInstanceKey Holds namespace information that will be used to define the scope of the search
     * @param schemaKey Schemas in which the search will be performed
     * @param searchString String that will be searched on the Clob for the instances that meet the namespace/schema
     *                     criteria
     * @return Stream of schemaInstances that meet the criteria
     */
    Stream<? extends SchemaInstanceHolder> findInstancesBySchemaKeyAndStringInClob(SchemaInstanceKey schemaInstanceKey,
                                                                         SchemaKey schemaKey, String searchString);
}
