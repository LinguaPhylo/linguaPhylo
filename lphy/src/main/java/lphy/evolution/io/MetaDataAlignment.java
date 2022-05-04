package lphy.evolution.io;

import jebl.evolution.sequences.SequenceType;
import lphy.evolution.Taxa;
import lphy.evolution.Taxon;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.AlignmentUtils;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.traits.CharSetBlock;
import lphy.graphicalModel.MethodInfo;
import lphy.util.LoggerUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The meta data parsed from a nexus file and stored in {@link Taxon},
 * and the charsets.
 * @see NexusParser
 * @author Walter Xie
 */
public class MetaDataAlignment extends SimpleAlignment {

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

    protected String spRegxStr;

    //*** age direction ***//

    public enum AgeDirection {
        forward,  // virus
        backward, // fossils
        dates,    // forward
        ages      // backward
    }


    public MetaDataAlignment(Taxa taxa, int nchar, SequenceType sequenceType) {
        super(taxa, nchar, sequenceType);
    }


    //*** ages ***//

    /**
     * Parse age/date string in Map, and assign to {@link Taxa}.
     * @param ageStringMap  Taxon name &lt;=&gt; age/date string,
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
     * TreeMap of Taxon name &lt;=&gt; age/date string,
     * which can be alternatively obtained from the nexus file.
     * @param ageRegxStr  Java regular expression to extract dates from taxa names.
     * @param ageDirectionStr  {@link AgeDirection}
     */
    public void setAgesParsedFromTaxaName(final String ageRegxStr, final String ageDirectionStr) {
        this.ageRegxStr = ageRegxStr;

        Map<String, String> ageStringMap = new TreeMap<>();
        // guess dates
        final Pattern regx = Pattern.compile(ageRegxStr);

        for (String taxonName : getTaxaNames()) {
            // TODO take nth element given separator
            String ageStr = getAttrFirstMatch(taxonName, regx);
            ageStringMap.put(taxonName, ageStr);
        }

        assignAges(ageStringMap, ageDirectionStr);
    }

    public void setSpeciesParsedFromTaxaName(String spRegxStr) {
        this.spRegxStr = spRegxStr;
        // guess species
        final Pattern regx = Pattern.compile(spRegxStr);

        for (Taxon taxon : getTaxonArray()) {
            String taxonName = taxon.getName();
            String spStr = getAttrFirstMatch(taxonName, regx);
            taxon.setSpecies(Objects.requireNonNull(spStr));
        }
    }

    //*** ChronoUnit ***//

    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }

    public void setChronoUnit(ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;
    }

    //*** charsets ***//

    @MethodInfo(description="return a partition alignment. " +
            "If the string doesn't match charset's syntax, then check if the string matches " +
            "a defined name in the nexus file. Otherwise it is an error. " +
            "The string is referred to one partition at a call, but can be multiple blocks, " +
            "such as d.charset(\"2-457\\3 660-896\\3\").", narrativeName = "character set")
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

//    @MethodInfo(description="return a trait alignment, which contains the set of traits<br>" +
//            "extracted from taxa names in this alignment.<br>" +
//            "The <i>sepStr</i> is the substring to split the taxa names,<br>" +
//            "where Java regular expression escape characters will be given no special meaning.<br>" +
//            "The <i>i</i> (>=0) is the index to extract the trait value." )
//    public Alignment extractTrait(String sepStr, Integer i) {
//        String[] taxaNames = this.getTaxaNames();
//        String[] traitVal = new String[taxaNames.length];
//
//        for (int t = 0; t < taxaNames.length; t++) {
//            String[] parts = taxaNames[t].split(Pattern.quote(sepStr));
//            if (parts.length > i)
//                traitVal[t] = parts[i];
//            else
//                throw new IllegalArgumentException("Cannot find " + i +
//                        "th element after splitting name " + taxaNames[t] + " by substring " + sepStr);
//        }
//        // no sorting demes
//        Set<String> uniqTraitVal = new LinkedHashSet<>(Arrays.asList(traitVal));
//        List<String> uniqueDemes = new ArrayList<>(uniqTraitVal);
//        // state names are sorted unique demes
//        Standard standard = new Standard(uniqueDemes);
//        SimpleAlignment traitAl = new SimpleAlignment(this.getTaxa(), 1, standard);
//        // fill in trait values, traitVal and taxaNames have to maintain the same order
//        for (int t = 0; t < traitVal.length; t++) {
//            int demeIndex = standard.getStateNameIndex(traitVal[t]);
//            traitAl.setState(t, 0, demeIndex);
//        }
//        return traitAl;
//    }

    public void setCharsetMap(Map<String, List<CharSetBlock>> charsetMap) {
        this.charsetMap = charsetMap;
    }

    public Map<String, List<CharSetBlock>> getCharsetMap() {
        return charsetMap;
    }

    public AgeDirection getAgeDirection() {
        return ageDirection;
    }

    //*** summary ***//

    /**
     * @return a summary of loading nexus file.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (getCharsetMap() != null)
            sb.append(", ").append( getCharsetMap().size() ).append(" charset(s)");
        if (isUltrametric())
            sb.append(", age direction is ").append( getAgeDirection() );
        return sb.toString();
    }

//    @Override
//    public JComponent getComponent(Value<Alignment> value) {
//        StringBuilder sb = new StringBuilder(super.toString());
//        if (getCharsetMap() != null) {
//            sb.append("\n").append( getCharsetMap().toString() );
//        }
//        if (hasAges()) {
//            sb.append("\nageDirection = ").append( getAgeDirection() );
//            // wrap map string by comma
//            sb.append("\nages = ").append( Arrays.toString( getAges() ) );
//        }
//
//        JTextArea textArea = new JTextArea(sb.toString());
//        textArea.setEditable(false);
//
//        return textArea;
//    }

    //******  private  ******//

    // default to forward
    private AgeDirection getAgeDirection(String ageDirectionStr){
        if (ageDirectionStr == null) {
            ageDirectionStr = AgeDirection.forward.toString();
            LoggerUtils.log.warning("Tip calibration type is not defined, set to " + ageDirectionStr + " as default.");
        }
        return AgeDirection.valueOf(ageDirectionStr.toLowerCase());
    }

    /**
     * @param taxonName
     * @param regx
     * @return  extracted attribute from a taxon name using regx
     */
    private String getAttrFirstMatch(final String taxonName, final Pattern regx) {
        Matcher matcher = regx.matcher(taxonName);
        if (matcher.find())
            return matcher.group(1);
        throw new IllegalArgumentException("Cannot extract attributes from " + taxonName + " using " + regx);
    }

    // return null, if cannot parseDouble,
    // which assumes the string is a date in uuuu-MM-dd format
    private double[] parseDateString(final String[] datesStr) {
        double[] vals = new double[Objects.requireNonNull(datesStr).length];
        // parse the age value
        for (int i = 0; i < datesStr.length; i++) {
            try {
                vals[i] = Double.parseDouble(datesStr[i]);
            } catch (NumberFormatException e) {
                // the val is Date not Number
                LoggerUtils.log.warning("Warning: the value (" + datesStr[i] +
                        ") is not numeric, so guess it is a date by uuuu-MM-dd format");
                return null;
            }
        }
        return vals;
    }

    // convert uuuu-MM-dd to the unit of years in decimal
    private double[] convertDateToAge(final String[] datesStr, ChronoUnit unit) {
        final String formatter = "uuuu-MM-dd";
        DateTimeFormatter f = DateTimeFormatter.ofPattern(formatter);
        if (!unit.equals(ChronoUnit.YEARS))
            throw new UnsupportedOperationException("Only support year as unit for parsing a date '" + formatter + "' !");

        double[] vals = new double[Objects.requireNonNull(datesStr).length];
        for (int i = 0; i < datesStr.length; i++) {
            try {
                LocalDate date = LocalDate.parse(datesStr[i], f);
                // decimal year, e.g. 1999.55
                vals[i] = date.getYear() + (date.getDayOfYear() - 1.0) / (date.isLeapYear() ? 366.0 : 365.0);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Cannot parse the date string by " + formatter + " ! " + datesStr[i]);
            }
        }
        return vals;
    }


    /**
     * @param charsetMap  obtained from NexusImporter
     * @return  true, if the nexus file defines "charset".
     */
    private boolean hasCharsets(Map<String, List<CharSetBlock>> charsetMap) {
        return ! (charsetMap == null || charsetMap.size() == 0);
    }

    /**
     * @param partName     the charset name defined in the nexus file.
     * @param charsetMap   obtained from NexusImporter
     * @return    the List<CharSetBlock> matching to the charset name defined in the nexus file.
     */
    private List<CharSetBlock> getCharSet(String partName, Map<String, List<CharSetBlock>> charsetMap) {
        List<CharSetBlock> blocks = Objects.requireNonNull(charsetMap).get(partName);
        if (blocks == null)
            throw new IllegalArgumentException("Charset name " + partName + " not exist !");
        return blocks;
    }


}
