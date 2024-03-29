package org.molgenis.vcf.inheritance.genemapper;

import static java.lang.String.format;

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

class AppCommandLineOptions {

  static final String OPT_INPUT_OMIM = "i";
  static final String OPT_INPUT_OMIM_LONG = "omim input";
  static final String OPT_INPUT_CGD = "c";
  static final String OPT_INPUT_CGD_LONG = "cgd input";
  static final String OPT_INPUT_IP = "ip";
  static final String OPT_INPUT_IP_LONG = "incomplete_penetrance";
  static final String OPT_HPO_INPUT = "h";
  static final String OPT_HPO_INPUT_LONG = "hpo";
  static final String OPT_OUTPUT = "o";
  static final String OPT_OUTPUT_LONG = "output";
  static final String OPT_FORCE = "f";
  static final String OPT_FORCE_LONG = "force";
  static final String OPT_DEBUG = "d";
  static final String OPT_DEBUG_LONG = "debug";
  static final String OPT_VERSION = "v";
  static final String OPT_VERSION_LONG = "version";
  private static final Options APP_OPTIONS;
  private static final Options APP_VERSION_OPTIONS;

  static {
    Options appOptions = new Options();
    appOptions.addOption(
        Option.builder(OPT_INPUT_OMIM)
            .hasArg(true)
            .longOpt(OPT_INPUT_OMIM_LONG)
            .desc("Input OMIM genemap2 file.")
            .build());
    appOptions.addOption(
        Option.builder(OPT_HPO_INPUT)
            .hasArg(true)
            .required()
            .longOpt(OPT_HPO_INPUT_LONG)
            .desc("Input HPO .hpoa file.")
            .build());
    appOptions.addOption(
        Option.builder(OPT_INPUT_CGD)
            .hasArg(true)
            .longOpt(OPT_INPUT_CGD_LONG)
            .desc("Input cgd txt.gz file.")
            .build());
    appOptions.addOption(
        Option.builder(OPT_INPUT_IP)
            .required()
            .hasArg(true)
            .longOpt(OPT_INPUT_IP_LONG)
            .desc(
                "file with incomplete penetrance genes, containing at lease a 'Gene' and a 'Source' column (.tsv).")
            .build());
    appOptions.addOption(
        Option.builder(OPT_OUTPUT)
            .hasArg(true)
            .longOpt(OPT_OUTPUT_LONG)
            .desc("Output file (.tsv).")
            .build());
    appOptions.addOption(
        Option.builder(OPT_FORCE)
            .longOpt(OPT_FORCE_LONG)
            .desc("Override the output file if it already exists.")
            .build());
    appOptions.addOption(
        Option.builder(OPT_DEBUG)
            .longOpt(OPT_DEBUG_LONG)
            .desc("Enable debug mode (additional logging).")
            .build());
    APP_OPTIONS = appOptions;
    Options appVersionOptions = new Options();
    appVersionOptions.addOption(
        Option.builder(OPT_VERSION)
            .required()
            .longOpt(OPT_VERSION_LONG)
            .desc("Print version.")
            .build());
    APP_VERSION_OPTIONS = appVersionOptions;
  }

  private AppCommandLineOptions() {}

  static Options getAppOptions() {
    return APP_OPTIONS;
  }

  static Options getAppVersionOptions() {
    return APP_VERSION_OPTIONS;
  }

  static void validateCommandLine(CommandLine commandLine) {
    validateInput(commandLine);
    validateHpo(commandLine);
    validateOutput(commandLine);
  }

  private static void validateInput(CommandLine commandLine) {
    if (commandLine.hasOption(OPT_INPUT_CGD)) {
      validateFile(commandLine, OPT_INPUT_CGD, ".txt.gz");
    }
    if (commandLine.hasOption(OPT_INPUT_OMIM)) {
      validateFile(commandLine, OPT_INPUT_OMIM, ".txt");
    }
    if (!commandLine.hasOption(OPT_INPUT_OMIM) && !commandLine.hasOption(OPT_INPUT_CGD)) {
      throw new MissingInputException();
    }
  }

  private static void validateFile(CommandLine commandLine, String option, String extension) {
    Path inputPath = Path.of(commandLine.getOptionValue(option));
    if (!Files.exists(inputPath)) {
      throw new IllegalArgumentException(
          format("Input file '%s' does not exist.", inputPath.toString()));
    }
    if (Files.isDirectory(inputPath)) {
      throw new IllegalArgumentException(
          format("Input file '%s' is a directory.", inputPath.toString()));
    }
    if (!Files.isReadable(inputPath)) {
      throw new IllegalArgumentException(
          format("Input file '%s' is not readable.", inputPath.toString()));
    }
    String inputPathStr = inputPath.toString();
    if (!inputPathStr.endsWith(extension)) {
      throw new IllegalArgumentException(
          format("Input file '%s' is not a %s file.", inputPathStr, extension));
    }
  }

  private static void validateHpo(CommandLine commandLine) {
    Path inputPath = Path.of(commandLine.getOptionValue(OPT_HPO_INPUT));
    if (!Files.exists(inputPath)) {
      throw new IllegalArgumentException(
          format("Input HPO file '%s' does not exist.", inputPath.toString()));
    }
    if (Files.isDirectory(inputPath)) {
      throw new IllegalArgumentException(
          format("Input HPO file '%s' is a directory.", inputPath.toString()));
    }
    if (!Files.isReadable(inputPath)) {
      throw new IllegalArgumentException(
          format("Input HPO file '%s' is not readable.", inputPath.toString()));
    }
    String inputPathStr = inputPath.toString();
    if (!inputPathStr.endsWith(".hpoa")) {
      throw new IllegalArgumentException(
          format("Input HPO file '%s' is not a .hpoa file.", inputPathStr));
    }
  }

  private static void validateOutput(CommandLine commandLine) {
    if (!commandLine.hasOption(OPT_OUTPUT)) {
      return;
    }

    Path outputPath = Path.of(commandLine.getOptionValue(OPT_OUTPUT));

    if (!commandLine.hasOption(OPT_FORCE) && Files.exists(outputPath)) {
      throw new IllegalArgumentException(
          format("Output file '%s' already exists", outputPath.toString()));
    }
  }
}
