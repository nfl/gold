package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import com.nfl.dm.shield.dynamic.domain.schema.instancefield.SchemaInstanceField;
import com.nfl.dm.shield.dynamic.repository.SchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Service
public class SchemaServiceImpl implements SchemaService {

    private static final Logger log = LoggerFactory.getLogger(SchemaServiceImpl.class);

    private final SchemaRepository schemaRepository;

    @Autowired
    public SchemaServiceImpl(SchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    @Override
    public List<SchemaDescription> findDirectRelatedSchemas(SchemaDescription sd) {
        final List<SchemaDescription> allSchemas = schemaRepository.list(sd.getNamespace());

        Set<SchemaDescription> res = allSchemas.stream().flatMap(schema -> schema.getDomainFields().stream())
                .filter(field -> field.hasRelation(sd)).map(SchemaInstanceField::getParent).collect(Collectors.toSet());
        return new ArrayList<>(res);
    }

    @Override
    public List<SchemaDescription> findSchemas(List<String> names, String schemaNamespace) {
        if (names.size() == 1) {
            SchemaDescription desc = schemaRepository.findByName(new SchemaKey(names.get(0), schemaNamespace));
            return desc == null ? emptyList() : singletonList(desc);
        }
        return schemaRepository.findByNames(names.stream()
                .map(n -> new SchemaKey(n, schemaNamespace)).collect(Collectors.toList()));
    }

    public List<SchemaDescription> findAllSchemas(String schemaNamespace) {
        return schemaRepository.list(schemaNamespace);
    }

    @Override
    public SchemaDescription upsert(SchemaDescription schemaDescription) {
        return schemaRepository.upsert(schemaDescription);
    }

    @Override
    public SchemaDescription deleteSchema(String name, String namespace) {
        final SchemaDescription returnDest = schemaRepository.findByName(new SchemaKey(name, namespace));
        if (returnDest == null) {
            log.warn("Unable to delete schema. Schema with name {} not found in namespace {}", name, namespace);
            return null;
        }

        List<SchemaDescription> relatedSchemas = findDirectRelatedSchemas(returnDest);

        // Self-references (if any) won't affect deletion
        relatedSchemas = relatedSchemas.stream()
                .filter(s -> !s.getSchemaKey().equals(returnDest.getSchemaKey()))
                .collect(Collectors.toList());

        if (!relatedSchemas.isEmpty()) {
            final String msg = String.format("Schema %s has relation with: %s",
                    name, mapToNames(relatedSchemas));
            throw new IllegalStateException(msg);
        }
        return schemaRepository.delete(returnDest.getSchemaKey());
    }

    private String mapToNames(List<SchemaDescription> relatedSchemas) {
        return relatedSchemas.stream().map(SchemaDescription::getName).collect(Collectors.joining(", "));
    }
}
