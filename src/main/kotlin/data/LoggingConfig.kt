package data

import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter


class LoggingConfig {

    fun LoggingConfig() {
        try {
            // Load a properties file from class path that way can't be achieved with java.util.logging.config.file
            /*
            final LogManager logManager = LogManager.getLogManager();
            try (final InputStream is = getClass().getResourceAsStream("/logging.properties")) {
                logManager.readConfiguration(is);
            }
            */

            // Programmatic configuration
            System.setProperty(
                "java.util.logging.SimpleFormatter.format",
                "%1\$tY-%1\$tm-%1\$td %1\$tH:%1\$tM:%1\$tS.%1\$tL %4$-7s [%3\$s] (%2\$s) %5\$s %6\$s%n"
            )
            val consoleHandler = ConsoleHandler()
            consoleHandler.level = Level.SEVERE
            consoleHandler.formatter = SimpleFormatter()
            val app: Logger = Logger.getLogger("app")
            app.setLevel(Level.SEVERE)
            app.addHandler(consoleHandler)
        } catch (e: Exception) {
            // The runtime won't show stack traces if the exception is thrown
            e.printStackTrace()
        }
    }

}