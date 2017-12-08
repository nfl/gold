package com.nfl.dm.shield.dynamic.domain.schema.instancefield;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

@Test
public class EnumValueDefTest {

    public void coverSetters() {
        EnumValueDef evd = new EnumValueDef();
        evd.setName("foo");
        evd.setValue("bar");
        assertNotNull(evd);
    }
}
