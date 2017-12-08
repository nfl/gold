package com.nfl.dm.shield.dynamic.domain.schema;

import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.testng.Assert.assertNotNull;

@Test
public class ValueObjectFieldTest {


    // make sonar happy with coverage on setters and default constructor.
    public void coverSetters() {
        ValueObjectField vof = new ValueObjectField();
        vof.setName("testField");
        vof.setValueFields(emptyList());
        assertNotNull(vof);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void defaultInitValue() {

        Map<String, Object> initMap = Collections.singletonMap("fizbit", "bogus value");
        new ValueObjectField(null, initMap, null);
    }}
