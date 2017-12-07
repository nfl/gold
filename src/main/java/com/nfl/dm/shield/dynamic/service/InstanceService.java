package com.nfl.dm.shield.dynamic.service;


import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;

import java.util.List;

public interface InstanceService {
    /**
     * Finds schema instances with references to the provided id of schemaDescription type on the same namespace as the
     * the passed schemaInstanceKey
     * @param schemaInstanceKey Holds the instance namespace that will be used for the search
     * @param schemaDescription Type the instances should belong to in order to be consider a valid reference to id
     * @param id Id of the instance we are looking references for
     * @return List of instances with references that match the criteria to be consider a reference
     */
    List<SchemaInstance> findRefereeSchemaInstances(SchemaInstanceKey schemaInstanceKey,
                                                    SchemaDescription schemaDescription, String id);
}