package com.nfl.dm.shield.dynamic.domain.schema;

import com.nfl.dm.shield.dynamic.domain.context.InstanceFieldBuilderContext;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceKey;
import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstanceReferencedBy;
import com.nfl.dm.shield.dynamic.domain.schema.instancefield.InstanceFieldType;
import com.nfl.dm.shield.dynamic.domain.schema.instancefield.SchemaInstanceField;
import com.nfl.dm.shield.dynamic.service.InstanceOutputTypeService;
import graphql.schema.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.nfl.dm.shield.dynamic.config.HashConfig.DEFAULT_HASH_TABLE_SIZE;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.SchemaInstanceField.MEMBER_TYPE_FIELD;
import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static java.util.Collections.emptyList;

@SuppressWarnings({"unused", "WeakerAccess", "SameParameterValue"})
public class SchemaDescription {

    public static final String REFERENCED_BY_FIELD = "referencedBy";
    public static final String INSTANCE_COUNT_FIELD = "instanceCount";
    public static final String SCHEMA_DEFINITION_OUTPUT_TYPE = "schema_definition_output";
    static final String DESCRIPTION_FIELD = "description";
    public static final String NAME_FIELD = "name";
    private static final String SCHEMA_KEY_FIELD = "schemaKey";
    private static final String DOMAIN_FIELDS = "domainFields";
    private static final String VALUE_DEFINITIONS_FIELD = "valueDefinitions";
    private static final String ID_GENERATION_FIELD = "idGeneration";
    private static final String SILLY_DEFAULT_NAME = "Silly Default name.";
    private static final String MEMBER_CONFIGURATION_FIELD = "memberConfiguration";
    private static final String FILTER_CONFIGURATION_FIELD = "filterConfigurations";

    /** Regular expression to validate schema names */
    private static final Pattern VALID_SCHEMA_NAME_PATTERN = Pattern.compile("[_A-Za-z][_0-9A-Za-z]*");

    private static final IdGenerationFetcher ID_GENERATION_DATA_FETCHER = new IdGenerationFetcher();

    private static final GraphQLFieldDefinition ID_OUTPUT_FIELD = newFieldDefinition()
            .type(GraphQLID)
            .name(SchemaInstance.ID)
            .build();

    private static final GraphQLInputObjectField ID_INPUT_FIELD = newInputObjectField()
            .type(GraphQLID)
            .name(SchemaInstance.ID)
            .build();

    public static final String INSTANCE_COUNT_INSTANCE_NAMESPACE_ARGUMENT = "instanceNamespace";

    private String name = SILLY_DEFAULT_NAME;

    private String namespace = "SCHEMA";

    private String description = "A placeholder description.";

    private IdGeneration idGeneration = IdGeneration.SERVICE_GENERATED_GUID;

    private List<SchemaInstanceField> domainFields = new ArrayList<>(DEFAULT_HASH_TABLE_SIZE);

    private List<ValueObjectField> valueDefinitions = new ArrayList<>(DEFAULT_HASH_TABLE_SIZE);

    private String memberConfiguration;

    private List<FilterConfiguration> filterConfigurations = new ArrayList<>(DEFAULT_HASH_TABLE_SIZE);

    public SchemaDescription() {
    }

    public SchemaDescription(String namespace, Map<String, Object> initValues, InstanceOutputTypeService instanceOutputTypeService) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace is required");
        }
        this.namespace = namespace;
        initMembers(initValues, instanceOutputTypeService);
    }

    private void initMembers(Map<String, Object> initValues, InstanceOutputTypeService instanceOutputTypeService) {
        if (initValues.containsKey(VALUE_DEFINITIONS_FIELD)) {
            //noinspection unchecked
            initValuesDefinitions((List<Map<String, Object>>) initValues.get(VALUE_DEFINITIONS_FIELD), instanceOutputTypeService);
        }

        for (String keyValue : initValues.keySet()) {
            initValue(keyValue, initValues.get(keyValue), instanceOutputTypeService);
        }
    }

    private void initValuesDefinitions(List<Map<String, Object>> valueDefinitions, InstanceOutputTypeService instanceOutputTypeService) {
        setValueDefinitions(new ArrayList<>(DEFAULT_HASH_TABLE_SIZE));
        valueDefinitions.forEach(valueDef -> addValueDefinition(new ValueObjectField(this, valueDef, instanceOutputTypeService)));
    }

    private void initValue(String keyValue, Object initObj, InstanceOutputTypeService instanceOutputTypeService) {
        switch (keyValue) {
            case DESCRIPTION_FIELD:
                description = initObj.toString();
                break;

            case NAME_FIELD:
                name = initObj.toString();
                break;

            case ID_GENERATION_FIELD:
                idGeneration = IdGeneration.valueOf(initObj.toString());
                break;

            case DOMAIN_FIELDS:
                initDomainFields(initObj, instanceOutputTypeService);
                break;

            case VALUE_DEFINITIONS_FIELD:
                //noinspection unchecked
                initValuesDefinitions((List<Map<String, Object>>) initObj, instanceOutputTypeService);
                break;

            case MEMBER_CONFIGURATION_FIELD:
                memberConfiguration = initObj.toString();
                break;

            case FILTER_CONFIGURATION_FIELD:
                //noinspection unchecked
                filterConfigurations = FilterConfiguration.buildFilterConfiguration((List<Map<String, Object>>) initObj);
                break;

            default:
                throw new IllegalStateException("Unknown key value:" + keyValue);
        }

        if (SILLY_DEFAULT_NAME.equals(name))
            throw new IllegalStateException("Name must be properly initialized");
    }

    private void initDomainFields(Object domainFields, InstanceOutputTypeService instanceOutputTypeService) {
        @SuppressWarnings("unchecked")
        List<SchemaInstanceField> instances = buildSchemaInstanceFields((List<Map<String, Object>>) domainFields, instanceOutputTypeService);
        setDomainFields(instances);
    }

    List<SchemaInstanceField> buildSchemaInstanceFields(List<Map<String, Object>> domainFieldDefs, InstanceOutputTypeService instanceOutputTypeService) {

        return domainFieldDefs.stream().map(schemaInstanceMap -> {
            if (!schemaInstanceMap.containsKey(MEMBER_TYPE_FIELD)) {
                throw new IllegalArgumentException(MEMBER_TYPE_FIELD + " not specified.");
            }
            InstanceFieldType fieldType = InstanceFieldType.valueOf(schemaInstanceMap.get(MEMBER_TYPE_FIELD).toString());
            SchemaInstanceField schemaInstanceField = fieldType.fieldFactory(this, schemaInstanceMap);

            schemaInstanceField.validateSchema(namespace, instanceOutputTypeService);

            return schemaInstanceField;
        }).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public SchemaKey getSchemaKey() {
        return new SchemaKey(name, namespace);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @SuppressWarnings("WeakerAccess")
    public IdGeneration getIdGeneration() {
        return idGeneration;
    }

    public void setIdGeneration(IdGeneration idGeneration) {
        this.idGeneration = idGeneration;
    }

    public List<SchemaInstanceField> getDomainFields() {
        return domainFields;
    }

    @SuppressWarnings("WeakerAccess")
    public void setDomainFields(List<SchemaInstanceField> domainFields) {
        this.domainFields = domainFields;
    }

    public List<ValueObjectField> getValueDefinitions() {
        return valueDefinitions;
    }

    @SuppressWarnings("WeakerAccess")
    public void setValueDefinitions(List<ValueObjectField> valueDefinitions) {
        this.valueDefinitions = valueDefinitions;
    }

    public String getMemberConfiguration() {
        return memberConfiguration;
    }

    public void setMemberConfiguration(String memberConfiguration) {
        this.memberConfiguration = memberConfiguration;
    }

    public List<FilterConfiguration> getFilterConfigurations() {
        return filterConfigurations;
    }

    public void setFilterConfigurations(List<FilterConfiguration> filterConfigurations) {
        this.filterConfigurations = filterConfigurations;
    }

    private void addValueDefinition(ValueObjectField valueDef) {
        valueDefinitions.add(valueDef);
    }

    public boolean hasValueType(String valueTypeName) {
        long count = valueDefinitions.stream().filter(valDef -> valDef.getName().equals(valueTypeName)).count();
        return count > 0L;
    }

    public ValueObjectField findValueType(String valueTypeName) {
        //noinspection OptionalGetWithoutIsPresent,ConstantConditions
        return valueDefinitions.stream().filter(valDef -> valDef.getName().equals(valueTypeName)).findFirst().get();
    }

    public static GraphQLOutputType buildSchemaOutputType(
            DataFetcher schemaReferencedByDataFetcher,
            DataFetcher schemaInstanceCountDataFetcher
    ) {
        List<GraphQLFieldDefinition> schemaDefFields = new LinkedList<>();
        GraphQLFieldDefinition nameField = newFieldDefinition()
                .type(GraphQLString)
                .name(NAME_FIELD)
                .build();
        schemaDefFields.add(nameField);
        GraphQLFieldDefinition schemaKey = newFieldDefinition()
                .type(SchemaKey.buildSchemaOutputType())
                .name(SCHEMA_KEY_FIELD)
                .build();
        schemaDefFields.add(schemaKey);
        GraphQLFieldDefinition descriptionField = newFieldDefinition()
                .type(GraphQLString)
                .name(DESCRIPTION_FIELD)
                .build();
        schemaDefFields.add(descriptionField);
        GraphQLFieldDefinition typeField = newFieldDefinition()
                .type(new GraphQLNonNull(IdGeneration.createEnumType()))
                .name(ID_GENERATION_FIELD)
                .dataFetcher(ID_GENERATION_DATA_FETCHER)
                .build();
        schemaDefFields.add(typeField);
        GraphQLObjectType instanceType = SchemaInstanceField.buildSchemaOutputType();
        GraphQLFieldDefinition fieldInstance = newFieldDefinition()
                .type(new GraphQLList(instanceType))
                .name(DOMAIN_FIELDS)
                .build();
        schemaDefFields.add(fieldInstance);
        GraphQLObjectType valueDefinitions = ValueObjectField.buildSchemaOutputType();
        GraphQLFieldDefinition valueInstance = newFieldDefinition()
                .type(new GraphQLList(valueDefinitions))
                .name(VALUE_DEFINITIONS_FIELD)
                .build();
        schemaDefFields.add(valueInstance);
        GraphQLFieldDefinition memberConfigurationField = newFieldDefinition()
                .type(GraphQLString)
                .name(MEMBER_CONFIGURATION_FIELD)
                .build();
        schemaDefFields.add(memberConfigurationField);
        schemaDefFields.add(newFieldDefinition()
                .type(new GraphQLList(FilterConfiguration.buildSchemaOutputType()))
                .name(FILTER_CONFIGURATION_FIELD)
                .build());
        schemaDefFields.add(newFieldDefinition()
                .name(REFERENCED_BY_FIELD)
                .description("Direct related schemas.")
                .type(new GraphQLList(new GraphQLTypeReference(SCHEMA_DEFINITION_OUTPUT_TYPE)))
                .dataFetcher(schemaReferencedByDataFetcher)
                .build());
        schemaDefFields.add(newFieldDefinition()
                .name(INSTANCE_COUNT_FIELD)
                .description("The number of instances in a given instanceNamespace.")
                .type(GraphQLLong)
                .argument(new GraphQLArgument(INSTANCE_COUNT_INSTANCE_NAMESPACE_ARGUMENT, new GraphQLNonNull(GraphQLString)))
                .dataFetcher(schemaInstanceCountDataFetcher)
                .build());
        return GraphQLObjectType.newObject()
                .name(SCHEMA_DEFINITION_OUTPUT_TYPE)
                .description("Dynamic Domain Schema Description")
                .fields(schemaDefFields)
                .build();
    }

    public static GraphQLInputType buildSchemaInputType() {
        List<GraphQLInputObjectField> schemaDefFields = new LinkedList<>();
        GraphQLInputObjectField nameField = newInputObjectField()
                .type(new GraphQLNonNull(GraphQLString))
                .name(NAME_FIELD)
                .build();
        schemaDefFields.add(nameField);
        GraphQLInputObjectField descriptionField = newInputObjectField()
                .type(GraphQLString)
                .name(DESCRIPTION_FIELD)
                .build();
        schemaDefFields.add(descriptionField);
        GraphQLInputObjectField typeField = newInputObjectField()
                .type(IdGeneration.createEnumType())
                .name(ID_GENERATION_FIELD)
                .build();
        schemaDefFields.add(typeField);
        GraphQLInputType instanceType = SchemaInstanceField.buildSchemaInputType();
        GraphQLInputObjectField fieldInstance = newInputObjectField()
                .type(new GraphQLList(instanceType))
                .name(DOMAIN_FIELDS)
                .build();
        schemaDefFields.add(fieldInstance);
        GraphQLInputType valueDefinitionType = ValueObjectField.buildSchemaInputType();
        GraphQLInputObjectField valueDefInstance = newInputObjectField()
                .type(new GraphQLList(valueDefinitionType))
                .name(VALUE_DEFINITIONS_FIELD)
                .build();
        schemaDefFields.add(valueDefInstance);
        GraphQLInputObjectField memberConfigurationField = newInputObjectField()
                .type(GraphQLString)
                .name(MEMBER_CONFIGURATION_FIELD)
                .build();
        schemaDefFields.add(memberConfigurationField);
        schemaDefFields.add(newInputObjectField()
                .type(new GraphQLList(FilterConfiguration.buildSchemaInputType()))
                .name(FILTER_CONFIGURATION_FIELD)
                .build());

        return new GraphQLInputObjectType("schema_definition_input", "Dynamic Domain Schema Input Description", schemaDefFields);
    }

    private static class IdGenerationFetcher implements DataFetcher {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            SchemaDescription schemaDescription = environment.getSource();
            return schemaDescription.getIdGeneration().name();
        }
    }

    public GraphQLObjectType buildInstanceOutputType(InstanceFieldBuilderContext instanceFieldBuilderContext,
                                                     InstanceOutputTypeService instanceOutputTypeService) {
        String schemaName = getName();
        instanceFieldBuilderContext.computeOutput(schemaName);
        // Add to cache if not there.
        if (!instanceFieldBuilderContext.hasObjectType(schemaName)) {
            instanceFieldBuilderContext.addObjectType(schemaName,
                    buildInitialInstanceObjectType(schemaName, instanceFieldBuilderContext, instanceOutputTypeService));
        }
        return instanceFieldBuilderContext.retrieveObjectType(schemaName);
    }

    private GraphQLObjectType buildInitialInstanceObjectType(String schemaName,
            InstanceFieldBuilderContext instanceFieldBuilderContext,
            InstanceOutputTypeService instanceOutputTypeService) {

        List<GraphQLFieldDefinition> schemaInstanceFields = getDomainFields().stream()
                .filter(instanceFieldBuilderContext.getFieldFilter())
                .map(instanceDef -> instanceDef.buildGraphOutputField(instanceFieldBuilderContext, instanceOutputTypeService))
                .collect(Collectors.toList());

        schemaInstanceFields.add(ID_OUTPUT_FIELD);
        schemaInstanceFields.add(newFieldDefinition()
                .type(new GraphQLNonNull(SchemaInstanceKey.buildSchemaOutputType()))
                .name(SchemaInstance.SCHEMA_INSTANCE_KEY_FIELD)
                .build());

        schemaInstanceFields.add(newFieldDefinition()
                .type(new GraphQLList(SchemaInstanceReferencedBy.buildSchemaOutputType()))
                .name(SchemaInstance.INSTANCE_REFERENCED_BY_FIELD)
                .dataFetcher(instanceOutputTypeService.getDataFetcherFactory().getInstanceReferencedByDataFetcher(instanceOutputTypeService))
                .build());

        schemaInstanceFields.add(newFieldDefinition()
                .type(GraphQLLong)
                .name(SchemaInstance.UPDATE_DATE_FIELD)
                .build());

        return new GraphQLObjectType(schemaName, "Dynamic Domain Instance",
                schemaInstanceFields, emptyList());
    }


    public GraphQLInputType buildInstanceInputType(InstanceFieldBuilderContext instanceFieldBuilderContext) {
        List<GraphQLInputObjectField> schemaInstanceFields = getDomainFields().stream()
                .map(instanceField -> instanceField.buildGraphInputField(instanceFieldBuilderContext))
                .collect(Collectors.toList());

        schemaInstanceFields.add(ID_INPUT_FIELD);

        return new GraphQLInputObjectType(getName() + "Input",
                "Dynamic Domain Instance Description", schemaInstanceFields);
    }

    public void validateSchema() {
        //Verify schema name contains only valid characters
        Matcher nameMatcher = VALID_SCHEMA_NAME_PATTERN.matcher(name);

        if(!nameMatcher.matches()) {
            throw new IllegalArgumentException("Schema Description name must match: [_A-Za-z][_0-9A-Za-z]*");
        }

        filterConfigurations.forEach(fc -> {
            String fieldName = fc.getFieldName();

            Optional<SchemaInstanceField> any = domainFields.stream().filter(df -> df.getMemberFieldName().equals(fieldName)).findAny();

            if (!any.isPresent()) {
                throw new IllegalArgumentException("Field names for filter configuration must exist: " + fieldName);
            } else {
                if (!any.get().getMemberType().isLiteralType(InstanceFieldType.NONE)) {
                    throw new IllegalArgumentException("Field for filter configuration must be a literal type: " + fieldName);
                }
            }
        });
    }
}
