package lphy.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtils {

    public static final Logger log = Logger.getLogger("linguaPhylo");

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        log.addHandler(handler);
    }

    /**
     * log Exception stack trace, so that it can be printed in Errors panel.
     * @param e Exception
     */
    public static void logStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        LoggerUtils.log.severe(sw.toString());
    }
}
