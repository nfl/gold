package com.nfl.dm.shield.dynamic.security;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.nfl.dm.shield.dynamic.config.HashConfig.DEFAULT_HASH_TABLE_SIZE;

public class SchemaWriteAccess {

    public static final String SCHEMA_MODIFY = "SCHEMA_MODIFY";
    public static final String INSTANCE_MODIFY = "INSTANCE_MODIFY";
    public static final String INSTANCE_DELETE = "INSTANCE_DELETE";
    public static final String INSTANCE_TRUNCATE = "INSTANCE_TRUNCATE";

    private final String authHeader;

    private final Map<String, Set<String>> permissions = new HashMap<>(DEFAULT_HASH_TABLE_SIZE);

    public SchemaWriteAccess() {
        authHeader = "Testing only.";
    }

    public SchemaWriteAccess(String authHeader) {
        this.authHeader = authHeader;
    }

    public void addPermission(String namespace, String permissionName) {

        // Initialize
        if (!permissions.containsKey(namespace)) {
            permissions.put(namespace, new HashSet<>(DEFAULT_HASH_TABLE_SIZE));
        }

        permissions.get(namespace).add(permissionName);
    }

    public boolean hasMutationWriteAccess(String namespace, String permissionName) {
        return permissions.containsKey(namespace) && permissions.get(namespace).contains(permissionName);
    }

    int countPerms() {
        return permissions.size();
    }

    public String getAuthHeader() {
        return authHeader;
    }
}
