package com.nfl.dm.shield.dynamic;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("gold_test")
@ComponentScan(basePackages = {"com.nfl.dm.shield.dynamic"})
public class ApplicationTestConfig {

}
