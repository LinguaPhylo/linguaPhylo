package lphy.evolution.io;

import lphy.evolution.Taxa;
import lphy.evolution.alignment.Alignment;
import lphy.evolution.alignment.TaxaCharacterMatrix;
import lphy.evolution.traits.CharSetBlock;
import lphy.graphicalModel.MethodInfo;
import lphy.graphicalModel.MultiDimensional;
import lphy.utils.LoggerUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The data object to contain the lphy objects parsed from the nexus file.
 * It extends either Alignment or ContinuousCharacterData.
 * @see NexusParser
 * @author Walter Xie
 */
public interface MetaData<T> extends Taxa, TaxaCharacterMatrix<T>, MultiDimensional {

    //*** age direction ***//

    public enum AgeDirection {
        forward,  // virus
        backward, // fossils
        dates,    // forward
        ages      // backward
    }

    // default to forward
    default AgeDirection getAgeDirection(String ageDirectionStr){
        if (ageDirectionStr == null) {
            ageDirectionStr = AgeDirection.forward.toString();
            LoggerUtils.log.severe("Tip calibration type is not defined, set to " + ageDirectionStr + " as default.");
        }
        return AgeDirection.valueOf(ageDirectionStr.toLowerCase());
    }

    //*** ChronoUnit ***//

    void setChronoUnit(ChronoUnit chronoUnit);

    ChronoUnit getChronoUnit();

    //*** ages ***//

    /**
     * Parse age/date string in Map, and assign to {@link Taxa}.
     * @param ageStringMap  Taxon name <=> age/date string,
     * @param ageDirectionStr  {@link AgeDirection}
     */
    void assignAges(final Map<String, String> ageStringMap, final String ageDirectionStr);

    /**
     * @param ageRegxStr  Java regular expression to extract dates from taxa names.
     * @param ageDirectionStr  {@link AgeDirection}
     * @return TreeMap of Taxon name <=> age/date string,
     *         which can be alternatively obtained from the nexus file.
     */
    void setAgesFromTaxaName(final String ageRegxStr, final String ageDirectionStr);

    /**
     * @param taxonName
     * @param regx
     * @return  extracted attribute from a taxon name using regx
     */
    default String getAttrFirstMatch(final String taxonName, final Pattern regx) {
        Matcher matcher = regx.matcher(taxonName);
        if (matcher.find())
            return matcher.group(1);
        throw new IllegalArgumentException("Cannot extract attributes from " + taxonName + " using " + regx);
    }

    // return null, if cannot parseDouble,
    // which assumes the string is a date in uuuu-MM-dd format
    default double[] parseDateString(final String[] datesStr) {
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
    default double[] convertDateToAge(final String[] datesStr, ChronoUnit unit) {
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

    //*** charsets ***//

    @MethodInfo(description="return a partition. " +
            "If the string doesn't match charset's syntax, then check if the string matches " +
            "a defined name in the nexus file. Otherwise it is an error. " +
            "The string is referred to one partition at a call, but can be multiple blocks, " +
            "such as d.charset(\"2-457\\3 660-896\\3\")." )
    TaxaCharacterMatrix<T> charset(String str);

    /**
     * @param charsetMap  obtained from NexusImporter
     * @return  true, if the nexus file defines "charset".
     */
    default boolean hasCharsets(Map<String, List<CharSetBlock>> charsetMap) {
        return ! (charsetMap == null || charsetMap.size() == 0);
    }

    /**
     * @param partName     the charset name defined in the nexus file.
     * @param charsetMap   obtained from NexusImporter
     * @return    the List<CharSetBlock> matching to the charset name defined in the nexus file.
     */
    default List<CharSetBlock> getCharSet(String partName, Map<String, List<CharSetBlock>> charsetMap) {
        List<CharSetBlock> blocks = Objects.requireNonNull(charsetMap).get(partName);
        if (blocks == null)
            throw new IllegalArgumentException("Charset name " + partName + " not exist !");
        return blocks;
    }

    //*** getter / setter ***//

    void setCharsetMap(Map<String, List<CharSetBlock>> charsetMap);

    Map<String, List<CharSetBlock>> getCharsetMap();

    // TODO no Taxa.hasAges()
    boolean hasAges();

    AgeDirection getAgeDirection(); // no setter, given in assignAges() and setAgesFromTaxaName()

    //*** trait ***//
    @MethodInfo(description="return a trait alignment. " +
            "The regular expression is the separator to split the taxa names, " +
            "and i (>=0) is the index to extract the trait value." )
    Alignment trait(String sepRegex, Integer i);

    /**
     * @param sb  there should be an alignment description in the begin.
     * @return    a summary of loading nexus file.
     */
    default String toString(StringBuilder sb) {
        if (getCharsetMap() != null)
            sb.append(", ").append( getCharsetMap().size() ).append(" charset(s)");
        if (hasAges())
            sb.append(", age direction is ").append( getAgeDirection() );
        return sb.toString();
    }


}
