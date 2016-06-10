//
// Built on Fri Jun 10 10:46:28 UTC 2016 by logback-translator
// For more information on configuration files in Groovy
// please see http://logback.qos.ch/manual/groovy.html

// For assistance related to this tool or configuration files
// in general, please contact the logback user mailing list at
//    http://qos.ch/mailman/listinfo/logback-user

// For professional support please see
//   http://www.qos.ch/shop/products/professionalSupport

import static ch.qos.logback.classic.Level.ERROR
import static ch.qos.logback.classic.Level.INFO

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.spi.LifeCycle
import net.logstash.logback.composite.JsonProviders
import net.logstash.logback.composite.loggingevent.ArgumentsJsonProvider
import net.logstash.logback.composite.loggingevent.LogLevelJsonProvider
import net.logstash.logback.composite.loggingevent.LoggerNameJsonProvider
import net.logstash.logback.composite.loggingevent.LoggingEventFormattedTimestampJsonProvider
import net.logstash.logback.composite.loggingevent.MdcJsonProvider
import net.logstash.logback.composite.loggingevent.MessageJsonProvider
import net.logstash.logback.composite.loggingevent.StackTraceJsonProvider
import net.logstash.logback.composite.loggingevent.TagsJsonProvider
import net.logstash.logback.composite.loggingevent.ThreadNameJsonProvider
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder

def mdcPattern = '%replace(%X){"[a-z]+:( |$)", ""}'
def logPattern = "%-5p %d [%t] ${mdcPattern} %c{20}: %m%n"

appender("jsonFile", RollingFileAppender) {
  filter(ThresholdFilter) {
    level = INFO
  }
  file = "/tmp/test.log"
  rollingPolicy(TimeBasedRollingPolicy) {
    fileNamePattern = "/tmp/test.log.%d{yyyy-MM-dd}.log"
    maxHistory = 1
  }
  encoder(LoggingEventCompositeJsonEncoder) {
    // You MUST add an import declaration as appropriate for [Providers]
    JsonProviders aProviders = new JsonProviders()
    aProviders.addProvider(new LogLevelJsonProvider())
    aProviders.addProvider(new LoggingEventFormattedTimestampJsonProvider())
    aProviders.addProvider(new ThreadNameJsonProvider())
    aProviders.addProvider(new MdcJsonProvider())
    aProviders.addProvider(new LoggerNameJsonProvider())
    aProviders.addProvider(new MessageJsonProvider())
    aProviders.addProvider(new StackTraceJsonProvider())
    aProviders.addProvider(new ArgumentsJsonProvider())
    aProviders.addProvider(new TagsJsonProvider())
    if (aProviders instanceof LifeCycle)
      aProviders.start()
    providers = aProviders
  }
}
appender("console", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "${logPattern}"
  }
}
root(ERROR)
logger("com.pauldailly", INFO, ["console", "jsonFile"])

