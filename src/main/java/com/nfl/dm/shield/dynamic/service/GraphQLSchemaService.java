package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.domain.context.GraphQLSchemaRequestContext;
import com.nfl.dm.shield.dynamic.domain.schema.SchemaDescription;
import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"WeakerAccess", "SpringJavaAutowiringInspection"})
@Service
public class GraphQLSchemaService extends GraphQLBaseService {

    private final GraphQL graphQL;

    private final SchemaService schemaService;

    @Autowired
    public GraphQLSchemaService(@Qualifier("schema") GraphQL graphQL, SchemaService schemaService) {
        this.graphQL = graphQL;
        this.schemaService = schemaService;
    }

    public GraphQLResult executeQuery(String query, Map<String, Object> variablesMap, SchemaWriteAccess mutablePerms) {

        GraphQLSchemaRequestContext graphQLSchemaRequestContext = new GraphQLSchemaRequestContext(variablesMap, mutablePerms, new ConcurrentHashMap<>());

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .context(graphQLSchemaRequestContext)
                .root(graphQLSchemaRequestContext)
                .variables(variablesMap)
                .build();
        ExecutionResult executionResult = graphQL.execute(executionInput);
        return buildResult(executionResult);
    }

    public List<SchemaDescription> findDirectRelatedSchemas(SchemaDescription schemaDescription) {
        return schemaService.findDirectRelatedSchemas(schemaDescription);
    }
}
