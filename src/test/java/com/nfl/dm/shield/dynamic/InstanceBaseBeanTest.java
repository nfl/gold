package com.nfl.dm.shield.dynamic;

import com.nfl.dm.shield.dynamic.security.SchemaWriteAccess;
import com.nfl.dm.shield.dynamic.service.GraphQLInstanceService;
import com.nfl.dm.shield.dynamic.service.GraphQLResult;
import com.nfl.dm.shield.dynamic.service.GraphQLSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public abstract class InstanceBaseBeanTest extends BaseBeanTest {
    private final String addSkiBob;
    private final String addSkiBobInstance;
    private final String addJohnCarter;

    @Autowired
    private GraphQLInstanceService instanceService;

    protected InstanceBaseBeanTest() {
        addJohnCarter = loadFromFile("graphql/add_john_carter.txt");
        addSkiBob = loadFromFile("graphql/add_ski_bob.txt");
        addSkiBobInstance = loadFromFile("graphql/add_ski_bob_instance.txt");
    }

    @BeforeClass
    public void loadSchemaDefs() {
        SchemaWriteAccess mutableAccess = buildSchemaWriteAccess();
        assertFalse(addSkiBob.isEmpty());
        GraphQLResult result = schemaService.executeQuery(addSkiBob, buildSchemaVariableMap(), mutableAccess);
        assertTrue(result.isSuccessful());

        assertFalse(addJohnCarter.isEmpty());
        result = schemaService.executeQuery(addJohnCarter, buildSchemaVariableMap(), mutableAccess);
        assertTrue(result.isSuccessful());
    }

    @BeforeMethod
    public void loadSchemaInstances() {
        assertFalse(addSkiBobInstance.isEmpty());
        GraphQLResult result = instanceService.executeQuery(addSkiBobInstance,
                buildVariableMap("skibob"), buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);
        assertTrue(result.isSuccessful());
    }

    @AfterMethod
    public void clearInstanceRepo() {
        inMemorySchemaRepository.clearForInstanceTesting();
    }

    @AfterClass
    public void clearRepo() {
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
