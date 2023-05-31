package lphy.core.parser;

import lphy.core.exception.SimulatorParsingException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Overwrite the default implementation of ANTLRErrorListener.
 * @author Walter Xie
 */
public class LPhyBaseErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        // e is null in the case where the parser was able to recover in line without exiting the surrounding rule.
        if (e != null) {
            e.printStackTrace();
            if (e instanceof NoViableAltException) {
                NoViableAltException nvae = (NoViableAltException) e;
                System.out.println(nvae.getLocalizedMessage());
//              msg = "X no viable alt; token="+nvae.token+
//                 " (decision="+nvae.decisionNumber+
//                 " state "+nvae.stateNumber+")"+
//                 " decision=<<"+nvae.grammarDecisionDescription+">>";
            } else {
            }
        }
        throw new SimulatorParsingException(msg, charPositionInLine, line);
    }

//   @Override
//   public void syntaxError(Recognizer<?, ?> recognizer,
//                           Object offendingSymbol,
//                           int line, int charPositionInLine,
//                           String msg, RecognitionException e) {
//       throw new SimulatorParsingException(msg, charPositionInLine, line);
//   }
}
