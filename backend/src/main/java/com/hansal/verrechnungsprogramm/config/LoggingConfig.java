package com.hansal.verrechnungsprogramm.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.hansal.verrechnungsprogramm.service.LogStorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LoggingConfig {

    private final LogStorageService logStorageService;

    @PostConstruct
    public void init() {
        // Set the LogStorageService in the appender
        InMemoryLogAppender.setLogStorageService(logStorageService);

        // Get the Logback logger context
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Create and configure the appender
        InMemoryLogAppender appender = new InMemoryLogAppender();
        appender.setContext(loggerContext);
        appender.setName("IN_MEMORY");
        appender.start();

        // Add the appender to the root logger
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
    }
}
