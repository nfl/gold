package com.nfl.dm.shield.dynamic;

import com.nfl.dm.shield.dynamic.repository.ExternalReferenceRepositoryImpl;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("gold_test")
@ComponentScan(basePackages = {"com.nfl.dm.shield.dynamic"}, excludeFilters =
        {
                @ComponentScan.Filter(value = {
                        ExternalReferenceRepositoryImpl.class},
                        type = FilterType.ASSIGNABLE_TYPE)
        })
public class ApplicationTestConfig {

}
