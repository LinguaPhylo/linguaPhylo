package lphy.base.evolution;

import jebl.evolution.sequences.SequenceType;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.SimpleAlignment;
import lphy.base.evolution.datatype.Variant;
import lphy.core.model.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SNPSamplerTest {
    @Test
    void testSNPSampler() {
        Alignment a = new SimpleAlignment(Taxa.createTaxa(1),90000000, SequenceType.NUCLEOTIDE);
        Number p = 0.001;
        Number r = 0;
        Value<Alignment> alignmentValue = new Value<>("id", a);
        Value<Number> pValue = new Value<>("p", p);
        Value<Number> rValue = new Value<>("r", r);

        SNPSampler snpSampler = new SNPSampler(alignmentValue, pValue, rValue);
        Variant[] variant = snpSampler.sample().value();

        assertEquals(a.nchar().intValue() * p.doubleValue() , variant.length, 1000);
        for (Variant v : variant) {
           assertEquals("0|1", v.getGenotype());
        }
    }

    @Test
    void testSNPSamplerR() {
        Alignment a = new SimpleAlignment(Taxa.createTaxa(1),90000000, SequenceType.NUCLEOTIDE);
        Number p = 0.001;
        Number r = 1.6;
        Value<Alignment> alignmentValue = new Value<>("id", a);
        Value<Number> pValue = new Value<>("p", p);
        Value<Number> rValue = new Value<>("r", r);

        SNPSampler snpSampler = new SNPSampler(alignmentValue, pValue, rValue);
        Variant[] variant = snpSampler.sample().value();

        assertEquals(a.nchar().intValue() * p.doubleValue() , variant.length, 1000);
        int heter = 0;
        int homo = 0;
        for (Variant v : variant) {
            if (v.getGenotype().equals("0|1")){
                heter ++;
            } else if (v.getGenotype().equals("1|1")){
                homo++;
            }
        }
        double actual = (double) heter / homo;
        assertEquals(r.doubleValue(), actual, 0.05);
    }
}
