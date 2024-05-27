package lphy.core.parser;

import lphy.core.model.Value;
import lphy.core.parser.graphicalmodel.GraphicalModel;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static lphy.core.parser.ParserSingleton.getParser;
import static lphy.core.parser.ParserTest.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test if the dictionary to store the correct values after parsing.
 * The core method is {@link lphy.core.parser.graphicalmodel.GraphicalModel#put(String, Value, GraphicalModel.Context)}.
 */
public class ParserDictTest {

    @Test
    public void testData() {
        parse("a=1;");

        LPhyParserDictionary parserDictionary = getParser();
        Map<String, Value<?>> dataDict = parserDictionary.getDataDictionary();
        Set<Value> dataValueSet = parserDictionary.getDataValues();
        Map<String, Value<?>> modelDict = parserDictionary.getModelDictionary();
        Set<Value> modelValueSet = parserDictionary.getModelValues();

        assertEquals(1, dataDict.size());
        assertEquals(1, dataValueSet.size());
        assertEquals(0, modelDict.size());
        assertEquals(0, modelValueSet.size());
        // 1 sink
        assertEquals(1, parserDictionary.getDataModelSinks().size());


        parse("a=3;b=2;");

        dataDict = parserDictionary.getDataDictionary();
        dataValueSet = parserDictionary.getDataValues();
        modelDict = parserDictionary.getModelDictionary();
        modelValueSet = parserDictionary.getModelValues();

        assertEquals(2, dataDict.size());
        assertEquals(2, dataValueSet.size());
        assertEquals(0, modelDict.size());
        assertEquals(0, modelValueSet.size());

        // 2 sinks
        assertEquals(2, parserDictionary.getDataModelSinks().size());
    }

    @Test
    public void testModel() {
        parse("model {\n  a=1; \n}");

        LPhyParserDictionary parserDictionary = getParser();
        Map<String, Value<?>> dataDict = parserDictionary.getDataDictionary();
        Set<Value> dataValueSet = parserDictionary.getDataValues();
        Map<String, Value<?>> modelDict = parserDictionary.getModelDictionary();
        Set<Value> modelValueSet = parserDictionary.getModelValues();

        assertEquals(0, dataDict.size());
        assertEquals(0, dataValueSet.size());
        assertEquals(1, modelDict.size());
        assertEquals(1, modelValueSet.size());
        // 1 sink
        assertEquals(1, parserDictionary.getDataModelSinks().size());


        parse("model {\n a=3;\nb=2; \n}");

        dataDict = parserDictionary.getDataDictionary();
        dataValueSet = parserDictionary.getDataValues();
        modelDict = parserDictionary.getModelDictionary();
        modelValueSet = parserDictionary.getModelValues();

        assertEquals(0, dataDict.size());
        assertEquals(0, dataValueSet.size());
        assertEquals(2, modelDict.size());
        assertEquals(2, modelValueSet.size());

        // 2 sinks
        assertEquals(2, parserDictionary.getDataModelSinks().size());
    }

}
