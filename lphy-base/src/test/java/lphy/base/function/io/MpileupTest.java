package lphy.base.function.io;

import lphy.base.evolution.Mpileup;
import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class MpileupTest {
    @Test
    void testReading() throws IOException {
        // make files up
        Path mpileup = Files.createTempFile("mpileup", ".mpileup");

        List<String> lines = List.of(
                "chr1\t1\tG\t18\t..................\t~~~~~~~~~~~~~~~~~~\t0\t*\t~~~~~~~~~~\t3\t...\t~~~\t80\t................................................................................\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t7\t.......\t~~~~~~~\t1\t.\t~\t3\t..T\t~~~\t8\t........\t~~~~~~~~\t7\t.......\t~~~~~~~\t31\t...............................\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t6\t......\t~~~~~~\t3\t...\t~~~\t2\t..\t~~\t9\t.........\t~~~~~~~~~\t5\t.....\t~~~~~\t5\t.....\t~~~~~\t0\t*\t~~~~~~~~~~\t16\t................\t~~~~~~~~~~~~~~~~\t6\t......\t~~~~~~\t13\t.............\t~~~~~~~~~~~~~\t36\t....................................\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t5\t.....\t~~~~~\t10\t..........\t~~~~~~~~~~\t7\t.......\t~~~~~~~\t4\t....\t~~~~\t10\t..........\t~~~~~~~~~~\t21\t.....................\t~~~~~~~~~~~~~~~~~~~~~\t12\t............\t~~~~~~~~~~~~\t22\t......................\t~~~~~~~~~~~~~~~~~~~~~~\t13\t.............\t~~~~~~~~~~~~~\t0\t*\t~~~~~~~~~~\t28\t............................\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t4\t....\t~~~~\t4\t....\t~~~~\t3\t...\t~~~\t5\t.....\t~~~~~\t4\t....\t~~~~\t0\t*\t~~~~~~~~~~\t3\t...\t~~~\t1\t.\t~",
                "chr1\t2\tA\t1\t.\t~\t4\t....\t~~~~\t3\t...\t~~~\t24\t........................\t~~~~~~~~~~~~~~~~~~~~~~~~\t2\t..\t~~\t2\t..\t~~\t3\t...\t~~~\t2\t..\t~~\t7\t.......\t~~~~~~~\t23\t.......................\t~~~~~~~~~~~~~~~~~~~~~~~\t3\t...\t~~~\t34\t..................................\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t15\t...............\t~~~~~~~~~~~~~~~\t8\t........\t~~~~~~~~\t12\t............\t~~~~~~~~~~~~\t23\t.......................\t~~~~~~~~~~~~~~~~~~~~~~~\t1\t.\t~\t8\t........\t~~~~~~~~\t1\t.\t~\t15\t...............\t~~~~~~~~~~~~~~~\t6\t......\t~~~~~~\t6\t......\t~~~~~~\t5\t.....\t~~~~~\t15\t...............\t~~~~~~~~~~~~~~~\t3\t...\t~~~\t9\t.........\t~~~~~~~~~\t4\t....\t~~~~\t2\t..\t~~\t22\t......................\t~~~~~~~~~~~~~~~~~~~~~~\t39\t.......................................\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t2\t..\t~~\t5\t.....\t~~~~~\t15\t...............\t~~~~~~~~~~~~~~~\t3\t...\t~~~\t1\t.\t~\t6\t......\t~~~~~~\t2\t..\t~~\t14\t..............\t~~~~~~~~~~~~~~\t5\t.....\t~~~~~\t12\t............\t~~~~~~~~~~~~"
        );
        Files.write(mpileup, lines);

        String[] names = new String[40];
        for (int i = 0; i < 40; i++) {
            names[i] = String.valueOf(i);
        }

        // read in made up files
        Value<String> mpileupValue = new Value<>("", mpileup.toString());
        Value<Object[]> cellNameValue = new Value<>("", names);
        ReadMpileup reader = new ReadMpileup(mpileupValue, cellNameValue);
        Mpileup results = reader.apply().value();

        // check the results
        assertEquals(2, results.getPileupData().size());

        // check mpileup cell num
        assertEquals(40, results.getPileupData().get(0).size());
        assertEquals(40, results.getPileupData().get(1).size());

        // check 3 columns
        assertEquals("chr1", results.getChromNames()[0]);
        assertEquals(1, results.getPositions()[0]);
        assertEquals(2, results.getRefs()[0]);

        assertEquals("chr1", results.getChromNames()[1]);
        assertEquals(2, results.getPositions()[1]);
        assertEquals(0, results.getRefs()[1]);
    }

    @Test
    void testInvalidFormat() throws IOException {
        Path mpileup = Files.createTempFile("mpileup", ".txt");
        Value<String> mpileupValue = new Value<>("", mpileup.toString());
        String[] cellNames = new String[]{"Cell1", "Cell2"};
        Value<Object[]> cellNameValue = new Value<>("", cellNames);
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new ReadMpileup(mpileupValue, cellNameValue),
                "Expected constructor to throw for invalid mpileup extension"
        );
    }
}
