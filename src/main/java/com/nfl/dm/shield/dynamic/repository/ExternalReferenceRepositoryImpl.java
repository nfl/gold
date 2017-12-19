package com.nfl.dm.shield.dynamic.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nfl.dm.shield.dynamic.exception.UnauthorizedException;
import com.nfl.graphql.mediator.GraphQLMediator;
import graphql.language.Field;
import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.schema.GraphQLOutputType;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.AbstractReferenceType.REFERENCE_ID;
import static com.nfl.dm.shield.dynamic.domain.schema.instancefield.AbstractReferenceType.REFERENCE_TYPE;

@Service
public class ExternalReferenceRepositoryImpl implements ExternalReferenceRepository {

    private final String PARAM_TEMPLATE = "?variables={variables}&query={query}";

    private final ObjectMapper objectMapper;

    private GraphQLMediator mediator;

    private String baseURLTemplate;

    @Autowired
    public ExternalReferenceRepositoryImpl(
            ObjectMapper objectMapper,
            @Value("${external.reference.baseUrl:http://example.com}") String baseURLTemplate) {
        this.objectMapper = objectMapper;
        this.baseURLTemplate = baseURLTemplate;
    }

    @Override
    public GraphQLOutputType deriveFromExternalTypeName(String typeName, String authHeader) {
        return buildMediator(authHeader).retrieveOutputDescription(typeName);
    }

    private GraphQLMediator buildMediator(String authHeader) {
        if (mediator != null) {
            return mediator;
        }

        RestTemplate schemaRetriever = new RestTemplate();
        HttpEntity<String> entity = buildEntity(authHeader);
        Map<String, String> variables = buildVariables(loadFromFile());

        try {
            ResponseEntity<String> response = schemaRetriever.exchange(baseURLTemplate + PARAM_TEMPLATE,
                    HttpMethod.GET, entity, String.class, variables);

            if (HttpStatus.OK.equals(response.getStatusCode())) {
                mediator = new GraphQLMediator(response.getBody());
                return mediator;
            }

            throw new RuntimeException("Schema retrieve failed: " + response.getStatusCode() + " body: " + response.getBody());
        } catch (HttpStatusCodeException statusCodeException) {
            if (HttpStatus.UNAUTHORIZED.equals(statusCodeException.getStatusCode())) {
                throw new UnauthorizedException(statusCodeException);
            }

            throw statusCodeException;
        }
    }

    private HttpEntity<String> buildEntity(String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        return new HttpEntity<>(headers);
    }

    private Map<String, String> buildVariables(String queryToRun) {
        Map<String, String> variables = new HashMap<>();
        variables.put("query", queryToRun);
        variables.put("variables", "{}");
        return variables;
    }

    private String loadFromFile() {
        ClassPathResource cpr = new ClassPathResource("introspection_query.txt");
        try {
            return IOUtils.toString(cpr.getInputStream(), Charset.defaultCharset());
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public Map<String, Object> findById(SelectionSet selections, Map<String, String> id, String authHeader) {

        String queryToRun = buildSelectionQuery(selections, id);

        RestTemplate schemaRetriever = new RestTemplate();
        HttpEntity<String> entity = buildEntity(authHeader);
        Map<String, String> variables = buildVariables(queryToRun);

        ResponseEntity<String> response = schemaRetriever.exchange(baseURLTemplate + PARAM_TEMPLATE,
                HttpMethod.GET, entity, String.class, variables);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            String jsonRaw = response.getBody();
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> retMap = objectMapper.readValue(jsonRaw, Map.class);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> itemList = (List<Map<String, Object>>)
                        ((Map<String, Object>) ((Map<String, Object>) retMap.get("data")).get("viewer")).get("nodes");
                if (itemList.isEmpty()) {
                    return null;
                } else {
                    return itemList.get(0);
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);          }
        }

        throw new RuntimeException("Schema retrieve failed: " + response.getStatusCode() + " body: " + response.getBody());
    }

    private String buildSelectionQuery(SelectionSet selections, Map<String, String> id) {

        StringBuilder sb = new StringBuilder("query {   viewer {     nodes(ids:[\"");
        sb.append(id.get(REFERENCE_ID)).append("\"] repository:ENDZONE) {\n... on ");
        sb.append(id.get(REFERENCE_TYPE)).append("{\n");

        InlineFragment selectionFragment = (InlineFragment) selections.getSelections().get(0);
        buildSelections(selectionFragment.getSelectionSet(), sb);
        return sb.append("}}}}").toString();
    }

    private void buildSelections(SelectionSet selectionSet, StringBuilder sb) {
        for (Selection sel : selectionSet.getSelections()) {
            if (sel instanceof Field) {
                Field fieldSel = (Field) sel;
                // Literal
                if (fieldSel.getSelectionSet() == null) {
                    sb.append(fieldSel.getName()).append("\n");
                } else { // Object
                    sb.append(fieldSel.getName()).append(" {\n");
                    buildSelections(fieldSel.getSelectionSet(), sb);
                    sb.append("}\n");
                }
            }
        }
    }
}
