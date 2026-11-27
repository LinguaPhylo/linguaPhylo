package lphy.base;

import lphy.core.model.Value;
import lphy.core.simulator.NamedRandomValueSimulator;
import lphy.core.simulator.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * slphy -seed: the 1st replicate, simulated while parsing, used to ignore the seed,
 * and the later replicates were re-seeded each time, so they were all identical.
 */
public class SimulateSeedTest {

    private static final int REPS = 4;

    private static List<Double> simulate(File lphyFile, Long seed) throws IOException {
        Map<Integer, List<Value>> allReps = new NamedRandomValueSimulator()
                .simulate(lphyFile, REPS, null, null, seed);
        List<Double> xs = new ArrayList<>();
        for (int i = 0; i < REPS; i++) {
            Value x = allReps.get(i).stream().filter(v -> "x".equals(v.getId())).findFirst().orElseThrow();
            xs.add((Double) x.value());
        }
        return xs;
    }

    @Test
    public void testSeedIsReproducibleAndReplicatesDiffer(@TempDir Path dir) throws IOException {
        File lphyFile = dir.resolve("seed.lphy").toFile();
        Files.writeString(lphyFile.toPath(), "x ~ Normal(mean=0.0, sd=1.0);\n");

        List<Double> first = simulate(lphyFile, 42L);
        // draw from the generator in between, so a missing seed cannot pass by chance
        RandomUtils.getRandom().nextDouble();
        List<Double> second = simulate(lphyFile, 42L);
        assertEquals(first, second, "the same seed gave different replicates");

        assertEquals(REPS, new HashSet<>(first).size(), "replicates are not independent: " + first);
        assertNotEquals(first, simulate(lphyFile, 43L), "different seeds gave the same replicates");
    }
}
