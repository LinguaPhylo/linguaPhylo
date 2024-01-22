package lphy.base.function.io;

import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.alignment.FastaAlignment;
import lphy.core.model.DeterministicFunction;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorCategory;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;

public class WriteFasta extends DeterministicFunction<FastaAlignment> {


    public WriteFasta(@ParameterInfo(name = ReaderConst.ALIGNMENT,
                              description = "the lphy alignment that is written into the fasta file.")
                      Value<Alignment> alignmentValue ) {


        if (alignmentValue == null) throw new IllegalArgumentException("The alignment can't be null!");
        setParam(ReaderConst.ALIGNMENT, alignmentValue);
    }


    @GeneratorInfo(name="fasta", verbClause = "is read from", narrativeName = "fasta file",
            category = GeneratorCategory.TAXA_ALIGNMENT, examples = {"covidDPG.lphy"},
            description = "A function that parses an alignment from a fasta file.")
    public Value<FastaAlignment> apply() {

        Alignment alignment = ((Value<Alignment>) getParams().get(ReaderConst.ALIGNMENT)).value();

        // this only creates taxa and nchar
        FastaAlignment faData = new FastaAlignment(alignment.nchar(), alignment);

        // fill in states
        for (int i=0; i < alignment.ntaxa(); i++) {
            for (int j = 0; j < alignment.nchar(); j++) {
                faData.setState(i, j, alignment.getState(i, j));
            }
        }

        return new Value<>(null, faData, this);

    }



}
