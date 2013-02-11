package com.google.caliper.config;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.logging.Level.INFO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.caliper.model.Run;
import com.google.common.io.Files;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Tests {@link LoggingConfigLoader}.
 */

@RunWith(MockitoJUnitRunner.class)
public class LoggingConfigLoaderTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Mock LogManager logManager;
  @Mock Logger logger;
  @Captor ArgumentCaptor<Handler> handlerCaptor;

  private LoggingConfigLoader loader;
  private UUID runId = UUID.randomUUID();
  private Instant startTime = new Instant();
  private File caliperDirectory;

  @Before public void setUp() throws IOException {
    this.caliperDirectory = folder.newFolder();
    this.loader = new LoggingConfigLoader(caliperDirectory, logManager, new Run.Builder(runId)
        .label("fake run")
        .startTime(startTime)
        .build());
  }

  @Test public void testLoadDefaultLogConfiguration()
      throws SecurityException, IOException {
    when(logManager.getLogger("")).thenReturn(logger);
    loader.maybeLoadDefaultLogConfiguration(logManager);
    verify(logManager).reset();
    verify(logger).addHandler(handlerCaptor.capture());
    FileHandler fileHandler = (FileHandler) handlerCaptor.getValue();
    assertEquals(UTF_8.name(), fileHandler.getEncoding());
    assertTrue(fileHandler.getFormatter() instanceof SimpleFormatter);
    fileHandler.publish(new LogRecord(INFO, "some message"));
    File logFile = new File(new File(caliperDirectory, "log"),
        startTime.toString() + "." + runId + ".log");
    assertTrue(logFile.isFile());
    assertTrue(Files.toString(logFile, UTF_8).contains("some message"));
  }
}
