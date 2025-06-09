import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

enum LogLevel {
    DEBUG, INFO, WARNING, ERROR, FATAL
}

class LogMessage {
    private final LogLevel level;
    private final String message;
    private final Date timestamp;

    public LogMessage(LogLevel level, String message) {
        this.level = level;
        this.message = message;
        this.timestamp = new Date();
    }

    public String format() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] [%s] %s", dateFormat.format(timestamp), level, message);
    }
}

class LogConfig {
    private LogLevel level;
    private LogAppender appender;

    public LogConfig (LogLevel level, LogAppender appender) {
        this.level = level;
        this.appender = appender;
    }

    public LogLevel getLevel() {
        return level;
    }
    public void setLevel(LogLevel level) {
        this.level = level;
    }
    public LogAppender getAppender() {
        return appender;
    }
    public void setAppender(LogAppender appender) {
        this.appender = appender;
    }
}

interface LogAppender {
    void append(LogMessage message);
}

class ConsoleAppender implements LogAppender {
    @Override
    public void append(LogMessage message) {
        System.out.println(message.format());
    }
}

class FileAppender implements LogAppender {
    private final String filePath;

    public FileAppender(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void append(LogMessage message) {
        try{
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
                bw.write(message.format());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to write log to file: " + e.getMessage());
        }
    }
}

class DatabaseAppender implements LogAppender {
    private final String connectionString;

    public DatabaseAppender(String connectionString) {
        this.connectionString = connectionString;
    }

    @Override
    public void append(LogMessage message) {
        // Simulate database logging
        System.out.println("Logging to database: " + message.format());
        // Actual database logic would go here
    }
}

class Logger {
    private static Logger instance;
    private static final Object lock = new Object();
    private LogConfig config;

    private Logger(LogConfig config) {
        this.config = config;
    }
    public static Logger getInstance(LogConfig config) {
        synchronized (lock) {
            if (instance == null) {
                instance = new Logger(config);
            }
        }
        return instance;
    }

    public void setConfig(LogConfig config) {
        synchronized (lock) {
            this.config = config;
        }
    }

    public LogConfig getConfig() {
        synchronized (lock) {
            return config;
        }
    }

    public void log(LogLevel level, String message) {
        synchronized (lock){
            if(level.ordinal() >= config.getLevel().ordinal()){
                LogMessage logMessage = new LogMessage(level, message);
                config.getAppender().append(logMessage);
            }
        }
    }

    // Convenience methods
    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }
    public void info(String message) {
        log(LogLevel.INFO, message);
    }
    public void warning(String message) {
        log(LogLevel.WARNING, message);
    }
    public void error(String message) {
        log(LogLevel.ERROR, message);
    }
    public void fatal(String message) {
        log(LogLevel.FATAL, message);
    }

}


public class LoggingDemo {
    public static void main(String[] args) throws InterruptedException {
        Logger logger = Logger.getInstance(new LogConfig(LogLevel.INFO , new ConsoleAppender()));

        // Default logger (INFO + console)
        logger.debug("This will not be logged (below INFO)");
        logger.info("This is an info message.");

        // Change to DEBUG level + file output
        logger.setConfig(new LogConfig(LogLevel.DEBUG, new FileAppender("app.log")));

        logger.debug("Debug message - should go to file.");
        logger.error("Error message - should go to file.");

        // Change to DB appender
        logger.setConfig(new LogConfig(LogLevel.WARNING, new DatabaseAppender("")));

        logger.info("This will NOT be logged (below WARNING).");
        logger.warning("Warning - stored in DB mock.");


        Runnable task = () -> {
            for (int i = 0; i < 3; i++) {
                logger.warning(Thread.currentThread().getName() + ": Logging from thread " + i);
            }
        };

        Thread thread1 = new Thread(task, "T1");
        Thread thread2 = new Thread(task, "T2");
        thread1.start();
        thread1.join();
        thread2.start();
        thread2.join();
    }
}