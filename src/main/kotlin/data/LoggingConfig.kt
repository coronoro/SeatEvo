package data

import java.util.logging.*


class LoggingConfig {

    init {
        try {
            // Programmatic configuration
            System.setProperty(
                "java.util.logging.SimpleFormatter.format",
                "%1\$tY-%1\$tm-%1\$td %1\$tH:%1\$tM:%1\$tS.%1\$tL %4$-7s [%3\$s] (%2\$s) %5\$s %6\$s%n"
            )
            val logger = Logger.getLogger("org.restlet")
            for (handler in logger.parent.handlers) {
                // Find the console handler
                if (handler.javaClass.equals(ConsoleHandler::class.java)) {
                    // set level to SEVERE. We could disable it completely with
                    // a custom filter but this is good enough.
                    handler.level = Level.SEVERE
                }
            }
            LogManager.getLogManager().reset();
        } catch (e: Exception) {
            // The runtime won't show stack traces if the exception is thrown
            e.printStackTrace()
        }
    }

}