package lphy.evolution.io;

import lphy.evolution.Taxon;
import lphy.utils.LoggerUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create Taxon map from dates. No time unit.
 * @author Walter Xie
 */
public class TaxaData { // TODO Make it extends Taxa.Simple, or merge to NexusOptions?

//    protected final Pattern regx;
    protected final AgeDirection ageDirection;
    // use map to guarantee mapping correct
    @Deprecated
    protected Map<String, Taxon> taxonMap;

    public enum AgeDirection {
        forward,  // virus
        backward, // fossils
        dates,    // forward
        ages      // backward
    }

    /**
     * To extract the date from taxa names and compute ages
     * according to {@link AgeDirection}
     * @param idMap    taxa names Map<String, Integer>.
     * @param regxStr      Java regular expression.
     * @param ageDirectionStr   tip calibration type (i.e. forward, backward).
     */
    public TaxaData(Map<String, Integer> idMap, String regxStr, String ageDirectionStr) {
        this.ageDirection = initType(ageDirectionStr);

        String[] datesStr = new String[Objects.requireNonNull(idMap).size()];
        String[] taxaNames = new String[idMap.size()];
        // guess dates
        final Pattern regx = Pattern.compile(regxStr);
        for (Map.Entry<String, Integer> entry : idMap.entrySet()) {
            datesStr[entry.getValue()] = getAttr(entry.getKey(), regx);
            taxaNames[entry.getValue()] = entry.getKey();
        }

        createTaxaAgeMap(taxaNames, datesStr);
    }

    /**
     * For {@link ExtNexusImporter}.
     * @param taxaNames   taxa names.
     * @param datesStr     the date strings of corresponding taxa names.
     * @param ageDirectionStr  {@link AgeDirection} in string, either forward or backward.
     */
    public TaxaData(String[] taxaNames, String[] datesStr, String ageDirectionStr) {
        this.ageDirection = initType(ageDirectionStr);

        createTaxaAgeMap(taxaNames, datesStr);
    }

    private AgeDirection initType(String ageDirectionStr){
        if (ageDirectionStr == null) { // default to forward
            ageDirectionStr = AgeDirection.forward.toString();
            LoggerUtils.log.severe("Tip calibration type is not defined, set to " + ageDirectionStr + " as default.");
        }
        return AgeDirection.valueOf(ageDirectionStr.toLowerCase());
    }

    public void createTaxaAgeMap(final String[] taxaNames, final String[] datesStr) {
        double[] vals = parseDateString(datesStr);

        if (vals == null) {// if it is date uuuu-MM-dd
            assert AgeDirection.forward.equals(ageDirection) || AgeDirection.dates.equals(ageDirection);
            // only forward in time using dates
            //TODO compute the value based on months, days.
            vals = convertDateToAge(datesStr, ChronoUnit.YEARS); // TODO hard code to unit year
        }
        // find min max for forward or backward
        double max = vals[0], min = vals[0];
        for (int i = 1; i < taxaNames.length; i++) {
            if (vals[i] > max) max = vals[i];
            else if (vals[i] < min) min = vals[i];
        }

        taxonMap = new LinkedHashMap<>();
        for (int i = 0; i < taxaNames.length; i++) {

//            if (TipCalibrationType.age.equals(tipCalibType)) {
//                taxaAgeMap.put(taxaNames[i], vals[i]);
            if (AgeDirection.forward.equals(ageDirection) || AgeDirection.dates.equals(ageDirection)) {
                // like virus
                taxonMap.put(taxaNames[i], new Taxon(taxaNames[i], max - vals[i]) );
            } else if (AgeDirection.backward.equals(ageDirection)|| AgeDirection.ages.equals(ageDirection)) {
                // like fossils
                taxonMap.put(taxaNames[i], new Taxon(taxaNames[i], vals[i] - min) );
            } else {
                throw new IllegalArgumentException("Not recognised age direction to convert dates or vals : " + ageDirection);
            }
        }
    }

    // return null, if cannot parseDouble,
    // which assumes the string is a date in uuuu-MM-dd format
    protected double[] parseDateString(final String[] datesStr) {
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
    protected double[] convertDateToAge(final String[] datesStr, ChronoUnit unit) {
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

    protected String getAttr(final String taxonName, final Pattern regx) {
        Matcher matcher = regx.matcher(taxonName);
        if (matcher.find())
            return matcher.group(1);
        throw new IllegalArgumentException("Cannot extract attributes from " + taxonName + " using " + regx);
    }

    //*** getters and overrides ***//

    public Map<String, Taxon> getTaxonMap() {
        return taxonMap;
    }

    public Taxon[] getTaxa() {
        return taxonMap.values().toArray(Taxon[]::new);
    }

    public AgeDirection getAgeDirection() {
        return ageDirection;
    }

}
