package com.nfl.dm.shield.dynamic;

import com.nfl.dm.shield.dynamic.config.ExternalReferenceRepositoryConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = {"com.nfl.dm.shield.dynamic"}, excludeFilters =
        {
                @ComponentScan.Filter(value = {
                        ExternalReferenceRepositoryConfig.class},
                        type = FilterType.ASSIGNABLE_TYPE)
        })
public class ApplicationTestConfig {

}
