package lphy.core.exception;

import org.antlr.v4.runtime.ParserRuleContext;

public class SimulatorParsingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	String message;
    Integer characterNum, lineNum;
    ParserRuleContext context;

    /**
     * Create new parsing exception.
     *
     * @param message      Human-readable error message.
     * @param characterNum Character offset of error.
     * @param lineNum      Line offset of error.
     */
    public SimulatorParsingException(String message, Integer characterNum, Integer lineNum) {
        this.message = message;
        this.characterNum = characterNum;
        this.lineNum = lineNum;
    }

    /**
     * Create new parsing exception.
     *
     * @param message      Human-readable error message.
     * @param context  the parser rule context.
     */
    public SimulatorParsingException(String message, ParserRuleContext context) {
        this.message = message;
        this.context = context;
        this.lineNum = context.getStart().getLine();
        this.characterNum = context.getStart().getCharPositionInLine();
    }


    /**
     * Create new parsing exception
     *
     * @param message Human-readable error message.
     */
    SimulatorParsingException(String message) {
        this(message, null, null);
    }

    @Override
    public String getMessage() {
        StringBuilder msg = new StringBuilder(message);
        msg.append(" line ");
        msg.append(lineNum);
        msg.append(" character ");
        msg.append(characterNum);

        if (context != null) {
            msg.append("\n -> ");
            String text = context.getText();
            msg.append(text);
            msg.append("\n    ");
            msg.append("^".repeat(text.length()));
        }
        return msg.toString();
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
