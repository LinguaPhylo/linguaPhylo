package lphy.base.evolution;

import jebl.evolution.sequences.Nucleotides;
import lphy.base.distribution.BernoulliMulti;
import lphy.base.distribution.ParametricDistribution;
import lphy.base.distribution.UniformDiscrete;
import lphy.base.evolution.alignment.Alignment;
import lphy.base.evolution.datatype.Variant;
import lphy.base.function.io.ReaderConst;
import lphy.core.model.RandomVariable;
import lphy.core.model.Value;
import lphy.core.model.annotation.GeneratorInfo;
import lphy.core.model.annotation.ParameterInfo;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.*;

import static lphy.base.evolution.datatype.Variant.getGenotype;

public class SNPSampler extends ParametricDistribution<Variant[]> {
    public final String probabilityName = "p";
    public final String ratioName = "r";
    Value<Alignment> alignment;
    Value<Number> p;
    Value<Number> r;

    public SNPSampler(@ParameterInfo(name = ReaderConst.ALIGNMENT, description = "the one sequence alignment") Value<Alignment> alignment,
                      @ParameterInfo(name = probabilityName, description = "the probability of each site to be SNP, deafult to be 0.01", optional = true) Value<Number> p,
                      @ParameterInfo(name = ratioName, description = "the ratio of heterozygousSNPs and non-reference homozygous SNPs, " +
                               "default all SNPs are heterozygous", optional = true) Value<Number> r) {

        if (alignment == null) throw new IllegalArgumentException("The alignment can't be null!");
        if (alignment.value().length() >= 1) throw new IllegalArgumentException("The alignment should be one sequence alignment");
        if (! alignment.getType().equals(Nucleotides.NAME)) throw new IllegalArgumentException("Only take haploid alignment!");
        setParam(ReaderConst.ALIGNMENT, alignment);

        if (p != null){
            setParam(probabilityName, p);
        }

        if (r != null){
            setParam(ratioName, r);
        }
    }
    @Override
    protected void constructDistribution(RandomGenerator random) {

    }

    @GeneratorInfo(name = "SNPSampler", examples = {""},
            description = "Sample SNPs with the given alignment by sampling mutation sites using a binomial distribution (size = number of sites)." +
                    "Take haploid alignment only, take the site as ref and randomly sample an alt.")
    @Override
    public RandomVariable<Variant[]> sample() {
        // get parameter value
        Alignment alignment = getAlignment().value();

        Number p;
        if (getProbability()!= null){
             p = getProbability().value();
        } else {
            p = 0.001;
        }

        Number r;
        if (getRatio()!=null){
            r = getRatio().value();
        }

        // initialise the output snps
        List<Variant> snpList = new ArrayList<>();

        BernoulliMulti bm = new BernoulliMulti(new Value<>("id", (double) p), new Value<>("id", alignment.nchar()), null);
        Boolean[] snp_mask = bm.sample().value();

        String taxaName = alignment.getTaxonName(0);

        // TODO: deal with heterozygous/non-ref homo rate
        for (int i = 0; i < snp_mask.length; i++) {
            if (snp_mask[i]) {
                int position = i+1;
                int ref = getAmbiguousStateIndex(alignment.getState(0,i));
                int alt = getRandomCanonicalState(ref);
                String genotype = getGenotype(ref,alt);
                Variant snp = new Variant(taxaName, position, ref, alt, genotype);

                snpList.add(snp);
            }
        }

        return new RandomVariable<>(null, snpList.toArray(new Variant[0]), this);
    }

    /**
     * @param stateIndex state index of the nucleotide
     * @return the certain homozygous phased genotype state index
     */
    public static int getAmbiguousStateIndex(int stateIndex) {
        if (stateIndex >= 4) {
            // get the array for the states
            int[] ambiguousState = ambiguousState(stateIndex);
            // get the Value<Integer> for the lower and upper boundary
            Value<Integer> lower = new Value<>("id", 0);
            Value<Integer> upper = new Value<>("id", ambiguousState.length - 1);

            // get the random index for the integer in the array
            UniformDiscrete uniformDiscrete = new UniformDiscrete(lower, upper);
            RandomVariable<Integer> randomNumber = uniformDiscrete.sample();

            // give the stateIndex its certain state
            stateIndex = ambiguousState[randomNumber.value()];
        }
        return stateIndex;
    }

    /**
     * @param stateIndex the state index of nucleotide
     * @return the array of all possible states indices of the ambiguous nucleotide states (unkown and gap states have
     * all four possible states)
     */

    private static int[] ambiguousState(int stateIndex) {
        // switch the ambiguous states into canonical states (0=A, 1=C, 2=G, 3=T)
        switch (stateIndex) {
            case 4:
                // 4 = A/G
                return new int[]{0, 2};
            case 5:
                // 5 = C/T
                return new int[]{1, 3};
            case 6:
                // 6 = A/C
                return new int[]{0, 1};
            case 7:
                // 7 = A/T
                return new int[]{0, 3};
            case 8:
                // 8 = C/G
                return new int[]{1, 2};
            case 9:
                // 9 = G/T
                return new int[]{2, 3};
            case 10:
                // 10 = C/G/T
                return new int[]{1, 2, 3};
            case 11:
                // 11 = A/G/T
                return new int[]{0, 2, 3};
            case 12:
                // 12 = A/C/T
                return new int[]{0, 1, 3};
            case 13:
                // 13 = A/C/G
                return new int[]{0, 1, 2};
            case 14, 16, 15:
                // 14 = unkown base (N) = A/C/G/T
                // 15 = unkown base (?) = A/C/G/T
                // 16 = gap (-) = A/C/G/T
                return new int[]{0, 1, 2, 3};
            default:
                throw new IllegalArgumentException("Unexpected state: " + stateIndex);
        }
    }

    public static int getRandomCanonicalState(int refIndex) {
        List<Integer> stateIndices = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        stateIndices.remove(refIndex);

        int randomIndex = sampleRandomNumber(0, stateIndices.size() - 1);
        return stateIndices.get(randomIndex);
    }

    private static int sampleRandomNumber(int min, int max) {
        Value<Integer> lower = new Value<>("low", min);
        Value<Integer> upper = new Value<>("high",max);
        UniformDiscrete uniformDiscrete = new UniformDiscrete(lower, upper);
        RandomVariable<Integer> num = uniformDiscrete.sample();

        return num.value();
    }

    @Override
    public Map<String, Value> getParams() {
        Map<String, Value> params = new TreeMap<>();
        if (ReaderConst.ALIGNMENT != null) params.put(ReaderConst.ALIGNMENT, alignment);
        if (probabilityName != null) params.put(probabilityName, p);
        if (ratioName != null) params.put(ratioName, r);
        return params;
    }

    public void setParam(String paramName, Value value){
        if (paramName.equals(ReaderConst.ALIGNMENT)) alignment = value;
        else if (paramName.equals(probabilityName)) p = value;
        else if (paramName.equals(ratioName)) r = value;
    }

    public Value<Alignment> getAlignment() {
        return getParams().get(ReaderConst.ALIGNMENT);
    }

    public Value<Number> getProbability() {
        return getParams().get(probabilityName);
    }

    public Value<Number> getRatio() {
        return getParams().get(ratioName);
    }


}
