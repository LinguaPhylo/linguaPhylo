package lphy.base.function.io;

import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class GuessSeqTypeFastaTest {

    FastaImporter fastaImporter;
    Reader reader;

    @Test
    void readNucleotide() {
        reader = new StringReader(">nuc1\nACGTN-");
        // if null, then guess the sequence type
        fastaImporter = new FastaImporter(reader, null);

        try {
            List<Sequence> sequenceList = fastaImporter.importSequences();

            Sequence sequence = sequenceList.get(0);

            assertEquals(SequenceType.NUCLEOTIDE, sequence.getSequenceType());

            assertEquals("nuc1", sequence.getTaxon().getName());
            assertEquals("ACGTN-", sequence.getString());

        } catch (IOException | ImportException e) {
            fail("Fail to read fasta in SequenceType.NUCLEOTIDE : ", e);
        }

    }

    @Test
    void readAminoAcid() {
        reader = new StringReader(">Cow\n" +
                "MAYPMQLGFQDATSPIMEELLHFHDHTLMIVFLISSLVLYIISLMLTTKLTHTSTMDAQEVETIWTILPAIILILIALPSLRILYMMDEINNPSLTVKTMGHQWYWSYEYTDYEDLSFDSYMIPTSELKPGELRLLEVDNRVVLPMEMTIRMLVSSEDVLHSWAVPSLGLKTDAIPGRLNQTTLMSSRPGLYYGQCSEICGSNHSFMPIVLELVPLKYFEKWSASML-------\n");
        fastaImporter = new FastaImporter(reader, null);

        try {
            List<Sequence> sequenceList = fastaImporter.importSequences();

            Sequence sequence = sequenceList.get(0);

            assertEquals(SequenceType.AMINO_ACID, sequence.getSequenceType());

            assertEquals("Cow", sequence.getTaxon().getName());
            assertEquals("MAYPMQLGFQ", sequence.getString().substring(0, 10));

        } catch (IOException | ImportException e) {
            fail("Fail to read fasta in SequenceType.AMINO_ACID : ", e);
        }

    }

}