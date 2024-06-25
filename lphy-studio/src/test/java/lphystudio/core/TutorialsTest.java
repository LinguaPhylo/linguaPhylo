//package lphystudio.core;
//
//import lphy.core.io.UserDir;
//import lphy.core.model.Narrative;
//import lphy.core.model.NarrativeUtils;
//import lphy.core.parser.LPhyMetaParser;
//import lphy.core.parser.REPL;
//import lphystudio.core.narrative.HTMLNarrative;
//import org.junit.jupiter.api.BeforeEach;
//
//import java.io.*;
//import java.nio.file.Paths;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class TutorialsTest {
//
//    LPhyMetaParser parser;
//
//    @BeforeEach
//    void setUp() {
//        final String wd = System.getProperty("user.dir");
//        // github workflow run tests in $project_root/lphy, so "**/lphy/../tutorials" is required
//        final File tutorialDir = Paths.get(wd, "..", "tutorials").toFile();
//        assertTrue(tutorialDir.exists(), "Cannot find tutorials folder : " + tutorialDir);
//
//        // set user dir to tutorials folder, so the alignment relative path can work
//        UserDir.setUserDir(tutorialDir.getAbsolutePath());
//        File rsv2file = Paths.get(tutorialDir.getAbsolutePath(), "RSV2.lphy").toFile();
//
//        assertTrue(rsv2file.exists(), "Cannot find tutorial : " + rsv2file);
//
//        //*** Parse LPhy file ***//
//        LPhyMetaParser parser = new REPL();
////        FileReader lphyFile = null;
////        try {
////            lphyFile = new FileReader(rsv2file);
////        } catch (FileNotFoundException e) {
////            fail("Tutorial " + rsv2file + " failed at Exception :\n" + e.getMessage());
////        }
////        BufferedReader bufferedReader = new BufferedReader(lphyFile);
//        try {
//            parser.source(rsv2file);
//        } catch (IOException e) {
//            fail("Failed to parse " + rsv2file + " :\n" + e.getMessage());
//        }
//    }
//
////    @Test
//    void testRSV2() {
//
//
//        Narrative narrative = new HTMLNarrative();
//
//        String narMod = NarrativeUtils.getNarrative(parser, narrative, false, true);
//
//
//
//        assertEquals("", "", "");
//
//
//    }
//}
