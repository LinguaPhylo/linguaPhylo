package lphy.base;

import lphy.core.model.BasicFunction;
import lphy.core.model.GenerativeDistribution;
import lphy.core.model.GeneratorUtils;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.spi.Extension;
import lphy.core.spi.LPhyCoreLoader;
import lphy.core.spi.LPhyExtension;
import lphy.core.spi.LoaderManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validate if GenerativeDistribution and Function implemented correctly, also see
 * <a href="https://github.com/LinguaPhylo/linguaPhylo/blob/master/DEV_NOTE2.md">LPhy Developer Guide</a>.
 */
public class GeneratorValidationTest {

    private static List<Class<GenerativeDistribution>> generativeDistributions = new ArrayList<>();
    private static List<Class<BasicFunction>> functions = new ArrayList<>();

    private static List<String> BASIC_EXT_NAMES = List.of("lphy.core.spi.LPhyCoreImpl", "lphy.base.spi.LPhyBaseImpl");

    @BeforeAll
    static void setUp() {
        // cached, everything should be loaded already
        LPhyCoreLoader lphyCoreLoader = LoaderManager.getLphyCoreLoader();
        // get extensions given their class names
        Map<String, Extension> extensionMap = lphyCoreLoader.getExtensionMap(BASIC_EXT_NAMES);
        // check the size
        if (extensionMap.isEmpty())
            fail("Cannot find the extensions defined by the classes : " + BASIC_EXT_NAMES);

        // loop through all extesions
        for (Map.Entry<String, Extension> entry : extensionMap.entrySet()) {
            Extension extension = entry.getValue();
            if (LPhyExtension.class.isAssignableFrom(extension.getClass())) {
                // {@link GenerativeDistribution}, {@link BasicFunction}.
                Map<String, Set<Class<?>>> distMap = ((LPhyExtension) extension).getDistributions();
                generativeDistributions.addAll(LoaderManager.getAllClassesOfType(distMap, GenerativeDistribution.class));
                Map<String, Set<Class<?>>> funcMap = ((LPhyExtension) extension).getFunctions();
                functions.addAll(LoaderManager.getAllClassesOfType(funcMap, BasicFunction.class));
//                types.addAll(((LPhyExtension) extension).getTypes());
//            } else if (ValueFormatterExtension.class.isAssignableFrom(extension.getClass())) {
                // TODO
            } else {
                fail("Unsolved extension from core : " + extension.getExtensionName()
                        + ", which may be registered in " + extension.getModuleName());
            }
        }

    }


    @Test
    void validateGenerativeDistributions() {

        for (Class<GenerativeDistribution> genDistCls : generativeDistributions) {
            System.out.println("Validating Generative Distribution : " + genDistCls.getName());

            try {
                // RandomVariable<T> sample() {
                Method getParams = genDistCls.getMethod("sample");
            } catch (NoSuchMethodException e) {
                fail(genDistCls.getName() + " does not implement sample() !");
            }

            try {
                // public void setParam(String paramName, Value value) {
                Method setParam = genDistCls.getMethod("setParam", String.class, Value.class);
            } catch (NoSuchMethodException e) {
                fail(genDistCls.getName() + " does not implement setParam(String paramName, Value value) !");
            }

            try {
                // public void getParams() {
                Method getParams = genDistCls.getMethod("getParams");
            } catch (NoSuchMethodException e) {
                fail(genDistCls.getName() + " does not implement getParams() !");
            }

            GeneratorInfo ginfo = GeneratorUtils.getGeneratorInfo(genDistCls);
            assertNotNull(ginfo, genDistCls.getName());
            // must have name and description
            assertTrue(ginfo.name() != null && !ginfo.name().isEmpty(), genDistCls.getName());
            assertTrue(ginfo.description() != null && !ginfo.description().isEmpty(), genDistCls.getName());

            //TODO constructor, params

        }

        System.out.println("\nTotal " + generativeDistributions.size() + " Generative Distributions.\n");

    }

    @Test
    void validateFunctions() {

        for (Class<BasicFunction> funcCls : functions) {
            System.out.println("Validating function : " + funcCls.getName());

            try {
                // Value<T> apply() {
                Method apply = funcCls.getMethod("apply");
            } catch (NoSuchMethodException e) {
                fail(funcCls.getName() + " does not implement apply() !");
            }

            GeneratorInfo ginfo = GeneratorUtils.getGeneratorInfo(funcCls);
            assertNotNull(ginfo, funcCls.getName());
            // must have name and description
            assertTrue(ginfo.name() != null && !ginfo.name().isEmpty(), funcCls.getName());
            assertTrue(ginfo.description() != null && !ginfo.description().isEmpty(), funcCls.getName());

            //TODO constructor, params, setParam in constructor, ...
        }

        System.out.println("\nTotal " + functions.size() + " Generative Distributions.\n");
    }
}
