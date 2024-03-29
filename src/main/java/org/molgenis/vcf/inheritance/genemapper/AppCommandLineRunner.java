package org.molgenis.vcf.inheritance.genemapper;

import static java.util.Objects.requireNonNull;

import ch.qos.logback.classic.Level;
import java.nio.file.Path;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class AppCommandLineRunner implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppCommandLineRunner.class);

  private static final int STATUS_MISC_ERROR = 1;
  private static final int STATUS_COMMAND_LINE_USAGE_ERROR = 64;

  private final String appName;
  private final String appVersion;
  private final CommandLineParser commandLineParser;

  AppCommandLineRunner(
      @Value("${app.name}") String appName, @Value("${app.version}") String appVersion) {
    this.appName = requireNonNull(appName);
    this.appVersion = requireNonNull(appVersion);

    this.commandLineParser = new DefaultParser();
  }

  @Override
  public void run(String... args) {
    if (args.length == 1
        && (args[0].equals("-" + AppCommandLineOptions.OPT_VERSION)
            || args[0].equals("--" + AppCommandLineOptions.OPT_VERSION_LONG))) {
      LOGGER.info("{} {}", appName, appVersion);
      return;
    }

    for (String arg : args) {
      if (arg.equals('-' + AppCommandLineOptions.OPT_DEBUG)
          || arg.equals('-' + AppCommandLineOptions.OPT_DEBUG_LONG)) {
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (!(rootLogger instanceof ch.qos.logback.classic.Logger)) {
          throw new ClassCastException("Expected root logger to be a logback logger");
        }
        ((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.DEBUG);
        break;
      }
    }

    CommandLine commandLine = getCommandLine(args);
    AppCommandLineOptions.validateCommandLine(commandLine);

    try {
      Path omimPath = null;
      Path cgdPath = null;
      Path ipPath = null;

      if (commandLine.hasOption(AppCommandLineOptions.OPT_INPUT_OMIM)) {
        omimPath = Path.of(commandLine.getOptionValue(AppCommandLineOptions.OPT_INPUT_OMIM));
      }
      if (commandLine.hasOption(AppCommandLineOptions.OPT_INPUT_CGD)) {
        cgdPath = Path.of(commandLine.getOptionValue(AppCommandLineOptions.OPT_INPUT_CGD));
      }
      if(commandLine.hasOption(AppCommandLineOptions.OPT_INPUT_IP)){
        ipPath = Path.of(commandLine.getOptionValue(AppCommandLineOptions.OPT_INPUT_IP));
      }
      GenemapConverter.run(
          omimPath,
          cgdPath,
          Path.of(commandLine.getOptionValue(AppCommandLineOptions.OPT_HPO_INPUT)),
          ipPath,
          getOutput(commandLine));

    } catch (Exception e) {
      LOGGER.error(e.getLocalizedMessage(), e);
      System.exit(STATUS_MISC_ERROR);
    }
  }

  private CommandLine getCommandLine(String[] args) {
    CommandLine commandLine = null;
    try {
      commandLine = commandLineParser.parse(AppCommandLineOptions.getAppOptions(), args);
    } catch (ParseException e) {
      logException(e);
      System.exit(STATUS_COMMAND_LINE_USAGE_ERROR);
    }
    return commandLine;
  }

  private Path getOutput(CommandLine commandLine) {
    Path outputPath;
    if (commandLine.hasOption(AppCommandLineOptions.OPT_OUTPUT)) {
      outputPath = Path.of(commandLine.getOptionValue(AppCommandLineOptions.OPT_OUTPUT));
    } else {
      String output;
      if (commandLine.hasOption(AppCommandLineOptions.OPT_INPUT_OMIM)) {
        output =
            commandLine
                .getOptionValue(AppCommandLineOptions.OPT_INPUT_OMIM)
                .replace(".txt", "out.tsv");
      } else {
        output =
            commandLine
                .getOptionValue(AppCommandLineOptions.OPT_INPUT_CGD)
                .replace(".txt.gz", "out.tsv");
      }
      outputPath = Path.of(output);
    }
    return outputPath;
  }

  @SuppressWarnings("java:S106")
  private void logException(ParseException e) {
    LOGGER.error(e.getLocalizedMessage(), e);

    // following information is only logged to system out
    System.out.println();
    HelpFormatter formatter = new HelpFormatter();
    formatter.setOptionComparator(null);
    String cmdLineSyntax = "java -jar " + appName + ".jar";
    formatter.printHelp(cmdLineSyntax, AppCommandLineOptions.getAppOptions(), true);
    System.out.println();
    formatter.printHelp(cmdLineSyntax, AppCommandLineOptions.getAppVersionOptions(), true);
  }
}
