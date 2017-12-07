package com.nfl.dm.shield.dynamic.service;

import com.nfl.dm.shield.dynamic.ApplicationTestConfig;
import com.nfl.dm.shield.dynamic.BaseBeanTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.util.Map;

@Test
@ContextConfiguration(classes = {ApplicationTestConfig.class})
public class InstanceRelationTest extends BaseBeanTest {

    private final String addPalette;
    private final String addColor;
    private final String addRedColorInstance;
    private final String addGreenColorInstance;
    private final String addBlueColorInstance;
    private final String addRedGreenRedPaletteInstance;
    private final String addRedGreenBluePaletteInstance;
    private final String deleteRedColorInstance;
    private final String deleteGreenColorInstance;
    private final String deleteBlueColorInstance;
    private final String viewPaletteInstance;

    @Autowired
    private GraphQLInstanceService instanceService;

    public InstanceRelationTest() {
        addPalette = loadFromFile("graphql/relation/type/add_palette.txt");
        addColor = loadFromFile("graphql/relation/type/add_color.txt");
        addRedColorInstance = loadFromFile("graphql/relation/instance/add_red_color_instance.txt");
        addGreenColorInstance = loadFromFile("graphql/relation/instance/add_green_color_instance.txt");
        addBlueColorInstance = loadFromFile("graphql/relation/instance/add_blue_color_instance.txt");
        addRedGreenBluePaletteInstance = loadFromFile("graphql/relation/instance/add_red_green_blue_palette_instance.txt");
        addRedGreenRedPaletteInstance = loadFromFile("graphql/relation/instance/add_red_green_red_palette_instance.txt");
        deleteRedColorInstance = loadFromFile("graphql/relation/instance/delete_red_color_instance.txt");
        deleteGreenColorInstance = loadFromFile("graphql/relation/instance/delete_green_color_instance.txt");
        deleteBlueColorInstance = loadFromFile("graphql/relation/instance/delete_blue_color_instance.txt");
        viewPaletteInstance = loadFromFile("graphql/relation/instance/view_palette_instance.txt");
    }

    @BeforeClass
    public void setUp() throws Exception {
        GraphQLResult result = executeSchemaQuery(addColor);
        assertSuccess(result);

        result = executeSchemaQuery(addPalette);
        assertSuccess(result);
    }

    @BeforeMethod
    public void addRedGreenBlueColorInstances() throws Exception {

        instanceQuery(COLOR, addRedColorInstance,
                "{upsertSchemaInstance={id=red, name=Red color}}");

        instanceQuery(COLOR, addGreenColorInstance,
                "{upsertSchemaInstance={id=green, name=Green color}}");

        instanceQuery(COLOR, addBlueColorInstance,
                "{upsertSchemaInstance={id=blue, name=Blue color}}");
    }

    public void deleteRedColorInstance() throws Exception {
        instanceQuery(PALETTE, addRedGreenRedPaletteInstance,
                "{upsertSchemaInstance={id=rgr, name=Red Green Red, colors=[{id=red}, {id=green}, {id=red}]}}");

        instanceQuery(COLOR, deleteRedColorInstance,
                "{removeInstance={id=red}}");

        instanceQuery(PALETTE, viewPaletteInstance,
                "{viewer={instances={edges=[{node={id=rgr, colors=[{id=green}]}}]}}}");
    }

    public void deleteGreenColorInstance() throws Exception {
        instanceQuery(PALETTE, addRedGreenRedPaletteInstance,
                "{upsertSchemaInstance={id=rgr, name=Red Green Red, colors=[{id=red}, {id=green}, {id=red}]}}");

        instanceQuery(COLOR, deleteGreenColorInstance,
                "{removeInstance={id=green}}");

        instanceQuery(PALETTE, viewPaletteInstance,
                "{viewer={instances={edges=[{node={id=rgr, colors=[{id=red}, {id=red}]}}]}}}");
    }

    public void reductionPaletteColorsToEmptyInNaturalOrder() throws Exception {
        instanceQuery(PALETTE, addRedGreenBluePaletteInstance,
                "{upsertSchemaInstance={id=rgb, name=Red Green Blue, colors=[{id=red}, {id=green}, {id=blue}]}}");

        instanceQuery(COLOR, deleteRedColorInstance,
                "{removeInstance={id=red}}");

        instanceQuery(PALETTE, viewPaletteInstance,
                "{viewer={instances={edges=[{node={id=rgb, colors=[{id=green}, {id=blue}]}}]}}}");

        instanceQuery(COLOR, deleteGreenColorInstance,
                "{removeInstance={id=green}}");

        instanceQuery(PALETTE, viewPaletteInstance,
                "{viewer={instances={edges=[{node={id=rgb, colors=[{id=blue}]}}]}}}");

        instanceQuery(COLOR, deleteBlueColorInstance,
                "{removeInstance={id=blue}}");

        instanceQuery(PALETTE, viewPaletteInstance,
                "{viewer={instances={edges=[{node={id=rgb, colors=[]}}]}}}");
    }

    public void reductionPaletteColorsToEmptyInOppositeOrder() throws Exception {
        instanceQuery(PALETTE, addRedGreenBluePaletteInstance,
                "{upsertSchemaInstance={id=rgb, name=Red Green Blue, colors=[{id=red}, {id=green}, {id=blue}]}}");

        instanceQuery(COLOR, deleteGreenColorInstance,
                "{removeInstance={id=green}}");

        instanceQuery(PALETTE, viewPaletteInstance,
                "{viewer={instances={edges=[{node={id=rgb, colors=[{id=red}, {id=blue}]}}]}}}");

        instanceQuery(COLOR, deleteRedColorInstance,
                "{removeInstance={id=red}}");

        instanceQuery(PALETTE, viewPaletteInstance,
                "{viewer={instances={edges=[{node={id=rgb, colors=[{id=blue}]}}]}}}");

        instanceQuery(COLOR, deleteBlueColorInstance,
                "{removeInstance={id=blue}}");

        instanceQuery(PALETTE, viewPaletteInstance,
                "{viewer={instances={edges=[{node={id=rgb, colors=[]}}]}}}");
    }

    private void instanceQuery(String dynamicObjectName, String query, String expected) {
        Map<String, Object> variableMap = buildVariableMap(INSTANCE_NAME_SPACE, dynamicObjectName);

        GraphQLResult result = instanceService.executeQuery(query, variableMap,
                buildSchemaWriteAccess(), DEFAULT_MAX_RECURSE_DEPTH);

        assertResult(result, expected);
    }

    @AfterMethod
    public void clearInstanceRepo() {
        inMemorySchemaRepository.clearForInstanceTesting();
    }

    @AfterClass
    public void cleanSchema() {
        inMemorySchemaRepository.clearForSchemaTesting();
    }
}
