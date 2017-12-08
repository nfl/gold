package com.nfl.dm.shield.dynamic.domain.schema.instancefield.constraint;

import graphql.schema.*;

import java.util.LinkedList;
import java.util.List;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static java.util.Collections.emptyList;

@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class Constraint {

    public static final String CONSTRAINT_FIELD = "instanceMutationSchemaConstraint";
    private static final String OUR_DESCRIPTION = "Constraint Definition";

    private GraphQLInputTypeConstraint instanceMutationSchemaConstraint = GraphQLInputTypeConstraint.NONE;

    @SuppressWarnings("unused")
    public Constraint() {
    }

    public Constraint(GraphQLInputTypeConstraint instanceMutationSchemaConstraint) {
        this.instanceMutationSchemaConstraint = instanceMutationSchemaConstraint;
    }

    public static GraphQLInputType buildSchemaInputType() {
        List<GraphQLInputObjectField> constraintDescription = new LinkedList<>();

        GraphQLInputObjectField constraintField = newInputObjectField()
                .type(new GraphQLNonNull(GraphQLInputTypeConstraint.getEnumType()))
                .name(CONSTRAINT_FIELD)
                .build();
        constraintDescription.add(constraintField);

        return new GraphQLInputObjectType("constraintInput", OUR_DESCRIPTION, constraintDescription);
    }

    public static GraphQLObjectType buildSchemaOutputType() {
        List<GraphQLFieldDefinition> valueDefFields = new LinkedList<>();
        GraphQLFieldDefinition constraintField = newFieldDefinition()
                .type(GraphQLInputTypeConstraint.getEnumType())
                .name(CONSTRAINT_FIELD)
                .build();
        valueDefFields.add(constraintField);

        return new GraphQLObjectType("constraintDisplay", OUR_DESCRIPTION, valueDefFields, emptyList());
    }

    public GraphQLInputType wrapGraphQLTypeWithConstraint(GraphQLInputType graphQLInputType) {
        return instanceMutationSchemaConstraint.getGraphQLWrapFunction().apply(graphQLInputType);
    }

    @SuppressWarnings("unused")
    public GraphQLInputTypeConstraint getInstanceMutationSchemaConstraint() {
        return instanceMutationSchemaConstraint;
    }

    @SuppressWarnings("unused")
    public void setInstanceMutationSchemaConstraint(GraphQLInputTypeConstraint GraphQLInputTypeConstraint) {
        this.instanceMutationSchemaConstraint = GraphQLInputTypeConstraint;
    }

}
