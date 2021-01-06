package org.molgenis.vcf.inheritance.genemapper;

import static com.opencsv.ICSVWriter.DEFAULT_ESCAPE_CHARACTER;
import static com.opencsv.ICSVWriter.DEFAULT_LINE_END;
import static com.opencsv.ICSVWriter.DEFAULT_QUOTE_CHARACTER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.vcf.inheritance.genemapper.CgdMapper.mapCgdInheritanceMode;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.molgenis.vcf.inheritance.genemapper.model.CGDLine;
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

  public static void run(Path inputOmim, Path inputCgd, Path output) {
    List<OmimLine> omimLines = Collections.emptyList();
    List<CGDLine> cgdLines = Collections.emptyList();
    if (inputOmim != null) {
      omimLines = readOmimFile(inputOmim);
    }
    if (inputCgd != null) {
      cgdLines = readCgdFile(inputCgd);
    }
    Collection<GeneInheritanceValue> geneInheritanceValues =
        convertToGeneInheritanceValue(omimLines, cgdLines);
    writeToFile(output, geneInheritanceValues);
  }

  private static List<CGDLine> readCgdFile(Path inputCgd) {
    List<CGDLine> cgdLines;
    try (InputStream fileStream = new FileInputStream(inputCgd.toFile());
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader reader = new BufferedReader(new InputStreamReader(gzipStream))) {
      CsvToBean<CGDLine> csvToBean =
          new CsvToBeanBuilder<CGDLine>(reader)
              .withSeparator('\t')
              .withType(CGDLine.class)
              .withThrowExceptions(false)
              .build();
      cgdLines = csvToBean.parse();
      handleCsvParseExceptions(csvToBean.getCapturedExceptions());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return cgdLines;
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
      handleCsvParseExceptions(csvToBean.getCapturedExceptions());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return omimLines;
  }

  static void handleCsvParseExceptions(List<CsvException> exceptions) {
    exceptions.forEach(
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
                  String.format("%s,%s", csvException.getLineNumber(), csvException.getMessage()));
            }
          }
        });
  }

  private static boolean isMissingGeneException(CsvException csvException) {
    boolean result = false;
    if (csvException instanceof CsvRequiredFieldEmptyException) {
      CsvRequiredFieldEmptyException csvRequiredFieldEmptyException =
          ((CsvRequiredFieldEmptyException) csvException);
      Field field = csvRequiredFieldEmptyException.getDestinationField();
      result = (field != null && field.getName().equals("gene"));
    }
    return result;
  }

  static Collection<GeneInheritanceValue> convertToGeneInheritanceValue(
      List<OmimLine> omimLines, List<CGDLine> cgdLines) {
    Map<String, GeneInheritanceValue> geneInheritanceValues = new HashMap<>();
    for (OmimLine omimLine : omimLines) {
      Set<Phenotype> phenotypes = omimLine.getPhenotypes();
      if (!phenotypes.isEmpty()) {
        EnumSet<InheritanceMode> inheritanceModes = getInheritanceModesList(phenotypes);
        geneInheritanceValues.put(
            omimLine.getGene(),
            GeneInheritanceValue.builder()
                .phenotypes(omimLine.getPhenotypes())
                .geneSymbol(omimLine.getGene())
                .inheritanceModes(inheritanceModes)
                .build());
      }
    }
    for (CGDLine cgdLine : cgdLines) {
      Set<InheritanceMode> inheritanceModes = mapCgdInheritance(cgdLine.getInheritance());
      GeneInheritanceValue inheritance =
          GeneInheritanceValue.builder()
              .phenotypes(Collections.emptySet())
              .geneSymbol(cgdLine.getGene())
              .inheritanceModes(inheritanceModes)
              .build();
      geneInheritanceValues.putIfAbsent(cgdLine.getGene(), inheritance);
    }
    return geneInheritanceValues.values();
  }

  private static Set<InheritanceMode> mapCgdInheritance(String inheritance) {
    EnumSet<InheritanceMode> inheritanceModusList = EnumSet.noneOf(InheritanceMode.class);
    String[] modes = inheritance.split("/");
    for (String mode : modes) {
      InheritanceMode inheritanceMode = mapCgdInheritanceMode(mode);
      if (inheritanceMode != null) {
        inheritanceModusList.add(inheritanceMode);
      }
    }
    return inheritanceModusList;
  }

  private static EnumSet<InheritanceMode> getInheritanceModesList(Set<Phenotype> additionalInfos) {
    EnumSet<InheritanceMode> inheritanceModusList = EnumSet.noneOf(InheritanceMode.class);
    for (Phenotype additionalInfo : additionalInfos) {
      inheritanceModusList.addAll(additionalInfo.getInheritanceModes());
    }
    return inheritanceModusList;
  }

  private static void writeToFile(
      Path output, Collection<GeneInheritanceValue> geneInheritanceValues) {
    try (CSVWriter writer =
        new CSVWriter(
            new FileWriter(output.toString()),
            '\t',
            DEFAULT_QUOTE_CHARACTER,
            DEFAULT_ESCAPE_CHARACTER,
            DEFAULT_LINE_END)) {
      geneInheritanceValues.stream()
          .filter(geneInheritanceValue -> !geneInheritanceValue.getInheritanceModes().isEmpty())
          .forEach(
              geneInheritanceValue ->
                  writer.writeNext(geneInheritanceValueToString(geneInheritanceValue), false));
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
