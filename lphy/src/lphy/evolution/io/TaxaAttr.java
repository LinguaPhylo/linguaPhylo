package lphy.evolution.io;

import lphy.evolution.Taxa;
import lphy.evolution.Taxon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Guess ages from taxa name.
 * No time unit.
 * TODO what about species?
 * @author Walter Xie
 */
public class TaxaAttr implements Taxa {

//    protected final Pattern regx;
    protected final TipCalibrationType tipCalibType;
    // use map to guarantee mapping correct
    protected Map<String, Double> taxaAgeMap;

    //TODO speciesMap


    public enum TipCalibrationType {
        forward, // virus
        backward // fossils
//        age
    }

    /**
     * To guess the date from taxa names and compute ages
     * according to {@link TipCalibrationType}
     * @param taxaNames    taxa names.
     * @param regxStr      Java regular expression.
     * @param ageTypeStr   age type (i.e. forward, backward, age)
     */
    public TaxaAttr(String[] taxaNames, String regxStr, String ageTypeStr) {
        // lower case, forward backward age
        this.tipCalibType = TipCalibrationType.valueOf(ageTypeStr.toLowerCase());

        String[] datesStr = new String[Objects.requireNonNull(taxaNames).length];
        // guess dates
        final Pattern regx = Pattern.compile(regxStr);
        for (int i = 0; i < taxaNames.length; i++) {
            String tN = taxaNames[i];
            datesStr[i] = getAttr(tN, regx);
        }

        //TODO if val is date format, compute the age based on unit

        createTaxaAgeMap(taxaNames, datesStr);
    }

    /**
     * For {@link ExtNexusImporter}.
     * @param taxaNames   taxa names.
     * @param datesStr     the date strings of corresponding taxa names.
     * @param ageTypeStr  age type (i.e. forward, backward, age).
     * @see TipCalibrationType
     */
    public TaxaAttr(String[] taxaNames, String[] datesStr, String ageTypeStr) {
        // lower case, forward backward age
        this.tipCalibType = TipCalibrationType.valueOf(ageTypeStr.toLowerCase());

        createTaxaAgeMap(taxaNames, datesStr);
    }

    public void createTaxaAgeMap(final String[] taxaNames, final String[] datesStr) {
        double[] ages = parseDateString(datesStr);
        if (ages == null) // if it is date uuuu-MM-dd
            ages = convertDateToAge(datesStr, ChronoUnit.YEARS); // TODO hard code to unit year

        // find min max for forward or backward
        double max = ages[0], min = ages[0];
        for (int i = 1; i < taxaNames.length; i++) {
            if (ages[i] > max) max = ages[i];
            else if (ages[i] < min) min = ages[i];
        }

        taxaAgeMap = new LinkedHashMap<>();
        for (int i = 0; i < taxaNames.length; i++) {

//            if (TipCalibrationType.age.equals(tipCalibType)) {
//                taxaAgeMap.put(taxaNames[i], ages[i]);
            if (TipCalibrationType.forward.equals(tipCalibType)) {
                taxaAgeMap.put(taxaNames[i], max - ages[i]); // like virus
            } else if (TipCalibrationType.backward.equals(tipCalibType)) {
                taxaAgeMap.put(taxaNames[i], ages[i] - min); // like fossils
            } else {
                throw new IllegalArgumentException("Not recognised mode to convert dates to ages : " + tipCalibType);
            }
        }
    }

    // return null, if cannot parseDouble,
    // which assumes the string is a date in uuuu-MM-dd format
    protected double[] parseDateString(final String[] datesStr) {
        double[] ages = new double[Objects.requireNonNull(datesStr).length];
        // parse the age value
        for (int i = 0; i < datesStr.length; i++) {
            try {
                ages[i] = Double.parseDouble(datesStr[i]);
            } catch (NumberFormatException e) {
                // the val is Date not Number
                System.err.println("Warning: the age value (" + datesStr[i] +
                        ") is not numeric, so guessing the date by uuuu-MM-dd");
                return null;
            }
        }
        return ages;
    }

    // convert uuuu-MM-dd to the unit of years in decimal
    protected double[] convertDateToAge(final String[] datesStr, ChronoUnit unit) {
        final String formatter = "uuuu-MM-dd";
        DateTimeFormatter f = DateTimeFormatter.ofPattern(formatter);
        if (!unit.equals(ChronoUnit.YEARS))
            throw new UnsupportedOperationException("Only support year as unit for parsing a date '" + formatter + "' !");

        double[] ages = new double[Objects.requireNonNull(datesStr).length];
        for (int i = 0; i < datesStr.length; i++) {
            try {
                LocalDate date = LocalDate.parse(datesStr[i], f);
                // decimal year, e.g. 1999.55
                ages[i] = date.getYear() + (date.getDayOfYear() - 1.0) / (date.isLeapYear() ? 366.0 : 365.0);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Cannot parse the date string by " + formatter + " ! " + datesStr[i]);
            }
        }
        return ages;
    }

    protected String getAttr(final String taxonName, final Pattern regx) {
        Matcher matcher = regx.matcher(taxonName);
        if (matcher.find())
            return matcher.group(1);
        throw new IllegalArgumentException("Cannot extract attributes from " + taxonName + " using " + regx);
    }

    //*** getters and overrides ***//

    public Map<String, Double> getTaxaAgeMap() {
        return taxaAgeMap;
    }

    public TipCalibrationType getTipCalibrationType() {
        return tipCalibType;
    }

    @Override
    public int ntaxa() {
        return taxaAgeMap.size();
    }

    @Override
    public Double[] getAges() {
        // suppose to maintain the order
        return taxaAgeMap.values().toArray(Double[]::new);
    }

    @Override
    public String[] getTaxaNames() {
        // suppose to maintain the order
        return taxaAgeMap.keySet().toArray(String[]::new);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Taxon taxon : getTaxa()) {
            builder.append(taxon.toString());
            builder.append("\n");
        }
        return builder.toString();
    }
}
