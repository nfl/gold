package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaKey;
import com.nfl.dm.shield.dynamic.service.DataFetcherFactory;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.GraphQLOutputType;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Get Sonar happier.
 */
@Test
public class OtherDynamicDomainTypeTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void missingOtherType() {
        Map<String, Object> initFields = new HashMap<>();
        initFields.put("memberFieldName", "testField");

        InstanceFieldType.OTHER_DYNAMIC_DOMAIN.fieldFactory(null, initFields);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void otherTypeEmpty() {
        Map<String, Object> initFields = new HashMap<>();
        initFields.put(SchemaInstanceField.MEMBER_FIELD_NAME_FIELD, "testField");
        initFields.put("arrayEntryType", InstanceFieldType.LIST.name());
        initFields.put("otherTypeName", "");

        InstanceFieldType.OTHER_DYNAMIC_DOMAIN.fieldFactory(null, initFields);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void missingValueDef() {
        Map<String, Object> initFields = new HashMap<>();
        initFields.put(SchemaInstanceField.MEMBER_FIELD_NAME_FIELD, "testField");
        initFields.put("arrayEntryType", InstanceFieldType.LIST.name());
        initFields.put("otherTypeName", "missingDynamicName");

        SchemaInstanceField schemaInstanceField = InstanceFieldType.OTHER_DYNAMIC_DOMAIN.fieldFactory(new SchemaDescription(), initFields);

        schemaInstanceField.buildInstanceOutputType(
                new InstanceFieldBuilderContext("someNamespace", 0, null, new ConcurrentSkipListSet<>(), new ConcurrentHashMap<>()),
                buildEmptyInstanceLookupHelper());
    }

    private InstanceOutputTypeService buildEmptyInstanceLookupHelper() {
        return new InstanceOutputTypeService() {

            @Override
            public SchemaDescription findSchemaDescriptionByName(SchemaKey schemaKey) {
                return null;
            }

            @Override
            public SchemaInstance findSchemaInstance(SchemaInstanceKey schemaInstanceKey, String id) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<SchemaInstance> findSchemaInstances(SchemaInstanceKey schemaInstanceKey, List<String> instanceIds) {
                throw new UnsupportedOperationException();
            }

            @Override
            public SchemaInstance findMultiTypeById(SchemaInstanceKey schemaInstanceKey, Map<String, Object> id) {
                throw new UnsupportedOperationException();
            }

            @Override
            public GraphQLOutputType deriveFromExternalTypeName(String typeName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public DataFetcherFactory getDataFetcherFactory() {
                return null;
            }

            @Override
            public List<SchemaInstance> findRefereeSchemaInstances(SchemaInstanceKey schemaInstanceKey, SchemaKey schemaKey, String id) {
                return null;
            }
        };
    }
}
