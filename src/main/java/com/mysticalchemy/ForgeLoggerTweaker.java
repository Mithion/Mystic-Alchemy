package com.mysticalchemy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.LogRecord;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.AbstractFilter;

public class ForgeLoggerTweaker {

  public static void applyLoggerFilter () {
    List<LoggerConfig> foundLog4JLoggers = new ArrayList<>();

    final LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
    final Map<String, LoggerConfig> map = logContext.getConfiguration().getLoggers();

    for (final LoggerConfig logger : map.values()) {
      if (!foundLog4JLoggers.contains(logger)) {
        logger.addFilter(mbeLogFilter);
        foundLog4JLoggers.add(logger);
      }
    }
  }

  public static void setMinimumLevel(Level minimumLevel) {
    mbeLogFilter.minimumLevel = minimumLevel;
  }

  private static MBELogFilter mbeLogFilter = new MBELogFilter();

  public static class MBELogFilter extends AbstractFilter implements java.util.logging.Filter {

    // Oracle/Java Filter
    @Override
    public boolean isLoggable (LogRecord record) {
      return true;
    }

    // Apache/Log4J Filter - used by Forge
    @Override
    public Result filter(LogEvent event) {
      if (minimumLevel.compareTo(event.getLevel()) < 0) return Result.DENY;
      return Result.NEUTRAL;
    }

    private Level minimumLevel = Level.DEBUG;

  }

}
