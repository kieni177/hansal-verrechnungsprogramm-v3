package com.hansal.verrechnungsprogramm.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.hansal.verrechnungsprogramm.service.LogStorageService;
import org.springframework.context.ApplicationContext;

public class InMemoryLogAppender extends AppenderBase<ILoggingEvent> {

    private static LogStorageService logStorageService;

    public static void setLogStorageService(LogStorageService service) {
        logStorageService = service;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (logStorageService != null) {
            // Only capture application logs, not framework logs
            String loggerName = event.getLoggerName();
            if (loggerName.startsWith("com.hansal.verrechnungsprogramm")) {
                logStorageService.addLogEntry(
                        event.getLevel().toString(),
                        simplifyLoggerName(loggerName),
                        event.getFormattedMessage(),
                        event.getThreadName()
                );
            }
        }
    }

    private String simplifyLoggerName(String loggerName) {
        // Convert com.hansal.verrechnungsprogramm.service.OrderService to OrderService
        int lastDot = loggerName.lastIndexOf('.');
        if (lastDot >= 0) {
            return loggerName.substring(lastDot + 1);
        }
        return loggerName;
    }
}
