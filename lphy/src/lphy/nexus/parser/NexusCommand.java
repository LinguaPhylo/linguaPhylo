package lphy.nexus.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing a single command in a nexus file.
 * Command may be terminated by either a ";" or an EOF. All whitespace
 * is converted to single spaces.
 * Currently only used by parseTreesBlock and parseTaxaBlock.
 *
 * Modified from BEAST 2
 */
public class NexusCommand {
    String command;
    String arguments;

    Map<String,String> kvArgs;
    List<String> argList;

    boolean isCommand(String commandName) {
        return command.equals(commandName.toLowerCase());
    }

    boolean isEndOfBlock() {
        return command.equals("end");
    }

    NexusCommand(String commandString) {
        commandString = commandString.trim().replaceAll("\\s+", " ");

        command = commandString.split(" ")[0].toLowerCase();

        try {
            arguments = commandString.substring(command.length() + 1);
        } catch (IndexOutOfBoundsException ex) {
            arguments = "";
        }
    }



    /**
     * Used by argument processing methods to identify the end of a
     * nexus comment, begining at index idx.  Allows for nested comments.
     *
     * @param idx index at start of comment
     * @return first index past end of comment.
     * @throws IOException if the comment or nested string is not terminated.
     */
    private int findCommentEnd(int idx) throws IOException {

        idx += 1;

        while (idx < arguments.length()) {

            switch (arguments.charAt(idx)) {
                case ']':
                    return idx+1;

                case '"':
                case '\'':
                    idx = findStringEnd(idx, arguments.charAt(idx));
                    break;

                default:
                    idx += 1;
                    break;
            }
        }

        throw new IOException("Unterminated comment.");
    }

    /**
     * Used by argument processing methods to identify the end of a
     * string, begining at index idx with delmiter delim.
     *
     * @param idx index at start of string
     * @param delim terminating index
     * @return first index past end of string.
     * @throws IOException if the string is not terminated.
     */
    private int findStringEnd(int idx, char delim) throws IOException {

        idx += 1;

        while (idx < arguments.length()) {

            if (arguments.charAt(idx) == delim)
                return idx+1;

            idx += 1;
        }

        throw new IOException("Unterminated string.");

    }

    /**
     * Used by argument processing methods to identify the end of a
     * chunk of whitespace.
     *
     * @param idx start of (potential) whitespace block.
     * @return first non-whitespace character found.
     */
    private int findWhitespaceEnd(int idx) {
        while (idx < arguments.length() && Character.isWhitespace(arguments.charAt(idx)))
            idx += 1;

        return idx;
    }

    /**
     * Used by argument processing methods to identify the end of a token
     * in a manner that allows for comments and strings.
     *
     * @param idx start index
     * @return index of first character past the end of the identified token
     * @throws IOException if unterminated comments or strings are found.
     */
    private int findTokenEnd(int idx) throws IOException {

        boolean done = false;
        while (!done && idx < arguments.length()) {

            switch(arguments.charAt(idx)) {
                case '[':
                    idx = findCommentEnd(idx);
                    break;

                case '"':
                case '\'':
                    idx = findStringEnd(idx, arguments.charAt(idx));
                    break;

                default:
                    if (Character.isWhitespace(arguments.charAt(idx))
                            || arguments.charAt(idx) == '=')
                        done = true;
                    else
                        idx += 1;
                    break;
            }
        }

        return idx;
    }

    /**
     * Attempt to interpret arguments as key value pairs.
     * Arguments matching this pattern are added to a map, which
     * is then returned.
     *
     * @return map of key strings to value strings.
     * @throws IOException if unterminated comments/strings are found.
     */
    Map<String,String> getKeyValueArgs() throws IOException {
        if (kvArgs != null)
            return kvArgs;

        kvArgs = new HashMap<>();

        int idx=0;
        while (idx < arguments.length()) {

            idx = findWhitespaceEnd(idx);

            int keyStart = idx;
            idx = findTokenEnd(idx);
            int keyEnd = idx;

            idx = findWhitespaceEnd(idx);

            if (idx>= arguments.length() || arguments.charAt(idx) != '=')
                continue;

            idx += 1;

            idx = findWhitespaceEnd(idx);

            int valStart = idx;
            idx = findTokenEnd(idx);
            int valEnd = idx;

            kvArgs.put(arguments.substring(keyStart, keyEnd).trim(),
                    arguments.substring(valStart, valEnd).trim());
        }

        return kvArgs;
    }

    /**
     * Attempt to interpret arguments string as a whitespace-delimited set of
     * individual arguments. Arguments matching this pattern are added to a
     * list, which is then returned.
     *
     * @return list of argument strings in this command.
     * @throws IOException if unterminated comments/strings are found.
     */
    List<String> getArgList() throws IOException {
        if (argList != null)
            return argList;

        argList = new ArrayList<>();

        int idx=0;
        while (idx < arguments.length()) {
            idx = findWhitespaceEnd(idx);

            int keyStart = idx;
            idx = findTokenEnd(idx);
            int keyEnd = idx;

            idx = findWhitespaceEnd(idx);

            if (idx >= arguments.length() || arguments.charAt(idx) != '=') {
                argList.add(arguments.substring(keyStart, keyEnd).trim());
                continue;
            }

            idx += 1;

            idx = findWhitespaceEnd(idx);

            int valStart = idx;
            idx = findTokenEnd(idx);
            int valEnd = idx;

            argList.add(arguments.substring(keyStart, valEnd).trim());
        }

        return argList;
    }

    @Override
    public String toString() {
        return "Command: " + command + ", Args: " + arguments;
    }
}
