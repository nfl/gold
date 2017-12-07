package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import graphql.schema.GraphQLEnumType;

import java.util.Map;

import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.LiteralCheck.*;
import static graphql.schema.GraphQLEnumType.newEnum;

@SuppressWarnings("unused")
public enum InstanceFieldType {
    STRING("String", createBasicLiteral()) {

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, Map<String, Object> initValues) {
            return new StringType(parent, initValues);
        }

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, SchemaInstanceField baseField) {
            return new StringType(parent, baseField);
        }
    },
    INTEGER("Integer", createBasicLiteral()) {

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, Map<String, Object> initValues) {
            return new IntegerType(parent, initValues);
        }

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, SchemaInstanceField baseField) {
            return new IntegerType(parent, baseField);
        }
    },
    BOOLEAN("Boolean", createBasicLiteral()) {

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, Map<String, Object> initValues) {
            return new BooleanType(parent, initValues);
        }

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, SchemaInstanceField baseField) {
            return new BooleanType(parent, baseField);
        }
    },
    ENUM("Enum", createBasicLiteral()) {

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, Map<String, Object> initValues) {
            return new EnumType(parent, initValues);
        }

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, SchemaInstanceField baseField) {
            return new EnumType(parent, baseField);
        }
    },
    LIST("Array", createListLiteral()) {

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, Map<String, Object> initValues) {
            return new ListType(parent, initValues);
        }

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, SchemaInstanceField baseField) {
            return new ListType(parent, baseField);
        }

        @Override
        public boolean hasRelation(SchemaInstanceField field, SchemaDescription schema) {
            return field.getArrayEntryType().hasRelation(field, schema);
        }
    },
    SAME_REFERENCE("AnotherInstanceReference", createOtherLiteral()) {

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, Map<String, Object> initValues) {
            return new SameReferenceType(parent, initValues);
        }

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, SchemaInstanceField baseField) {
            return new SameReferenceType(parent, baseField);
        }

        @Override
        public boolean hasRelation(SchemaInstanceField field, SchemaDescription schema) {
            return field.getParent().getName().equals(schema.getName());
        }
    },
    OTHER_DYNAMIC_DOMAIN("AnotherDynamicDomainReference", createOtherLiteral()) {

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, Map<String, Object> initValues) {
            return new OtherDynamicDomainType(parent, initValues);
        }

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, SchemaInstanceField baseField) {
            return new OtherDynamicDomainType(parent, baseField);
        }

        @Override
        public boolean hasRelation(SchemaInstanceField field, SchemaDescription schema) {
            return field.getOtherTypeName().equals(schema.getName());
        }
    },
    VALUE_OBJECT("Struct", createOtherLiteral()) {

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, Map<String, Object> initValues) {
            return new ValueType(parent, initValues);
        }

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, SchemaInstanceField baseField) {
            return new ValueType(parent, baseField);
        }
    },
    MULTI_TYPE_DYNAMIC_REFERENCE("MultiTypeDynamicReference", createOtherLiteral()) {
        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, Map<String, Object> initValues) {
            return new MultiTypeDynamicReferenceType(parent, initValues);
        }

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, SchemaInstanceField baseField) {
            return new MultiTypeDynamicReferenceType(parent, baseField);
        }

        @Override
        public boolean hasRelation(SchemaInstanceField field, SchemaDescription schema) {
            return field.getPossibleTypes().contains(schema.getName());
        }
    },

    EXTERNAL_REFERENCE("ExternalReference", createOtherLiteral()) {
        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, Map<String, Object> initValues) {
            return new ExternalReferenceType(parent, initValues);
        }

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, SchemaInstanceField baseField) {
            return new ExternalReferenceType(parent, baseField);
        }
    },
    NONE("InternalUseOnly", createOtherLiteral()) {
        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, Map<String, Object> initValues) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SchemaInstanceField fieldFactory(SchemaDescription parent, SchemaInstanceField baseField) {
            throw new UnsupportedOperationException();
        }
    };

    private static final GraphQLEnumType graphQLEnumType;
    private String display;
    private LiteralCheck literalCheck;

    InstanceFieldType(String display, LiteralCheck literalCheck) {
        this.display = display;
        this.literalCheck = literalCheck;
    }

    public String getDisplay() {
        return display;
    }

    public boolean isLiteralType(InstanceFieldType arraySubType) {
        return literalCheck.literalCheck(arraySubType);
    }

    public abstract SchemaInstanceField fieldFactory(SchemaDescription parent, Map<String, Object> initValues);

    public abstract SchemaInstanceField fieldFactory(SchemaDescription parent, SchemaInstanceField baseField);

    static {
        GraphQLEnumType.Builder builder = newEnum().name("InstanceFieldType");

        for (InstanceFieldType value : InstanceFieldType.values()) {
            builder.value(value.getDisplay(), value.name());
        }

        graphQLEnumType = builder.build();

    }

    public static GraphQLEnumType createEnumType() {
        return graphQLEnumType;
    }

    public boolean hasRelation(SchemaInstanceField field, SchemaDescription schema) {
        return false;
    }
}
