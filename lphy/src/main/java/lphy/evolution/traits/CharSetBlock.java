package lphy.evolution.traits;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class CharSetBlock {

    private final int from;
    private final int to;
    private final int every;

    public CharSetBlock(int from, int to, int every) {
        this.from = from;
        this.to = to;
        this.every = every;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getEvery() {
        return every;
    }

    @Override
    public String toString() {
        return "CharSet{" +
                "from=" + from +
                ", to=" + to +
                ", every=" + every +
                '}';
    }

    //****** CharSet Utils ******//

    public static class Utils {
        // 1 458-659 3-629\3 662-.\3
        private final static String CHARSET_REGX = "^([0-9]+)$|^([0-9]+)\\-([0-9]+)(\\\\[0-9]+)*$|^([0-9]+)\\-(\\.)(\\\\[0-9]+)*$";

        public static boolean isValid(String str) {
            return Pattern.matches(CHARSET_REGX, str);
        }

        /**
         * @see #parseCharSet(String)
         * @param expression  charset expression, such as "2-457\3 660-896\3",
         *                    or "4-457\3 662-.\3", or "1 458-659".
         * @return
         */
        public static List<CharSetBlock> getCharSetBlocks(String expression) {
            String[] blocks = expression.split("\\s+");
            List<CharSetBlock> charSetBlocks = new ArrayList<>();
            for (String oneBlock : blocks) {
                CharSetBlock charSetBlock = parseCharSet(oneBlock);
                charSetBlocks.add(charSetBlock);
            }
            return charSetBlocks;
        }

        /**
         * @param charSet1Block The string must only contain one block.
         *                      The blocks could be an array, {@code charset="[3-629\3, 4-629\3, 5-629\3]"}.
         *                      The blocks could also be separated by spaces, for example,
         *                      "2-457\3 660-896\3" is considered as 2 blocks.
         *                      Use {@code split("\\s+")} to spilt charset blocks
         *                      before call this method.
         * @return only 1 {@link CharSetBlock} parsed from string
         * @throws IllegalArgumentException
         */
        public static CharSetBlock parseCharSet(String charSet1Block) throws IllegalArgumentException {
            if (!isValid(Objects.requireNonNull(charSet1Block)))
                throw new IllegalArgumentException("Invalid charset expression " + charSet1Block);

            // "4-629\3"
            String[] parts = charSet1Block.split("-");

            int from, to, every = 1;
            try {
                if (parts.length == 2) {
                    // from site
                    from = Integer.parseInt(parts[0].trim());

                    // codons : 629\3
                    if (parts[1].contains("/")) // to avoid typo
                        throw new IllegalArgumentException("Invalid delimiter for codon positions ! " + parts[1]);
                    String[] toParts = parts[1].split("\\\\");
                    // to site
                    if (toParts[0].trim().equals("."))
                        to = -1; // if (to <= 0) toSite = nchar;
                    else
                        to = Integer.parseInt(toParts[0].trim());
                    // codon position
                    if (toParts.length > 1)
                        every = Integer.parseInt(toParts[1].trim());
                    else
                        every = 1;

                } else if (parts.length == 1) {
                    // only 1 site
                    from = Integer.parseInt(parts[0].trim());
                    to = from;
                } else
                    throw new NumberFormatException();
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("block " + charSet1Block + " cannot be parsed");
            }

            return new CharSetBlock(from, to, every);
        }

    }
}
