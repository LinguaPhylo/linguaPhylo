package lphy.evolution.io;

import jebl.evolution.sequences.SequenceType;
import lphy.app.HasComponentView;
import lphy.evolution.Taxa;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.AlignmentUtils;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.traits.CharSetBlock;
import lphy.graphicalModel.MethodInfo;
import lphy.graphicalModel.Value;
import lphy.utils.LoggerUtils;

import javax.swing.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Walter Xie
 */
public class NexusAlignment extends SimpleAlignment implements NexusData<Integer>, HasComponentView<Alignment> {

    // if null, then no charset in the nexus file
    protected Map<String, List<CharSetBlock>> charsetMap;

    // ages/dates will be parsed and go to Taxon.
    protected double minAge;
    protected double maxAge;
    // SCALE, TODO fix to year at the moment
    protected ChronoUnit chronoUnit = ChronoUnit.YEARS;

    // default to forward
    protected AgeDirection ageDirection = AgeDirection.forward;
    protected String ageRegxStr;


    public NexusAlignment(Taxa taxa, int nchar, SequenceType sequenceType) {
        super(taxa, nchar, sequenceType);
    }


    //*** ages ***//

    /**
     * Parse age/date string in Map, and assign to {@link Taxa}.
     * @param ageStringMap  Taxon name <=> age/date string,
     * @param ageDirectionStr  {@link AgeDirection}
     */
    public void assignAges(final Map<String, String> ageStringMap, final String ageDirectionStr) {

        this.ageDirection = getAgeDirection(ageDirectionStr);

        //*** string processing ***//

        String[] datesStr = Objects.requireNonNull(ageStringMap).values().toArray(String[]::new);
        // check if double, or date format (return null)
        double[] vals = parseDateString(datesStr);

        if (vals == null) {// if it is date uuuu-MM-dd
            assert AgeDirection.forward.equals(ageDirection) || AgeDirection.dates.equals(ageDirection);
            // only forward in time using dates
            //TODO hard code to unit year, require to compute the value based on months, days.
            vals = convertDateToAge(datesStr, ChronoUnit.YEARS);
        }
        // find min max for forward or backward
        maxAge = vals[0];
        minAge = vals[0];
        for (int i = 1; i < vals.length; i++) {
            if (vals[i] > maxAge) maxAge = vals[i];
            else if (vals[i] < minAge) minAge = vals[i];
        }

        //*** assign ages ***//

        // same order as String[] datesStr
        String[] taxaNames = ageStringMap.keySet().toArray(String[]::new);

        if (ageStringMap.size() != ntaxa())
            throw new IllegalArgumentException("Invalid ages/dates map : size " + ageStringMap.size() +
                    " !=  taxa " + ntaxa());

        for (int i = 0; i < taxaNames.length; i++) {
            String taxonName = taxaNames[i];
            // make sure use the correct taxon
            int indexOfTaxon = indexOfTaxon(taxonName);
            if (indexOfTaxon < 0)
                throw new RuntimeException("Cannot locate taxon name " + taxonName +
                        " from ages/dates map in getAlignment() taxa " + Arrays.toString(getTaxaNames()));

            if (AgeDirection.forward.equals(ageDirection) || AgeDirection.dates.equals(ageDirection)) {
                // like virus
                getTaxon(indexOfTaxon).setAge(maxAge - vals[i]);
            } else if (AgeDirection.backward.equals(ageDirection)|| AgeDirection.ages.equals(ageDirection)) {
                // like fossils
                getTaxon(indexOfTaxon).setAge(vals[i] - minAge);
            } else {
                throw new IllegalArgumentException("Not recognised age direction to convert dates or ages : " + ageDirection);
            }

        }

    }

    /**
     * @param ageRegxStr  Java regular expression to extract dates from taxa names.
     * @param ageDirectionStr  {@link AgeDirection}
     * @return TreeMap of Taxon name <=> age/date string,
     *         which can be alternatively obtained from the nexus file.
     */
    public void setAgesFromTaxaName(final String ageRegxStr, final String ageDirectionStr) {
        this.ageRegxStr = ageRegxStr;

        if (hasAges())
            LoggerUtils.log.warning("Overwriting ages in taxa, which may have been defined by " +
                    "TIPCALIBRATION in the nexus file !");

        Map<String, String> ageStringMap = new TreeMap<>();
        // guess dates
        final Pattern regx = Pattern.compile(ageRegxStr);

        for (String taxonName : getTaxaNames()) {
            String ageStr = getAttrFirstMatch(taxonName, regx);
            ageStringMap.put(taxonName, ageStr);
        }

        assignAges(ageStringMap, ageDirectionStr);
    }

    //*** ChronoUnit ***//

    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }

    public void setChronoUnit(ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;
    }

    //*** charsets ***//

    @Override
    @MethodInfo(description="return a partition alignment. " +
            "If the string doesn't match charset's syntax, then check if the string matches " +
            "a defined name in the nexus file. Otherwise it is an error. " +
            "The string is referred to one partition at a call, but can be multiple blocks, " +
            "such as d.charset(\"2-457\\3 660-896\\3\")." )
    public Alignment charset(String str) {
        List<CharSetBlock> charSetBlocks = new ArrayList<>();
        //*** charsets or part names ***//
        if (CharSetBlock.Utils.isValid(str)) {
            // is charset
            charSetBlocks = CharSetBlock.Utils.getCharSetBlocks(str);
        } else if (hasCharsets(charsetMap)) {
            // There is the partition name in the nexus file
            // IllegalArgumentException if str not exist
            charSetBlocks = getCharSet(str, charsetMap);
        }
        if (charSetBlocks.size() < 1)
            throw new IllegalArgumentException("Not recognised string " + str + " assign to charset !");
        return AlignmentUtils.getCharSetAlignment(charSetBlocks, this);
    }


    @Override
    public void setCharsetMap(Map<String, List<CharSetBlock>> charsetMap) {
        this.charsetMap = charsetMap;
    }



    @Override
    public JComponent getComponent(Value<Alignment> value) {
        StringBuilder sb = new StringBuilder(super.toString());
        if (charsetMap != null) {
            sb.append("\n").append( charsetMap.toString() );
        }
        if (hasAges()) {
            sb.append("\nageDirection = ").append( ageDirection );
            // wrap map string by comma
            sb.append("\nages = ").append( Arrays.toString( getAges() ) );
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);

        return textArea;
    }


}
