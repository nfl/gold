package com.nfl.dm.shield.dynamic.domain;

public class BaseKey {
    public static final String DYNAMIC_TYPE_NAMESPACE = "DYNAMIC_TYPE_NAMESPACE";
    public static final String DYNAMIC_INSTANCE_NAMESPACE = "DYNAMIC_INSTANCE_NAMESPACE";
    public static final String DYNAMIC_TYPE_NAME = "DYNAMIC_TYPE_NAME";
    public static final String SCHEMA_NAME_FIELD = "schemaName";
    protected static final String SCHEMA_NAMESPACE_FIELD = "schemaNamespace";

    private final String schemaNamespace;
    private final String schemaName;

    protected BaseKey(String schemaName, String schemaNamespace) {
        this.schemaName = schemaName;
        this.schemaNamespace = schemaNamespace;
    }

    public String getSchemaNamespace() {
        return schemaNamespace;
    }

    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BaseKey))
            return false;

        BaseKey that = (BaseKey) other;

        return schemaNamespace.equals(that.getSchemaNamespace())
                && schemaName.equals(that.getSchemaName());
    }

    @Override
    public int hashCode() {
        int result = schemaNamespace.hashCode();
        result = 31 * result + schemaName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return  this.getClass().getName() + "{" +
                "schemaNamespace='" + schemaNamespace + '\'' +
                ", schemaName='" + schemaName + '\'' +
                '}';
    }
}
