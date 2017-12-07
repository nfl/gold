package com.nfl.dm.shield.dynamic.domain.schema;

public class InMemorySchemaDescriptionHolder extends SchemaDescriptionHolder {

    private SchemaDescription schemaDescription;

    public InMemorySchemaDescriptionHolder(SchemaDescription schemaDescription) {
        this.schemaDescription = schemaDescription;
    }

    @Override
    public SchemaDescription toSchemaDescription() {
        return schemaDescription;
    }

}
