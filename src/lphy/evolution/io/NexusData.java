package lphy.evolution.io;

import lphy.app.HasComponentView;
import lphy.evolution.Taxa;
import lphy.evolution.Taxon;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.traits.CharSetBlock;
import lphy.graphicalModel.MethodInfo;
import lphy.graphicalModel.Value;
import lphy.utils.LoggerUtils;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Raw data from the nexus file, saved as LPhy objects.
 * Not only taxa and sequences, but also ages and other attributes
 * which are inside {@link Taxon}.
 * For example, <code>getData().getTaxa().getAges()</code>.
 *
 * It can be either {@link SimpleAlignment} in the most cases,
 * or {@link lphy.evolution.alignment.ContinuousCharacterData}.
 * They are the data directly converted from MATRIX,
 * where partitioning will be handled in the runtime.
 * If there is charset defined in the nexus, they will store
 * in {@link #charsetMap}, the same is age/date in {@link #ageStringMap}.
 *
 * @author Walter Xie
 */
public class NexusData implements HasComponentView<String> {//implements TaxaCharacterMatrix {

    protected final String fileName; // for caching data

    // for alignment before partitioning by charsets
    protected Alignment alignment;
    // TODO separate class for continuous data ?
//    protected TaxaCharacterMatrix data;

    // if null, then no charset in the nexus file
    protected Map<String, List<CharSetBlock>> charsetMap;

    // store strings for processing later, if tip calibration,
    // but dates will be parsed and go to Taxon eventually.
    // Taxon name <=> age/date string
    protected Map<String, String> ageStringMap;
    protected double minAge;
    protected double maxAge;
    // SCALE, TODO fix to year at the moment
    protected ChronoUnit chronoUnit = ChronoUnit.YEARS;

    // default to forward
    protected AgeDirection ageDirection = AgeDirection.forward;
    protected String ageRegxStr;

    //*** ages ***//

    public enum AgeDirection {
        forward,  // virus
        backward, // fossils
        dates,    // forward
        ages      // backward
    }


    /**
     * @param fileName  for caching data
     */
    public NexusData(String fileName) {
        this.fileName = fileName;
    }

    public NexusData(String fileName, String ageDirectionStr, String ageRegxStr) {
        this(fileName);
        if (ageDirectionStr != null)
            this.ageDirection = getAgeDirection(ageDirectionStr);
        if (ageRegxStr != null)
            this.ageRegxStr = ageRegxStr;
    }

    /**
     * Basic config to determine if to read the file again.
     * @param fileName
     * @param ageDirectionStr
     * @param ageRegxStr
     * @return   if false, then require to read the file.
     */
    public boolean hasSameOptions(String fileName, String ageDirectionStr, String ageRegxStr) {
        return this.fileName.equalsIgnoreCase(fileName) ||
                this.ageDirection.toString().equalsIgnoreCase(ageDirectionStr) ||
                (this.ageRegxStr != null && this.ageRegxStr.equalsIgnoreCase(ageRegxStr) ) ;
    }

    /**
     * Alternative method to set {@link #ageStringMap} from stored {@link Alignment},
     * call {@link #assignAges} after this to store the ages inside taxa.
     * @param ageRegxStr  regx to pull out ages from name strings
     * @see #setAgeStringMap(Map)
     */
    public void setAgeMapFromTaxa(final String ageRegxStr) {
        this.ageRegxStr = ageRegxStr;

        if (this.ageStringMap != null)
            LoggerUtils.log.warning("Overwriting age map which may have been defined in TIPCALIBRATION in the nexus file !");
        this.ageStringMap = new LinkedHashMap<>();

        // guess dates
        final Pattern regx = Pattern.compile(ageRegxStr);

        for (String taxonName : getAlignment().getTaxaNames()) {
            String ageStr = getAttrFirstMatch(taxonName, regx);
            ageStringMap.put(taxonName, ageStr);
        }
    }

    private String getAttrFirstMatch(final String taxonName, final Pattern regx) {
        Matcher matcher = regx.matcher(taxonName);
        if (matcher.find())
            return matcher.group(1);
        throw new IllegalArgumentException("Cannot extract attributes from " + taxonName + " using " + regx);
    }

    /**
     * Parse string ages from {@link #ageStringMap},
     * and assign to {@link Taxa} of {@link Alignment}.
     * @param ageDirectionStr  {@link AgeDirection}
     */
    public void assignAges(final String ageDirectionStr) {

        final Map<String, String> dateMap = getAgeStringMap();
        this.ageDirection = getAgeDirection(ageDirectionStr);

        //*** string processing ***//

        String[] datesStr = dateMap.values().toArray(String[]::new);
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
        String[] taxaNames = dateMap.keySet().toArray(String[]::new);

        if (dateMap.size() != getAlignment().ntaxa())
            throw new IllegalArgumentException("Invalid ages/dates map : size " + dateMap.size() +
                    " !=  taxa " + getAlignment().ntaxa());

        for (int i = 0; i < taxaNames.length; i++) {
            String taxonName = taxaNames[i];
            // make sure use the correct taxon
            int indexOfTaxon = getAlignment().indexOfTaxon(taxonName);
            if (indexOfTaxon < 0)
                throw new RuntimeException("Cannot locate taxon name " + taxonName +
                        " from ages/dates map in getAlignment() taxa " + Arrays.toString(getAlignment().getTaxaNames()));

            if (AgeDirection.forward.equals(ageDirection) || AgeDirection.dates.equals(ageDirection)) {
                // like virus
                getAlignment().getTaxon(indexOfTaxon).setAge(maxAge - vals[i]);
            } else if (AgeDirection.backward.equals(ageDirection)|| AgeDirection.ages.equals(ageDirection)) {
                // like fossils
                getAlignment().getTaxon(indexOfTaxon).setAge(vals[i] - minAge);
            } else {
                throw new IllegalArgumentException("Not recognised age direction to convert dates or ages : " + ageDirection);
            }

        }

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

    // default to forward
    private AgeDirection getAgeDirection(String ageDirectionStr){
        if (ageDirectionStr == null) {
            ageDirectionStr = AgeDirection.forward.toString();
            LoggerUtils.log.severe("Tip calibration type is not defined, set to " + ageDirectionStr + " as default.");
        }
        return AgeDirection.valueOf(ageDirectionStr.toLowerCase());
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

    //*** lphy methods ***//

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
        } else if (hasCharsets()) {
            // There is the partition name in the nexus file
            // IllegalArgumentException if str not exist
            charSetBlocks = getCharSet(str);
        }
        if (charSetBlocks.size() < 1)
            throw new IllegalArgumentException("Not recognised string " + str + " assign to charset !");
        return SimpleAlignment.Utils.getCharSetAlignment(charSetBlocks, getSimpleAlignment());
    }

    @MethodInfo(description="the taxa of alignment.")
    public Taxa taxa() {
        return getAlignment().getTaxa();
    }

    @MethodInfo(description="the nchar of alignment.")
    public int L() {
        return getAlignment().nchar();
    }

    // TODO may change in future
    @MethodInfo(description="get the alignment imported from the nexus file.")
    public Alignment getAlignment() {
        if (alignment == null)
            throw new IllegalArgumentException("Alignment is not available !");
        return alignment;
    }

    //*** getter setter ***//

//    public TaxaCharacterMatrix getData() {
//        return data;
//    }
//
//    public void setData(TaxaCharacterMatrix data) {
//        this.data = data;
//    }

    public SimpleAlignment getSimpleAlignment() {
        if (! (getAlignment() instanceof SimpleAlignment) )
            throw new IllegalArgumentException("Data imported from the nexus file " +
                    "is not a simple alignment ! " + getAlignment().getClass());
        return (SimpleAlignment) getAlignment();
    }

//    public CharSetAlignment getCharSetAlignment() {
//        if (! (getAlignment() instanceof CharSetAlignment) )
//            throw new IllegalArgumentException("Data imported from the nexus file " +
//                    "is not an charset alignment ! " + getAlignment().getClass());
//        return (CharSetAlignment) getAlignment();
//    }

    /**
     * @return  true, if the nexus file defines "TIPCALIBRATION".
     */
    public boolean hasAges() {
        return ! (ageStringMap == null || ageStringMap.size() == 0);
    }

    public Map<String, String> getAgeStringMap() {
        if (ageStringMap == null)
            throw new IllegalArgumentException("No ages are available !\n" +
                    "Either define TIPCALIBRATION in the nexus file, or use regex to extract from names.");
        return ageStringMap; // if null, no TIPCALIBRATION
    }

    public void setAgeStringMap(Map<String, String> ageStringMap) {
        this.ageStringMap = ageStringMap;
    }

    public ChronoUnit getChronoUnit() {
        return chronoUnit;
    }

    public void setChronoUnit(ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    /**
     * @return  true, if the nexus file defines "charset".
     */
    public boolean hasCharsets() {
        return ! (charsetMap == null || charsetMap.size() == 0);
    }

    /**
     * @return  could be null
     * @see #hasCharsets()
     */
    public Map<String, List<CharSetBlock>> getCharsetMap() {
        // if (charsetMap == null) charsetMap = new TreeMap<>();
        return charsetMap;
    }

    public void setCharsetMap(Map<String, List<CharSetBlock>> charsetMap) {
        this.charsetMap = charsetMap;
    }

    public List<CharSetBlock> getCharSet(String partName) {
        List<CharSetBlock> blocks = charsetMap.get(partName);
        if (blocks == null)
            throw new IllegalArgumentException("Charset name " + partName + " not exist !");
        return blocks;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public JComponent getComponent(Value<String> value) {
        StringBuilder sb = new StringBuilder();
        sb.append("file = ").append( getFileName() );
        if (charsetMap != null) {
            sb.append("\n").append( charsetMap.toString() );
        }
        if (ageStringMap != null) {
            sb.append("\nageDirection = ").append( ageDirection );
            // wrap map string by comma
            sb.append("\nages = ").append( ageStringMap.toString().replaceAll(",", ",\n") );
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);

        return textArea;
    }

}
