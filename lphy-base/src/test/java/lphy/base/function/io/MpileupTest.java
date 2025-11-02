package lphy.base.function.io;

import lphy.base.evolution.Mpileup;
import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class MpileupTest {
    @Test
    void testReading() throws IOException {
        // make files up
        Path mpileup = Files.createTempFile("mpileup", ".mpileup");
        Path cellName = Files.createTempFile("cell", ".txt");

        List<String> lines = List.of(
                "chr1\t1\tG\t18\t..................\t~~~~~~~~~~~~~~~~~~\t0\t*\t~~~~~~~~~~\t3\t...\t~~~\t80\t................................................................................\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t7\t.......\t~~~~~~~\t1\t.\t~\t3\t..T\t~~~\t8\t........\t~~~~~~~~\t7\t.......\t~~~~~~~\t31\t...............................\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t6\t......\t~~~~~~\t3\t...\t~~~\t2\t..\t~~\t9\t.........\t~~~~~~~~~\t5\t.....\t~~~~~\t5\t.....\t~~~~~\t0\t*\t~~~~~~~~~~\t16\t................\t~~~~~~~~~~~~~~~~\t6\t......\t~~~~~~\t13\t.............\t~~~~~~~~~~~~~\t36\t....................................\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t5\t.....\t~~~~~\t10\t..........\t~~~~~~~~~~\t7\t.......\t~~~~~~~\t4\t....\t~~~~\t10\t..........\t~~~~~~~~~~\t21\t.....................\t~~~~~~~~~~~~~~~~~~~~~\t12\t............\t~~~~~~~~~~~~\t22\t......................\t~~~~~~~~~~~~~~~~~~~~~~\t13\t.............\t~~~~~~~~~~~~~\t0\t*\t~~~~~~~~~~\t28\t............................\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t4\t....\t~~~~\t4\t....\t~~~~\t3\t...\t~~~\t5\t.....\t~~~~~\t4\t....\t~~~~\t0\t*\t~~~~~~~~~~\t3\t...\t~~~\t1\t.\t~",
                "chr1\t2\tA\t1\t.\t~\t4\t....\t~~~~\t3\t...\t~~~\t24\t........................\t~~~~~~~~~~~~~~~~~~~~~~~~\t2\t..\t~~\t2\t..\t~~\t3\t...\t~~~\t2\t..\t~~\t7\t.......\t~~~~~~~\t23\t.......................\t~~~~~~~~~~~~~~~~~~~~~~~\t3\t...\t~~~\t34\t..................................\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t15\t...............\t~~~~~~~~~~~~~~~\t8\t........\t~~~~~~~~\t12\t............\t~~~~~~~~~~~~\t23\t.......................\t~~~~~~~~~~~~~~~~~~~~~~~\t1\t.\t~\t8\t........\t~~~~~~~~\t1\t.\t~\t15\t...............\t~~~~~~~~~~~~~~~\t6\t......\t~~~~~~\t6\t......\t~~~~~~\t5\t.....\t~~~~~\t15\t...............\t~~~~~~~~~~~~~~~\t3\t...\t~~~\t9\t.........\t~~~~~~~~~\t4\t....\t~~~~\t2\t..\t~~\t22\t......................\t~~~~~~~~~~~~~~~~~~~~~~\t39\t.......................................\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\t2\t..\t~~\t5\t.....\t~~~~~\t15\t...............\t~~~~~~~~~~~~~~~\t3\t...\t~~~\t1\t.\t~\t6\t......\t~~~~~~\t2\t..\t~~\t14\t..............\t~~~~~~~~~~~~~~\t5\t.....\t~~~~~\t12\t............\t~~~~~~~~~~~~"
        );
        Files.write(mpileup, lines);

        List<String> names = IntStream.rangeClosed(1, 40)
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());
        Files.write(cellName, names);

        // read in made up files
        Value<String> mpileupValue = new Value<>("", mpileup.toString());
        Value<String> cellNameValue = new Value<>("", cellName.toString());
        ReadMpileup reader = new ReadMpileup(mpileupValue, cellNameValue);
        List<Mpileup> results = reader.apply().value();

        // check the results
        assertEquals(2, results.size());

        // check mpileup cell num
        assertEquals(40, results.get(0).getPileupData().size());
        assertEquals(40, results.get(1).getPileupData().size());

        // check 3 columns
        assertEquals("chr1", results.get(0).getChromName());
        assertEquals(1, results.get(0).getPosition());
        assertEquals(2, results.get(0).getRef());

        assertEquals("chr1", results.get(1).getChromName());
        assertEquals(2, results.get(1).getPosition());
        assertEquals(0, results.get(1).getRef());
    }

    @Test
    void testInvalidFormat() throws IOException {
        Path mpileup = Files.createTempFile("mpileup", ".txt");
        Path cellName = Files.createTempFile("cellName", ".pileup");
        Value<String> mpileupValue = new Value<>("", mpileup.toString());
        Value<String> cellNameValue = new Value<>("", cellName.toString());
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new ReadMpileup(mpileupValue, cellNameValue),
                "Expected constructor to throw for invalid mpileup extension"
        );
    }
}
