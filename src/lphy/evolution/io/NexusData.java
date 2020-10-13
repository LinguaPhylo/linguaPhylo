package lphy.evolution.io;

import lphy.evolution.Taxa;
import lphy.evolution.Taxon;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.SimpleAlignment;
import lphy.evolution.alignment.TaxaCharacterMatrix;
import lphy.evolution.traits.CharSetBlock;
import lphy.utils.LoggerUtils;

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
 * For example, <code>getData().getTaxa().getAges()</code>
 *
 * @author Walter Xie
 */
public class NexusData<T> {

    protected final String fileName; // for caching data

    // for alignment before partitioning by charsets, and continuous data
    protected TaxaCharacterMatrix<T> data;

    protected Map<String, List<CharSetBlock>> charsetMap;

    // store strings for processing later, if tip calibration,
    // but dates will be parsed and go to Taxon eventually.
    protected Map<String, String> ageStringMap; // Taxon name <=> age/date string
    protected double minAge;
    protected double maxAge;
    // SCALE, TODO fix to year at the moment
    protected ChronoUnit chronoUnit = ChronoUnit.YEARS;


    // nexus file needs to re-load again if data is changed.
    public NexusData(String fileName) {
        this.fileName = fileName;
    }

    //*** ages ***//

    public enum AgeDirection {
        forward,  // virus
        backward, // fossils
        dates,    // forward
        ages      // backward
    }

    private AgeDirection getAgeDirection(String ageDirectionStr){
        if (ageDirectionStr == null) { // default to forward
            ageDirectionStr = AgeDirection.forward.toString();
            LoggerUtils.log.severe("Tip calibration type is not defined, set to " + ageDirectionStr + " as default.");
        }
        return AgeDirection.valueOf(ageDirectionStr.toLowerCase());
    }


    /**
     * Alternative method to set {@link #ageStringMap},
     * call {@link #assignAges} after this to store the ages inside taxa.
     * @param taxa        could be {@link Alignment} as well
     * @param ageRegxStr  regx to pull out ages from name strings
     * @see #setAgeStringMap(Map)
     */
    public void setAgeMapFromTaxa(Taxa taxa, final String ageRegxStr) {
        if (this.ageStringMap != null)
            LoggerUtils.log.warning("Overwriting age map which may have been defined in TIPCALIBRATION in the nexus file !");
        this.ageStringMap = new LinkedHashMap<>();

        // guess dates
        final Pattern regx = Pattern.compile(ageRegxStr);

        for (String taxonName : taxa.getTaxaNames()) {
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
     * @param taxa             could be {@link Alignment} as well
     * @param ageDirectionStr  {@link AgeDirection}
     */
    public void assignAges(Taxa taxa, final String ageDirectionStr) {

        final Map<String, String> dateMap = getAgeStringMap();
        final AgeDirection ageDirection = getAgeDirection(ageDirectionStr);

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

        if (dateMap.size() != taxa.ntaxa())
            throw new IllegalArgumentException("Invalid ages/dates map : size " + dateMap.size() +
                    " !=  taxa " + taxa.ntaxa());

        for (int i = 0; i < taxaNames.length; i++) {
            String taxonName = taxaNames[i];
            // make sure use the correct taxon
            int indexOfTaxon = taxa.indexOfTaxon(taxonName);
            if (indexOfTaxon < 0)
                throw new RuntimeException("Cannot locate taxon name " + taxonName +
                        " from ages/dates map in alignment taxa " + Arrays.toString(taxa.getTaxaNames()));

            if (AgeDirection.forward.equals(ageDirection) || AgeDirection.dates.equals(ageDirection)) {
                // like virus
                taxa.getTaxon(indexOfTaxon).setAge(maxAge - vals[i]);
            } else if (AgeDirection.backward.equals(ageDirection)|| AgeDirection.ages.equals(ageDirection)) {
                // like fossils
                taxa.getTaxon(indexOfTaxon).setAge(vals[i] - minAge);
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
                System.err.println("Warning: the value (" + datesStr[i] +
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

    //*** getter setter ***//

    public SimpleAlignment getSimpleAlignment() {
        if (! (Objects.requireNonNull(data) instanceof SimpleAlignment) )
            throw new IllegalArgumentException("Data imported from the nexus file " +
                    "is not an alignment ! " + data.getClass());
        return (SimpleAlignment) data;
    }

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

    public TaxaCharacterMatrix<T> getData() {
        return data;
    }

    public void setData(TaxaCharacterMatrix<T> data) {
        this.data = data;
    }

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

}
