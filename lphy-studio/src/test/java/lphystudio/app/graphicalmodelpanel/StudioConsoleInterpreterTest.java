package lphystudio.app.graphicalmodelpanel;

import lphy.base.evolution.tree.TimeTree;
import lphy.core.model.RandomVariable;
import lphy.core.parser.LPhyParserDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudioConsoleInterpreterTest {

    final String taxa = "taxa = taxa(names=1:10);";

    final String coal = "ψ ~ Coalescent(theta=2, taxa=taxa);";

    GraphicalModelParserDictionary parserDictionary = new GraphicalModelParserDictionary();

    StudioConsoleInterpreter modelInterpreter;
    StudioConsoleInterpreter dataInterpreter;


    @BeforeEach
    void setUp() {
        modelInterpreter = new StudioConsoleInterpreter(parserDictionary, LPhyParserDictionary.Context.model,
                null, null);
        dataInterpreter = new StudioConsoleInterpreter(parserDictionary, LPhyParserDictionary.Context.data,
                modelInterpreter,null);

        dataInterpreter.interpretInput(taxa, LPhyParserDictionary.Context.data);
        modelInterpreter.interpretInput(coal, LPhyParserDictionary.Context.model);
    }

    /**
     * Test issue 66 and 183 : Re-run model block code if data block updated.
     */
    @Test
    public void interpretInput() {
        int nTaxa = 10;
        List<RandomVariable<?>> randomVariables =  parserDictionary.getAllVariablesFromSinks();

        testNTaxa(randomVariables, nTaxa);

        // update data block
        nTaxa = 5;
        final String newData = "taxa = taxa(names=1:" + nTaxa + ");";
        dataInterpreter.interpretInput(newData, LPhyParserDictionary.Context.data);

        randomVariables =  parserDictionary.getAllVariablesFromSinks();

        testNTaxa(randomVariables, nTaxa);

    }

    private static void testNTaxa(List<RandomVariable<?>> randomVariables, int nTaxa) {
        for(RandomVariable variable : randomVariables) {
            if ("ψ".equals(variable.getId())) {
                assertTrue(variable.value() instanceof TimeTree);
                TimeTree tree = (TimeTree) variable.value();

                assertEquals(nTaxa, tree.n(), "Expected taxa number is " + nTaxa + " !");
            } else
                fail("Only ψ is expected as a RandomVariable !");
        }
    }
}