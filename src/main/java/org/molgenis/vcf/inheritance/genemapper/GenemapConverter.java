package org.molgenis.vcf.inheritance.genemapper;

import static com.opencsv.ICSVWriter.DEFAULT_QUOTE_CHARACTER;
import static com.opencsv.ICSVWriter.DEFAULT_ESCAPE_CHARACTER;
import static com.opencsv.ICSVWriter.DEFAULT_LINE_END;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.molgenis.vcf.inheritance.genemapper.model.GeneInheritanceValue;
import org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode;
import org.molgenis.vcf.inheritance.genemapper.model.OmimLine;
import org.molgenis.vcf.inheritance.genemapper.model.Phenotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenemapConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenemapConverter.class);

  private GenemapConverter() {}

  public static final String COMMENT_PREFIX = "#";
  public static final String VALUE_SEPARATOR = ",";

  public static void run(Path input, Path output) {
    List<OmimLine> omimLines = readOmimFile(input);
    List<GeneInheritanceValue> geneInheritanceValues = convertToGeneInheritanceValue(omimLines);
    writeToFile(output, geneInheritanceValues);
  }

  private static List<OmimLine> readOmimFile(Path input) {
    List<OmimLine> omimLines;
    try (Reader reader = Files.newBufferedReader(input, UTF_8)) {

      CsvToBean<OmimLine> csvToBean =
          new CsvToBeanBuilder<OmimLine>(reader)
              .withSkipLines(3)
              .withSeparator('\t')
              .withType(OmimLine.class)
              .withThrowExceptions(false)
              .build();
      omimLines = csvToBean.parse();
      List<CsvException> csvExceptions = csvToBean.getCapturedExceptions();

      csvExceptions.forEach(
          csvException -> {
            // ignore errors parsing trailing comment lines
            if (!(csvException.getLine()[0].startsWith("#"))) {
              if (isMissingGeneException(csvException)) {
                LOGGER.debug(
                    String.format(
                        "line:%s,%s,%s",
                        csvException.getLineNumber(),
                        csvException.getMessage(),
                        Arrays.toString(csvException.getLine())));
              } else {
                LOGGER.error(
                    String.format(
                        "%s,%s", csvException.getLineNumber(), csvException.getMessage()));
              }
            }
          });
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return omimLines;
  }

  static boolean isMissingGeneException(CsvException csvException) {
    boolean result = false;
    if (csvException instanceof CsvRequiredFieldEmptyException) {
      CsvRequiredFieldEmptyException csvRequiredFieldEmptyException =
          ((CsvRequiredFieldEmptyException) csvException);
      Field field = csvRequiredFieldEmptyException.getDestinationField();
      result = (field != null && field.getName().equals("gene"));
    }
    return result;
  }

  static List<GeneInheritanceValue> convertToGeneInheritanceValue(List<OmimLine> omimLines) {
    List<GeneInheritanceValue> geneInheritanceValues = new ArrayList<>();
    for (OmimLine omimLine : omimLines) {
      Set<Phenotype> phenotypes = omimLine.getPhenotypes();
      if (!phenotypes.isEmpty()) {
        EnumSet<InheritanceMode> inheritanceModes = getInheritanceModesList(phenotypes);
        geneInheritanceValues.add(
            GeneInheritanceValue.builder()
                .phenotypes(omimLine.getPhenotypes())
                .geneSymbol(omimLine.getGene())
                .inheritanceModes(inheritanceModes)
                .build());
      }
    }
    return geneInheritanceValues;
  }

  private static EnumSet<InheritanceMode> getInheritanceModesList(Set<Phenotype> phenotypes) {
    EnumSet<InheritanceMode> inheritanceModusList = EnumSet.noneOf(InheritanceMode.class);
    for (Phenotype phenotype : phenotypes) {
      inheritanceModusList.addAll(phenotype.getInheritanceModes());
    }
    return inheritanceModusList;
  }

  private static void writeToFile(Path output, List<GeneInheritanceValue> geneInheritanceValues) {
    try (CSVWriter writer =
        new CSVWriter(
            new FileWriter(output.toString()),
            '\t',
            DEFAULT_QUOTE_CHARACTER,
            DEFAULT_ESCAPE_CHARACTER,
            DEFAULT_LINE_END)) {
      for (GeneInheritanceValue geneInheritanceValue : geneInheritanceValues) {
        writer.writeNext(geneInheritanceValueToString(geneInheritanceValue), false);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String[] geneInheritanceValueToString(GeneInheritanceValue geneInheritanceValue) {
    return new String[] {
      geneInheritanceValue.getGeneSymbol(),
      inheritanceModesToString(geneInheritanceValue.getInheritanceModes()),
      phenotypeValuesToString(geneInheritanceValue.getPhenotypes())
    };
  }

  private static String phenotypeValuesToString(Set<Phenotype> phenotypes) {
    StringBuilder result = new StringBuilder();
    phenotypes.stream()
        .sorted(Comparator.comparing(Phenotype::getName))
        .forEach(
            phenotype -> {
              if (result.length() != 0) {
                result.append(VALUE_SEPARATOR);
              }
              result.append(
                  String.format(
                      "%s:%s",
                      phenotype.getName(),
                      inheritanceModesToString(phenotype.getInheritanceModes())));
            });
    return result.toString();
  }

  private static String inheritanceModesToString(Set<InheritanceMode> inheritanceModes) {
    StringBuilder result = new StringBuilder();
    inheritanceModes.stream()
        .sorted()
        .forEach(
            inheritanceMode -> {
              if (result.length() != 0) {
                result.append(VALUE_SEPARATOR);
              }
              result.append(inheritanceMode.toString());
            });
    return result.toString();
  }
}
