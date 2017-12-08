package com.nfl.dm.shield.dynamic.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nfl.dm.shield.dynamic.repository.ExternalReferenceRepository;
import com.nfl.dm.shield.dynamic.repository.ExternalReferenceRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Configuration
public class ExternalReferenceRepositoryConfig {

    @Value("${external.reference.baseUrl:http://example.com}")
    private String baseURLTemplate;

    @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"})
    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public ExternalReferenceRepository externalReferenceRepository() {
        return new ExternalReferenceRepositoryImpl(objectMapper, baseURLTemplate);
    }

}
