package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceHolder;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.repository.SchemaInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Service
public class InstanceServiceImpl implements InstanceService {
    private final SchemaInstanceRepository schemaInstanceRepository;
    private final SchemaService schemaService;

    @Autowired
    public InstanceServiceImpl(SchemaInstanceRepository schemaInstanceRepository, SchemaService schemaService) {
        this.schemaInstanceRepository = schemaInstanceRepository;
        this.schemaService = schemaService;
    }

    /**
     * Returns the instances with references to the Id, schema on the namespace provided on the SchemaInstanceKey
     * @param schemaInstanceKey Holds namespace information to define the scope of the search
     * @param schemaDescription Schema description used by the instance we are looking references for
     * @param id Id for the instance we are looking references for
     * @return List of instances matching the criteria
     */
    public List<SchemaInstance> findRefereeSchemaInstances(SchemaInstanceKey schemaInstanceKey,
                                                           SchemaDescription schemaDescription, String id) {
        List<SchemaDescription> relatedSchemas = schemaService.findDirectRelatedSchemas(schemaDescription);

        //Get the instances for the related schemas that contain the ID on the clob, they are candidates to be
        // references, then filter further only real references
        return relatedSchemas.stream().flatMap(schemaDesc ->
                schemaInstanceRepository.findInstancesBySchemaKeyAndStringInClob(schemaInstanceKey,
                        schemaDesc.getSchemaKey(), id)
                        .map(SchemaInstanceHolder::toSchemaInstance)
                        .filter(schemaInstance -> hasReferencesToInstanceID(schemaInstance, schemaDesc,
                                schemaDescription, id))
        ).collect(Collectors.toList());
    }


    /**
     * Verifies whether the input refereeSchemaInstance contains a reference to the targetId for the given target type
     * (targetSchemaDescription). Both the value and type on the referee instance should match the target id and schema
     * @param refereeSchemaInstance instance that will be verified to confirm it has references to the targetId
     * @param refereeSchemaDescription schema the refereeSchemaInstance is based on
     * @param targetSchemaDescription schema the instance with targetId is based on
     * @param targetId Id of the instance we are checking references for
     * @return true if the refereeSchemaInstance contains fields that refer to the targetId based on the
     * targetSchemaDescription
     */
    private boolean hasReferencesToInstanceID(SchemaInstance refereeSchemaInstance,
                                              SchemaDescription refereeSchemaDescription,
                                              SchemaDescription targetSchemaDescription, String targetId){

        return refereeSchemaDescription.getDomainFields()
                .stream()
                .filter(sif -> sif.hasRelation(targetSchemaDescription))
                .anyMatch(sif -> refereeSchemaInstance.get(sif.getMemberFieldName()) != null
                        && sif.hasReferencesToInstanceID(refereeSchemaInstance.get(sif.getMemberFieldName()),
                        targetSchemaDescription, targetId));
    }
}
