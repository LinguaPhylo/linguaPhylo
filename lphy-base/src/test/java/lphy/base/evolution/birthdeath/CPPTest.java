package lphy.base.evolution.birthdeath;

import lphy.base.evolution.tree.TimeTree;
import lphy.base.evolution.tree.TimeTreeNode;
import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static lphy.base.evolution.birthdeath.CPPUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CPPTest {
    @Test
    public void testQdistBasic() {
        double result = CPPUtils.Qdist(2, 1, 1, 10);
        assertTrue(result > 0 && result < 1);
    }

    @Test
    void testCPPSimple() {
        Value<Number> samplingProb = new Value("", 0.2);
        Value<Number> birthRate = new Value("", 0.3);
        Value<Number> deathRate = new Value("", 0.1);
        Value<String[]> taxa = new Value<>("", new String[]{"1", "2", "3"});
        Value<Integer> n = new Value("", 3);
        CPPTree tree = new CPPTree(birthRate, deathRate,samplingProb, taxa, n, new Value<>("", 10), null);
        TimeTree sampledTree = tree.sample().value();
        assertEquals(3, sampledTree.getLeafNodes().size());
        assertEquals(10, sampledTree.getRoot().getAge());

        assert(List.of(sampledTree.getRoot().getLeafNames()).contains("1"));
        assert(List.of(sampledTree.getRoot().getLeafNames()).contains("3"));
        assert(List.of(sampledTree.getRoot().getLeafNames()).contains("2"));
    }

    @Test
    void testCPP() {
        Value<Number> samplingProb = new Value("", 0.2);
        Value<Number> birthRate = new Value("", 0.3);
        Value<Number> deathRate = new Value("", 0.1);
        Value<Integer> n = new Value("", 5);
        Value<String[][]> cladeTaxa = new Value<>("", new String[][]{{"1", "2", "3"}});
        Value<Number[]> cladeMRCAAge = new Value<>("", new Number[]{2.0});

        CalibratedCPPTree cpp = new CalibratedCPPTree(birthRate, deathRate, samplingProb, n, cladeTaxa, cladeMRCAAge, null, null, null);
        TimeTree cppTree = cpp.sample().value();

        for (int i = 0; i < cppTree.getNodeCount(); i++) {
            TimeTreeNode node = cppTree.getNodeByIndex(i);
            if (node.getChildren().size() != 0) {
                String[] children = node.getLeafNames();
                if (children.length == 3 && node.getAge() == 2) {
                    assert (List.of(children).contains("1"));
                    assert (List.of(children).contains("2"));
                    assert (List.of(children).contains("3"));
                }
            }
        }

    }

    @Test
    void testCPP2() {
        Value<Number> samplingProb = new Value("", 0.2);
        Value<Number> birthRate = new Value("", 0.3);
        Value<Number> deathRate = new Value("", 0.1);
        Value<Integer> n = new Value("", 5);
        Value<String[][]> cladeTaxa = new Value<>("", new String[][]{{"1", "2", "3"}});
        Value<Number[]> cladeMRCAAge = new Value<>("", new Number[]{2.0});

        CalibratedCPPTree cpp = new CalibratedCPPTree(birthRate, deathRate, samplingProb, n, cladeTaxa, cladeMRCAAge, null, new Value<>("",10), null);
        TimeTree cppTree = cpp.sample().value();

        for (int i = 0; i < cppTree.getNodeCount(); i++) {
            TimeTreeNode node = cppTree.getNodeByIndex(i);
            if (node.getChildren().size() != 0) {
                String[] children = node.getLeafNames();
                if (children.length == 3 && node.getAge() == 2) {
                    assert (List.of(children).contains("1"));
                    assert (List.of(children).contains("2"));
                    assert (List.of(children).contains("3"));
                }
            }
        }
        assertEquals(10, cppTree.getRoot().getAge());

    }

    @Test
    void testCPP3() {
        Value<Number> samplingProb = new Value("", 0.5);
        Value<Number> birthRate = new Value("", 0.3);
        Value<Number> deathRate = new Value("", 0.1);
        Value<Integer> n = new Value("", 3);
        Value<String[][]> cladeTaxa = new Value<>("", new String[][]{{"1", "2", "3"},{"1","2"}});
        Value<Number[]> cladeMRCAAge = new Value<>("", new Number[]{2.0, 1.0});

        CalibratedCPPTree cpp = new CalibratedCPPTree(birthRate, deathRate, samplingProb, n, cladeTaxa, cladeMRCAAge, null, null, null);
        for (int j = 0; j < 10 ; j ++) {
            try {
                TimeTree cppTree = cpp.sample().value();
                System.out.println("Iteration " + j);
                System.out.println(cppTree);

                for (int i = 0; i < cppTree.getNodeCount(); i++) {
                    TimeTreeNode node = cppTree.getNodeByIndex(i);
                    if (node.getChildren().size() != 0) {
                        String[] children = node.getLeafNames();
                        if (children.length == 3 && Math.abs(node.getAge() - 2.0) < 1e-8) {
                            List<String> childList = List.of(children);
                            assertEquals (2, node.getAge(), 1e-8);
                            assertTrue(childList.contains("1"), "Missing '1' in 3-clade");
                            assertTrue(childList.contains("2"), "Missing '2' in 3-clade");
                            assertTrue(childList.contains("3"), "Missing '3' in 3-clade");
                        }


                        if (children.length == 2 && List.of(children).contains("1") && List.of(children).contains("2")) {
                            assertEquals(1, node.getAge() , 1e-8);
                        }
                    }
                }

                //assertEquals(2, cppTree.getRoot().getAge());

            } catch (Throwable e) {
                System.err.println("Exception at iteration " + j);
                e.printStackTrace(); // Print full stack trace
                // Optional: break or continue depending on whether you want to stop
                break;
            }
        }
    }

    @Test
    void testCPP4() {
        Value<Number> samplingProb = new Value("", 0.5);
        Value<Number> birthRate = new Value("", 0.3);
        Value<Number> deathRate = new Value("", 0.1);
        Value<Integer> n = new Value("", 5);
        Value<String[][]> cladeTaxa = new Value<>("", new String[][]{{"1", "2", "3"},{"1","2"}});
        Value<Number[]> cladeMRCAAge = new Value<>("", new Number[]{2.0, 1.0});

        CalibratedCPPTree cpp = new CalibratedCPPTree(birthRate, deathRate, samplingProb, n, cladeTaxa, cladeMRCAAge, null, null, null);
        TimeTree cppTree = cpp.sample().value();
        System.out.println(cppTree);

        for (int i = 0; i < cppTree.getNodeCount(); i++) {
            TimeTreeNode node = cppTree.getNodeByIndex(i);
            if (node.getChildren().size() != 0) {
                String[] children = node.getLeafNames();
                if (children.length == 3 && Math.abs(node.getAge() - 2.0) < 1e-8) {
                    List<String> childList = List.of(children);
                    assertEquals (2, node.getAge(), 1e-8);
                    assertTrue(childList.contains("1"), "Missing '1' in 3-clade");
                    assertTrue(childList.contains("2"), "Missing '2' in 3-clade");
                    assertTrue(childList.contains("3"), "Missing '3' in 3-clade");
                }


                if (children.length == 2 && List.of(children).contains("1") && List.of(children).contains("2")) {
                    assertEquals(1, node.getAge() , 1e-8);
                }
            }
        }
    }

    @Test
    void testDeterministicFunctions() {
        double birthRate = 2.0;
        double deathRate = 1.0;
        double samplingProb = 0.5;
        int t = 2;

        // expected numbers calculated from r
        assertEquals(0.8646647, CDF(birthRate, deathRate, samplingProb, t), 1e-6);
        assertEquals(0 , CDF(birthRate, deathRate, samplingProb,0), 1e-6);
        assertEquals(1 , CDF(birthRate, deathRate, samplingProb,Double.POSITIVE_INFINITY), 1e-6);
        assertEquals(0.1353353, densityBD(birthRate, deathRate, samplingProb, t), 1e-6);
        assertEquals(0.2231436, inverseCDF(birthRate, deathRate, samplingProb, 0.2), 1e-6);
        assertEquals(1.203973, inverseCDF(birthRate, deathRate, samplingProb, 0.7), 1e-6);
        assertEquals(0.4707278, Qdist(birthRate, deathRate, t, 10), 1e-6);
        assertEquals(1.826258, transform(0.4, birthRate, deathRate, 10), 1e-6);

        List<Double> list = new ArrayList<>(Arrays.asList(0.0, 1.0, 2.0, 5.0, 0.0));
        assertEquals(0, indexOfMin(list));

        boolean[] list2 = new boolean[]{true, false, true, false, false};
        List<Integer> results = checkTrues(list2);
        int[] expect = new int[]{0,2};
        assertEquals(2, results.size());
        for (int i = 0; i < expect.length; i++) {
            assertEquals(expect[i], results.get(i));
        }
    }
//
//    @Test
//    void testRandomStem() {
//        double birthRate = 2.0;
//        double deathRate = 1.0;
//        //double samplingProb = 0.5;
//        int t = 2;
//        int n = 10;
//
//        List<Double> times = new ArrayList<>();
////        for (int i = 0; i < 100000; i++) {
////            double time = simRandomStem(birthRate, deathRate, t, n);
////            times.add(time);
////        }
//        double time = simRandomStem(birthRate, deathRate, t, n);
//        System.out.println(t);
//        double[] probs = {0.01, 0.05, 0.25, 0.5, 0.75, 0.95, 0.99};
//        double[] expect = {2.013695,2.067106,2.364808,2.833149,3.583671,5.221721,6.874644};
//        for (int i = 0; i < probs.length; i++) {
//            double quantile = quantile(times, probs[i]);
//            System.out.println(quantile);
//            //assertEquals(expect[i], quantile, 0.01);
//        }
//    }
//
//    @Test
//    void testSampleTimes1() {
//        double birthRate = 2.0;
//        double deathRate = 1.0;
//        double samplingProb = 0.5;
//        int t = 2;
//        int n = 1;
//        List<Double> times = new ArrayList<>();
//        for (int i = 0; i < 100000; i++) {
//            double time = sampleTimes(birthRate, deathRate, samplingProb, t, n)[0];
//            times.add(time);
//        }
//
//        Collections.sort(times);
//        double[] probs = {0.01, 0.05, 0.25, 0.5, 0.75, 0.95, 0.99};
//        double[] expect = {0.008414722,0.044227406,0.242445536,0.568499403,1.047400962,1.723441734,1.939893205};
//
//        for (int i = 0; i < probs.length; i++) {
//            double quantile = quantile(times,probs[i]);
//            assertEquals(expect[i], quantile, 0.01);
//        }
//    }
//
//    @Test
//    void testSampleTimes2() {
//        double birthRate = 2.0;
//        double deathRate = 1.0;
//        double samplingProb = 0.5;
//        int t = 2;
//        int n = 1;
//
//        List<Double> times = new ArrayList<>();
//        for (int i = 0; i < 100000; i++) {
//            double time = sampleTimes(birthRate, deathRate, samplingProb, t, false, n)[0];
//            times.add(time);
//        }
//
//        double[] probs = {0.01, 0.05, 0.25, 0.5, 0.75, 0.95, 0.99};
//        double[] expect = {2.009981,2.050876,2.285571,2.691947,3.385330,4.978580,6.550639};
//        for (int i = 0; i < probs.length; i++) {
//            double quantile = quantile(times,probs[i]);
//            assertEquals(expect[i], quantile, 0.01);
//        }
//    }
//
//    @Test
//    void testSampleTimes3() {
//        double birthRate = 2.0;
//        double deathRate = 1.0;
//        double samplingProb = 0.5;
//
//        List<Double> times = new ArrayList<>();
//        for (int i = 0; i < 100000; i++) {
//            double time = sampleTimes(birthRate, deathRate,samplingProb, 1)[0];
//            times.add(time);
//        }
//        Collections.sort(times);
//
//        double[] probs = {0.01, 0.05, 0.25, 0.5, 0.75, 0.95, 0.99};
//        double[] expect = {0.009931474,0.051163545,0.286334601,0.693110516,1.392268707,2.994492895,4.620363467};
//        for (int i = 0; i < probs.length; i++) {
//           double quantile = quantile(times,probs[i]);
//            assertEquals(expect[i], quantile, 0.01);
//        }
//    }

    public static double quantile(List<Double> data, double p) {
        if (p < 0 || p > 1) throw new IllegalArgumentException("p must be between 0 and 1");

        List<Double> x = new ArrayList<>(data); // copy to avoid modifying original
        Collections.sort(x);

        int n = x.size();
        if (n == 1) return x.get(0); // only one element

        double h = 1 + (n - 1) * p; // type 7 formula
        int j = (int) Math.floor(h);
        double gamma = h - j;

        // handle bounds
        if (j <= 0) return x.get(0);
        if (j >= n) return x.get(n - 1);

        // interpolate
        return (1 - gamma) * x.get(j - 1) + gamma * x.get(j);
    }

    @Test
    void testQuantile() {
        List<Double> data = new ArrayList<>();
        for (int i = 1; i < 101; i++) {
            data.add((double)i);
        }
        double[] probs = {0.01, 0.05, 0.25, 0.5, 0.75, 0.95, 0.99};
        double[] expect = {1.99,5.95,25.75,50.50,75.25,95.05,99.01 };
        for (int i = 0; i < probs.length; i++) {
            double quantile = quantile(data,probs[i]);
            assertEquals(expect[i], quantile, 0.00001);
        }
    }
}
