package com.nfl.dm.shield.dynamic.domain.schema;

import com.nfl.dm.shield.dynamic.domain.instance.SchemaInstance;
import graphql.schema.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.nfl.dm.shield.dynamic.domain.schema.FilterConfiguration.FilterOperator.graphQLEnumType;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static java.util.Collections.emptyList;

public class FilterConfiguration {

    public static final String FILTERS_ARGUMENT = "filters";
    private static final String FIELD_NAME = "fieldName";
    private static final String FILTER_NAME = "filterName";
    private static final String FILTER_OPERATOR = "filterOperator";
    private static final String OUR_DESCRIPTION = "Defines filterable fields on root";

    public enum FilterOperator {

        EQUALS(Object::equals);

        FilterOperator(BiFunction<Object, Object, Boolean> function) {
            this.predicateFunction = function;
        }

        private BiFunction<Object, Object, Boolean> predicateFunction;

        public static final GraphQLEnumType graphQLEnumType = buildEnum();

        private static GraphQLEnumType buildEnum() {
            GraphQLEnumType.Builder builder = newEnum().name("FilterOperator");

            for (FilterOperator value : FilterOperator.values()) {
                builder.value(value.name());
            }

            return builder.build();
        }
    }

    private String fieldName;

    private String outputFilterName;

    private FilterOperator filterOperator;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOutputFilterName() {
        return outputFilterName;
    }

    public void setOutputFilterName(String outputFilterName) {
        this.outputFilterName = outputFilterName;
    }

    @SuppressWarnings("unused")
    public FilterOperator getFilterOperator() {
        return filterOperator;
    }

    public void setFilterOperator(FilterOperator filterOperator) {
        this.filterOperator = filterOperator;
    }

    // Add Custom Predicate Implementation here.
    public Predicate<SchemaInstance> buildPredicate(Object filterValue) {
        return schemaInstance ->
                schemaInstance.containsKey(fieldName) && schemaInstance.get(fieldName) != null
                        && filterOperator.predicateFunction.apply(schemaInstance.get(fieldName), filterValue);

    }

    static GraphQLObjectType buildSchemaOutputType() {
        List<GraphQLFieldDefinition> filterConfigurationFields = new LinkedList<>();
        filterConfigurationFields.add(newFieldDefinition()
                .type(GraphQLString)
                .name(FIELD_NAME)
                .build());
        filterConfigurationFields.add(newFieldDefinition()
                .type(GraphQLString)
                .name(FILTER_NAME)
                .build());
        filterConfigurationFields.add(newFieldDefinition()
                .type(graphQLEnumType)
                .name(FILTER_OPERATOR)
                .build());
        return new GraphQLObjectType("filterConfigurationInput", OUR_DESCRIPTION, filterConfigurationFields, emptyList());
    }

    static GraphQLInputType buildSchemaInputType() {
        List<GraphQLInputObjectField> filterConfigurationFields = new LinkedList<>();
        filterConfigurationFields.add(newInputObjectField()
                .type(new GraphQLNonNull(GraphQLString))
                .name(FIELD_NAME)
                .build());
        filterConfigurationFields.add(newInputObjectField()
                .type(new GraphQLNonNull(GraphQLString))
                .name(FILTER_NAME)
                .build());
        filterConfigurationFields.add(newInputObjectField()
                .type(new GraphQLNonNull(graphQLEnumType))
                .name(FILTER_OPERATOR)
                .build());

        return new GraphQLInputObjectType("filterConfigurationOutput", OUR_DESCRIPTION, filterConfigurationFields);
    }

    static List<FilterConfiguration> buildFilterConfiguration(List<Map<String, Object>> init) {
        return init.stream().map(map -> {
            FilterConfiguration fc = new FilterConfiguration();
            fc.setFieldName((String) map.get(FIELD_NAME));
            fc.setOutputFilterName((String) map.get(FILTER_NAME));
            fc.setFilterOperator(FilterOperator.valueOf(map.get(FILTER_OPERATOR).toString()));

            return fc;
        }).collect(Collectors.toList());
    }
}
