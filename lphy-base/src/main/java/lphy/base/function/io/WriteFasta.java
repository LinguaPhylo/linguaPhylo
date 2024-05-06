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


    @GeneratorInfo(name="fasta", verbClause = "can be saved as a fasta format into", narrativeName = "file",
            category = GeneratorCategory.TAXA_ALIGNMENT, examples = {"jcc2Fasta.lphy"},
            description = "A function that returns an alignment which can be saved as a fasta file later " +
                    "using lphy studio or slphy.")
    public Value<FastaAlignment> apply() {

        Alignment alignment = ((Value<Alignment>) getParams().get(ReaderConst.ALIGNMENT)).value();

        // this only creates taxa and nchar
        FastaAlignment faData = new FastaAlignment(alignment);

        return new Value<>(null, faData, this);

    }



}
