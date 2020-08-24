package lphy.nexus.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modified from BEAST 2
 */
public class NexusBlockParser {
    /**
     * keep track of nexus file line number, to report when the file does not parse *
     */
    protected int lineNr;

    public NexusBlockParser() { }

    public int getLineNr() {
        return lineNr;
    }

    public void setLineNr(int lineNr) {
        this.lineNr = lineNr;
    }

    /**
     * read next line from nexus file that is not a comment and not empty *
     */
    protected String nextLine(final BufferedReader reader) throws IOException {
        String str = reader.readLine();
        lineNr++;
        if (str == null) return null;

        if (str.contains("[")) {
            final int start = str.indexOf('[');
            int end = str.indexOf(']', start);
            while (end < 0) {
                str += reader.readLine();
                end = str.indexOf(']', start);
            }
            str = str.substring(0, start) + str.substring(end + 1);
            if (str.matches("^\\s*$")) {
                return nextLine(reader);
            }
        }
        if (str.matches("^\\s*$")) {
            return nextLine(reader);
        }
        return str;
    }


    /**
     * Get next nexus command, if available.
     *
     * @param fin nexus file reader
     * @return nexus command, or null if none available.
     * @throws IOException if error reading from file
     */
    NexusCommand readNextCommand(BufferedReader fin) throws IOException {
        StringBuilder commandBuilder = new StringBuilder();

        while(true) {
            int nextVal = fin.read();
            if (nextVal<0)
                break;

            char nextChar = (char)nextVal;
            if (nextChar == ';')
                break;

            commandBuilder.append(nextChar);

            switch (nextChar) {
                case '[':
                    readNexusComment(fin, commandBuilder);
                    break;

                case '"':
                case '\'':
                    readNexusString(fin, commandBuilder, nextChar);
                    break;

                case '\n':
                    lineNr += 1;
                    break;

                default:
                    break;
            }
        }

        if (commandBuilder.toString().isEmpty())
            return null;
        else
            return new NexusCommand(commandBuilder.toString());
    }

    /**
     * Remove nexus comments from a given string.
     *
     * @param string input string
     * @return string with nexus comments removed.
     */
    String stripNexusComments(String string) {
        return string.replaceAll("\\[[^]]*]","");
    }

    /**
     * Used to advance reader past nexus strings.
     *
     * @param fin intput file reader
     * @param builder string builder where characters read are to be appended
     * @param stringDelim string delimiter
     *
     * @throws IOException on unterminated string
     */
    private void readNexusString(BufferedReader fin, StringBuilder builder, char stringDelim) throws IOException {
        boolean stringTerminated = false;
        while(true) {
            int nextVal = fin.read();
            if (nextVal<0)
                break;

            char nextChar = (char)nextVal;

            builder.append(nextChar);

            if (nextChar == stringDelim) {
                stringTerminated = true;
                break;
            }

            if (nextChar == '\n')
                lineNr += 1;
        }

        if (!stringTerminated)
            throw new IOException("Unterminated string.");
    }

    /**
     * Used to advance reader past nexus comments. Comments may themselves
     * contain strings.
     *
     * @param fin intput file reader
     * @param builder string builder where characters read are to be appended
     *
     * @throws IOException on unterminated comment.
     */
    private void readNexusComment(BufferedReader fin, StringBuilder builder) throws IOException {
        boolean commentTerminated = false;
        while(true) {
            int nextVal = fin.read();
            if (nextVal<0)
                break;


            char nextChar = (char)nextVal;
            builder.append(nextChar);

            if (nextChar == ']') {
                commentTerminated = true;
                break;
            }

            if (nextChar == '"' || nextChar == '\'')
                readNexusString(fin, builder, nextChar);

            if (nextChar == '\n')
                lineNr += 1;
        }

        if (!commentTerminated)
            throw new IOException("Unterminated comment.");
    }

    /**
     * return attribute value as a string *
     */
    protected String getAttValue(final String attribute, final String str) {
        final Pattern pattern = Pattern.compile(".*" + attribute + "\\s*=\\s*([^\\s;]+).*");
        final Matcher matcher = pattern.matcher(str.toLowerCase());
        if (!matcher.find()) {
            return null;
        }
        String att = matcher.group(1);
        if (att.startsWith("\"") && att.endsWith("\"")) {
            final int start = matcher.start(1);
            att = str.substring(start + 1, str.indexOf('"', start + 1));
        }
        return att;
    }


    //*** TODO below ***//


//    private ArrayList<String> readInCharstatelablesTokens(final BufferedReader fin) throws IOException {
//
//        ArrayList<String> tokens = new ArrayList<>();
//        String token="";
//        final int READING=0, OPENQUOTE=1, WAITING=2;
//        int mode = WAITING;
//        int numberOfQuotes=0;
//        boolean endOfBlock=false;
//        String str;
//
//        while (!endOfBlock) {
//            str = nextLine(fin);
//            Character nextChar;
//            for (int i=0; i< str.length(); i++) {
//                nextChar=str.charAt(i);
//                switch (mode) {
//                    case WAITING:
//                        if (!Character.isWhitespace(nextChar)) {
//                            if (nextChar == '\'') {
//                                mode=OPENQUOTE;
//                            } else if (nextChar == '/' || nextChar == ',') {
//                                tokens.add(nextChar.toString());
//                                token="";
//                            } else if (nextChar == ';') {
//                                endOfBlock = true;
//                            } else {
//                                token=token+nextChar;
//                                mode=READING;
//                            }
//                        }
//                        break;
//                    case READING:
//                        if (nextChar == '\'') {
//                            tokens.add(token);
//                            token="";
//                            mode=OPENQUOTE;
//                        } else if (nextChar == '/' || nextChar == ',') {
//                            tokens.add(token);
//                            tokens.add(nextChar.toString());
//                            token="";
//                            mode=WAITING;
//                        } else if (nextChar == ';') {
//                            tokens.add(token);
//                            endOfBlock = true;
//                        } else if (Character.isWhitespace(nextChar)) {
//                            tokens.add(token);
//                            token="";
//                            mode=WAITING;
//                        } else {
//                            token=token+nextChar;
//                        }
//                        break;
//                    case OPENQUOTE:
//                        if (nextChar == '\'') {
//                            numberOfQuotes++;
//                        } else {
//                            if (numberOfQuotes % 2 == 0) {
//                                for (int ind=0; ind< numberOfQuotes/2; ind++) {
//                                    token=token+"'";
//                                }
//                                token=token+nextChar;
//                            } else {
//                                for (int ind=0; ind< numberOfQuotes/2; ind++) {
//                                    token=token+"'";
//                                }
//                                tokens.add(token);
//                                token="";
//                                if (nextChar == '/' || nextChar == ',') {
//                                    tokens.add(nextChar.toString());
//                                    mode=WAITING;
//                                } else if (nextChar == ';') {
//                                    endOfBlock = true;
//                                } else if (Character.isWhitespace(nextChar)) {
//                                    mode=WAITING;
//                                } else {
//                                    token=token+nextChar;
//                                    mode=READING;
//                                }
//                            }
//                            numberOfQuotes=0;
//                        }
//                        break;
//                    default:
//                        break;
//                }
//            }
//        }
//
//        if (!tokens.get(tokens.size()-1).equals(",")) {
//            tokens.add(",");
//        }
//
//        return tokens;
//    }

//    private ArrayList<UserDataType> processCharstatelabelsTokens(ArrayList<String> tokens, int[] maxNumberOfStates) throws IOException {
//
//        ArrayList<UserDataType> charDescriptions = new ArrayList<>();
//
//        final int CHAR_NR=0, CHAR_NAME=1, STATES=2;
//        int mode = CHAR_NR;
//        int charNumber = -1;
//        String charName = "";
//        ArrayList<String> states = new ArrayList<>();
//
//        for (String token:tokens) {
//            switch (mode) {
//                case CHAR_NR:
//                    charNumber = Integer.parseInt(token);
//                    mode = CHAR_NAME;
//                    break;
//                case CHAR_NAME:
//                    if (token.equals("/")) {
//                        mode = STATES;
//                    } else if (token.equals(",")) {
//                        if (charNumber > charDescriptions.size()+1) {
//                            throw new IOException("Character descriptions should go in the ascending order and there " +
//                                    "should not be any description missing.");
//                        }
//                        charDescriptions.add(new UserDataType(charName, states));
//                        maxNumberOfStates[0] = Math.max(maxNumberOfStates[0], states.size());
//                        charNumber = -1;
//                        charName = "";
//                        states = new ArrayList<>();
//                        mode = CHAR_NR;
//                    } else {
//                        charName = token;
//                    }
//                    break;
//                case STATES:
//                    if (token.equals(",")) {
//                        if (charNumber > charDescriptions.size()+1) {
//                            throw new IOException("Character descriptions should go in the ascending order and there " +
//                                    "should not be any description missing.");
//                        }
//                        charDescriptions.add(new UserDataType(charName, states));
//                        maxNumberOfStates[0] = Math.max(maxNumberOfStates[0], states.size());
//                        charNumber = -1;
//                        charName = "";
//                        states = new ArrayList<>();
//                        mode = CHAR_NR;
//                    } else {
//                        states.add(token);
//                    }
//                default:
//                    break;
//            }
//        }
//
//        return charDescriptions;
//
//    }



}
