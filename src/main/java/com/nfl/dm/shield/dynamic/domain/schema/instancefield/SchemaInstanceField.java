package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import com.google.common.collect.ImmutableSet;
import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.domain.schema.instancefield.constraint.Constraint;
import com.nfl.dm.shield.dynamic.domain.schema.instancefield.constraint.GraphQLInputTypeConstraint;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.*;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.nfl.dm.shield.dynamic.config.HashConfig.DEFAULT_HASH_TABLE_SIZE;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static java.util.Collections.emptyList;

@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class SchemaInstanceField {

    private static final Set<String> RESERVED_KEYWORDS = ImmutableSet.of(SchemaInstance.ID,
            SchemaDescription.REFERENCED_BY_FIELD, SchemaInstance.UPDATE_DATE_FIELD);
    private static final String SILLY_DEFAULT_MEMBER_NAME = "Silly Default Member Name.";
    public static final String MEMBER_TYPE_FIELD = "memberType";
    public static final String MEMBER_FIELD_NAME_FIELD = "memberFieldName";
    public static final String LIST_TARGET_FIELD = "arrayEntryType";
    protected static final String OTHER_TYPE_NAME_FIELD = "otherTypeName";
    protected static final String ENUM_VALUES_FIELD = "enumValues";
    public static final String SERVICE_KEY_FIELD = "serviceKey";
    public static final String POSSIBLE_TYPES_FIELD = "possibleTypes";
    public static final String CONSTRAINTS = "constraints";
    protected static final String MEMBER_DESCRIPTION_FIELD = "memberDescription";
    protected static final String MEMBER_CONFIGURATION_FIELD = "memberConfiguration";

    private InstanceFieldType memberType;

    private String memberFieldName = SILLY_DEFAULT_MEMBER_NAME;

    private String memberDescription;

    private InstanceFieldType arrayEntryType = InstanceFieldType.NONE;

    private String otherTypeName = "";

    private List<EnumValueDef> enumValues = new ArrayList<>(DEFAULT_HASH_TABLE_SIZE);

    private String serviceKey;

    private List<String> possibleTypes = new ArrayList<>();

    private List<Constraint> constraints = new ArrayList<>();

    private String memberConfiguration;

    private SchemaDescription parent;

    public SchemaInstanceField() {
        // For persistence construction only.
    }

    public SchemaInstanceField(SchemaDescription parent, SchemaInstanceField baseField) {
        this.parent = parent;
        this.memberType = baseField.getMemberType();
        this.memberFieldName = baseField.getMemberFieldName();
        this.memberDescription = baseField.getMemberDescription();
        this.constraints = baseField.getConstraints();
        this.memberConfiguration = baseField.getMemberConfiguration();
    }

    public SchemaInstanceField(InstanceFieldType memberType, Map<String, Object> initValues, SchemaDescription parent) {
        if (!initValues.containsKey(MEMBER_FIELD_NAME_FIELD)) {
            throw new IllegalStateException("Member field name must be properly initialized");
        }
        this.memberType = memberType;
        this.memberFieldName = initValues.get(MEMBER_FIELD_NAME_FIELD).toString();
        if (initValues.containsKey(MEMBER_DESCRIPTION_FIELD)) {
            this.memberDescription = initValues.get(MEMBER_DESCRIPTION_FIELD).toString();
        }
        @SuppressWarnings("unchecked")
        List<Map<String, GraphQLInputTypeConstraint>> constraints = (List<Map<String, GraphQLInputTypeConstraint>>)
                initValues.get(CONSTRAINTS);
        if (!CollectionUtils.isEmpty(constraints)) {
            this.constraints = constraints.stream().map(constraintName ->
                    new Constraint(constraintName.get(Constraint.CONSTRAINT_FIELD))).collect(Collectors.toList());
        } else {
            this.constraints = emptyList();
        }
        Object memberConfiguration = initValues.get(MEMBER_CONFIGURATION_FIELD);
        if (memberConfiguration != null) {
            this.memberConfiguration = memberConfiguration.toString();
        }
        this.parent = parent;
        validate();
    }

    private void validate() {
        if (memberFieldName.isEmpty()) {
            throw new IllegalStateException("Member field name must not be empty");
        }

        if (!memberFieldName.trim().equals(memberFieldName)) {
            throw new IllegalStateException("Member field name must not have leading or trailing spaces");
        }

        if (SILLY_DEFAULT_MEMBER_NAME.equals(memberFieldName)) {
            throw new IllegalStateException("Member field name must be properly initialized");
        }

        if (RESERVED_KEYWORDS.contains(memberFieldName)) {
            throw new IllegalStateException("Member field name must not be one of reserved keywords "
                    + RESERVED_KEYWORDS);
        }
    }

    public SchemaDescription getParent() {
        return parent;
    }

    public void setParent(SchemaDescription parent) {
        this.parent = parent;
    }

    public InstanceFieldType getMemberType() {
        return memberType;
    }

    public void setMemberType(InstanceFieldType memberType) {
        this.memberType = memberType;
    }

    public String getMemberFieldName() {
        return memberFieldName;
    }

    public void setMemberFieldName(String memberFieldName) {
        this.memberFieldName = memberFieldName;
    }

    public String getMemberDescription() {
        return memberDescription;
    }

    public void setMemberDescription(String memberDescription) {
        this.memberDescription = memberDescription;
    }

    public InstanceFieldType getArrayEntryType() {
        return arrayEntryType;
    }

    public void setArrayEntryType(InstanceFieldType arrayEntryType) {
        this.arrayEntryType = arrayEntryType;
    }

    public String getOtherTypeName() {
        return otherTypeName;
    }

    public void setOtherTypeName(String otherTypeName) {
        this.otherTypeName = otherTypeName;
    }

    public List<EnumValueDef> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<EnumValueDef> enumValues) {
        this.enumValues = enumValues;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public List<String> getPossibleTypes() {
        return possibleTypes;
    }

    public void setPossibleTypes(List<String> possibleTypes) {
        this.possibleTypes = possibleTypes;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public String getMemberConfiguration() {
        return memberConfiguration;
    }

    public void setMemberConfiguration(String memberConfiguration) {
        this.memberConfiguration = memberConfiguration;
    }

    public boolean isLiteralType(InstanceFieldType fieldType) {
        return getMemberType().isLiteralType(fieldType);
    }

    public void validateInstance(@SuppressWarnings("UnusedParameters") Object fieldValue) {
    }

    @SuppressWarnings("unused")
    public void validateSchema(String schemaNamespace, InstanceOutputTypeService instanceOutputTypeService) {
    }

    public boolean hasRelation(SchemaDescription schema) {
        return getMemberType().hasRelation(this, schema);
    }

    /**
     * Verifies whether the passed targetId is referenced by the instance. Fields holding references to other instances
     * should override this method.
     * @param fieldValue Current value of the SchemaInstanceField that will be checked for references of targetId
     * @param targetSchemaDescription Schema the reference is expected to be based on
     * @param targetId Id to check on the fieldValue
     * @return true if the fieldValue contains references for the target id and schema description, false otherwise.
     */
    @SuppressWarnings("unused")
    public boolean hasReferencesToInstanceID(Object fieldValue, SchemaDescription targetSchemaDescription,
                                             String targetId) {
        return false;
    }


    // BEGIN: Input and OutputTypes for Schema
    private static final String OUR_DESCRIPTION = "Instance Field Schema Description";

    private static final InstanceTypeFetcher INSTANCE_TYPE_DATA_FETCHER = new InstanceTypeFetcher();
    private static final InstanceArrayTypeFetcher INSTANCE_ARRAY_TYPE_DATA_FETCHER = new InstanceArrayTypeFetcher();
    private static final GraphQLEnumType serviceKeyEnumType = newEnum().name("ServiceKeyType").value("SHIELD").build();
    private static final GraphQLInputType sifInputType = buildInitialSchemaInputType();
    private static final GraphQLObjectType sifOutputType = buildInitialSchemaOutputType();

    public static GraphQLInputType buildSchemaInputType() {
        return sifInputType;
    }

    public static GraphQLInputType buildInitialSchemaInputType() {
        List<GraphQLInputObjectField> schemaDefFields = new LinkedList<>();
        GraphQLInputObjectField typeField = newInputObjectField()
                .type(InstanceFieldType.createEnumType())
                .name(SchemaInstanceField.MEMBER_TYPE_FIELD)
                .build();
        schemaDefFields.add(typeField);
        GraphQLInputObjectField arrayTypeField = newInputObjectField()
                .type(InstanceFieldType.createEnumType())
                .name(LIST_TARGET_FIELD)
                .build();
        schemaDefFields.add(arrayTypeField);
        GraphQLInputObjectField fieldName = newInputObjectField()
                .type(new GraphQLNonNull(GraphQLString))
                .name(MEMBER_FIELD_NAME_FIELD)
                .build();
        schemaDefFields.add(fieldName);
        GraphQLInputObjectField descriptionField = newInputObjectField()
                .type(GraphQLString)
                .name(MEMBER_DESCRIPTION_FIELD)
                .build();
        schemaDefFields.add(descriptionField);
        GraphQLInputObjectField otherDynamic = newInputObjectField()
                .type(GraphQLString)
                .name(OTHER_TYPE_NAME_FIELD)
                .build();
        schemaDefFields.add(otherDynamic);
        GraphQLInputObjectField enumFieldValues = newInputObjectField()
                .type(new GraphQLList(EnumValueDef.buildSchemaInputType()))
                .name(SchemaInstanceField.ENUM_VALUES_FIELD)
                .build();
        schemaDefFields.add(enumFieldValues);
        GraphQLInputObjectField serviceKey = newInputObjectField()
                .type(serviceKeyEnumType)
                .name(SchemaInstanceField.SERVICE_KEY_FIELD)
                .build();
        schemaDefFields.add(serviceKey);
        GraphQLInputObjectField possibleTypes = newInputObjectField()
                .type(new GraphQLList(GraphQLString))
                .name(POSSIBLE_TYPES_FIELD)
                .build();
        schemaDefFields.add(possibleTypes);
        GraphQLInputType constraintType = Constraint.buildSchemaInputType();
        GraphQLInputObjectField constraintField = newInputObjectField()
                .type(new GraphQLList(constraintType))
                .name(CONSTRAINTS)
                .build();
        schemaDefFields.add(constraintField);
        GraphQLInputObjectField memberConfigurationField = newInputObjectField()
                .type(GraphQLString)
                .name(MEMBER_CONFIGURATION_FIELD)
                .build();
        schemaDefFields.add(memberConfigurationField);
        return new GraphQLInputObjectType("input_schema", OUR_DESCRIPTION, schemaDefFields);
    }

    public static GraphQLObjectType buildSchemaOutputType() {
        return sifOutputType;
    }

    public static GraphQLObjectType buildInitialSchemaOutputType() {
        List<GraphQLFieldDefinition> schemaDefFields = new LinkedList<>();
        GraphQLFieldDefinition typeField = newFieldDefinition()
                .type(new GraphQLNonNull(InstanceFieldType.createEnumType()))
                .name(MEMBER_TYPE_FIELD)
                .dataFetcher(INSTANCE_TYPE_DATA_FETCHER)
                .build();
        schemaDefFields.add(typeField);
        GraphQLFieldDefinition arrayTypeField = newFieldDefinition()
                .type(InstanceFieldType.createEnumType())
                .name(LIST_TARGET_FIELD)
                .dataFetcher(INSTANCE_ARRAY_TYPE_DATA_FETCHER)
                .build();
        schemaDefFields.add(arrayTypeField);
        GraphQLFieldDefinition fieldName = newFieldDefinition()
                .type(GraphQLString)
                .name(MEMBER_FIELD_NAME_FIELD)
                .build();
        schemaDefFields.add(fieldName);
        GraphQLFieldDefinition descriptionField = newFieldDefinition()
                .type(GraphQLString)
                .name(MEMBER_DESCRIPTION_FIELD)
                .build();
        schemaDefFields.add(descriptionField);
        GraphQLFieldDefinition otherDynamic = newFieldDefinition()
                .type(GraphQLString)
                .name(OTHER_TYPE_NAME_FIELD)
                .build();
        schemaDefFields.add(otherDynamic);
        GraphQLFieldDefinition enumFieldValues = newFieldDefinition()
                .type(new GraphQLList(EnumValueDef.buildSchemaOutputType()))
                .name(ENUM_VALUES_FIELD)
                .build();
        schemaDefFields.add(enumFieldValues);
        GraphQLFieldDefinition serviceKey = newFieldDefinition()
                .type(serviceKeyEnumType)
                .name(SERVICE_KEY_FIELD)
                .build();
        schemaDefFields.add(serviceKey);
        GraphQLFieldDefinition possibleTypes = newFieldDefinition()
                .type(new GraphQLList(GraphQLString))
                .name(POSSIBLE_TYPES_FIELD)
                .build();
        schemaDefFields.add(possibleTypes);
        GraphQLObjectType constraintType = Constraint.buildSchemaOutputType();
        GraphQLFieldDefinition fieldConstraint = newFieldDefinition()
                .type(new GraphQLList(constraintType))
                .name(CONSTRAINTS)
                .build();
        schemaDefFields.add(fieldConstraint);
        GraphQLFieldDefinition memberConfigurationField = newFieldDefinition()
                .type(GraphQLString)
                .name(MEMBER_CONFIGURATION_FIELD)
                .build();
        schemaDefFields.add(memberConfigurationField);
        return new GraphQLObjectType("display_schema", OUR_DESCRIPTION,
                schemaDefFields, emptyList());
    }

    private static class InstanceTypeFetcher implements DataFetcher {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            SchemaInstanceField instanceField = environment.getSource();
            return instanceField.getMemberType().name();
        }
    }

    private static class InstanceArrayTypeFetcher implements DataFetcher {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            SchemaInstanceField instanceField = environment.getSource();
            return instanceField.getArrayEntryType().name();
        }
    }
    // END: Input and OutputTypes for Schema


    // BEGIN: Input and OutputType for Instances
    public GraphQLFieldDefinition buildGraphOutputField(InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                        InstanceOutputTypeService instanceOutputTypeService) {

        return getGraphQLFieldDefinitionBuilder(instanceFieldBuilderContext, instanceOutputTypeService).build();
    }

    protected GraphQLFieldDefinition.Builder getGraphQLFieldDefinitionBuilder(
            InstanceFieldBuilderContext instanceFieldBuilderContext,
            InstanceOutputTypeService instanceOutputTypeService) {
        return newFieldDefinition()
                .type(buildInstanceOutputType(instanceFieldBuilderContext, instanceOutputTypeService))
                .name(getMemberFieldName());
    }

    @SuppressWarnings("unused")
    public GraphQLOutputType buildInstanceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                     InstanceOutputTypeService instanceOutputTypeService) {
        // Forces being overridden by subclasses.
        throw new UnsupportedOperationException();
    }

    public GraphQLInputObjectField buildGraphInputField(InstanceFieldBuilderContext instanceFieldBuilderContext) {
        GraphQLInputType fieldType = buildInstanceInputType(instanceFieldBuilderContext);
        for (Constraint constraint : getConstraints()) {
            fieldType = constraint.wrapGraphQLTypeWithConstraint(fieldType);
        }
        return newInputObjectField()
                .type(fieldType)
                .name(getMemberFieldName())
                .build();
    }

    public GraphQLInputType buildInstanceInputType(
            @SuppressWarnings("unused") InstanceFieldBuilderContext instanceFieldBuilderContext) {
        // Forces being overridden by subclasses.
        throw new UnsupportedOperationException();
    }
    // END: Input and OutputType for Instances
}

