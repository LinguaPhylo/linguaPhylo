package lphy.base.parser.newick;

public class TreeParsingException extends RuntimeException {
    String message;
    Integer characterNum, lineNum;

    /**
     * Create new parsing exception.
     *
     * @param message      Human-readable error message.
     * @param characterNum Character offset of error.
     * @param lineNum      Line offset of error.
     */
    public TreeParsingException(String message, Integer characterNum, Integer lineNum) {
        this.message = message;
        this.characterNum = characterNum;
        this.lineNum = lineNum;
    }

    /**
     * Create new parsing exception
     *
     * @param message Human-readable error message.
     */
    TreeParsingException(String message) {
        this(message, null, null);
    }

    @Override
    public String getMessage() {
        return message;
    }

    /**
     * @return location of error on line.  (May be null for non-lexer errors.)
     */
    public Integer getCharacterNum() {
        return characterNum;
    }

    /**
     * @return line number offset of error. (May be null for non-lexer errors.)
     */
    public Integer getLineNum() {
        return lineNum;
    }
}
