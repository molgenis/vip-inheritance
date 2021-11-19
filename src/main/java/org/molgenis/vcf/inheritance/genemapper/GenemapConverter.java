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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.molgenis.vcf.inheritance.genemapper.model.CgdLine;
import org.molgenis.vcf.inheritance.genemapper.model.GeneInheritanceValue;
import org.molgenis.vcf.inheritance.genemapper.model.HpoInheritanceMode;
import org.molgenis.vcf.inheritance.genemapper.model.HpoLine;
import org.molgenis.vcf.inheritance.genemapper.model.IncompletePenetranceLine;
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

  public static void run(
      Path inputOmim, Path inputCgd, Path inputHpo, Path inputIncompletePenetrance, Path output) {
    List<OmimLine> omimLines = Collections.emptyList();
    List<CgdLine> cgdLines = Collections.emptyList();
    List<IncompletePenetranceLine> incompletePenetrance = Collections.emptyList();

    if (inputOmim != null) {
      omimLines = readOmimFile(inputOmim);
    }
    if (inputCgd != null) {
      cgdLines = readCgdFile(inputCgd);
    }
    if (inputIncompletePenetrance != null) {
      incompletePenetrance = readIPFile(inputIncompletePenetrance);
    }
    List<HpoLine> hpoLines = readHpoFile(inputHpo);
    Map<String, Set<String>> omimHpoMapping = convertHpoLines(hpoLines);
    Collection<GeneInheritanceValue> geneInheritanceValues =
        convertToGeneInheritanceValue(omimLines, cgdLines, omimHpoMapping, incompletePenetrance);
    writeToFile(output, geneInheritanceValues);
  }

  private static List<CgdLine> readCgdFile(Path inputCgd) {
    List<CgdLine> cgdLines;
    try (InputStream fileStream = new FileInputStream(inputCgd.toFile());
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader reader = new BufferedReader(new InputStreamReader(gzipStream))) {
      CsvToBean<CgdLine> csvToBean =
          new CsvToBeanBuilder<CgdLine>(reader)
              .withSeparator('\t')
              .withType(CgdLine.class)
              .withThrowExceptions(false)
              .withIgnoreQuotations(true)
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

  private static List<HpoLine> readHpoFile(Path inputHpo) {
    List<HpoLine> hpoLines;
    try (Reader reader = Files.newBufferedReader(inputHpo, UTF_8)) {

      CsvToBean<HpoLine> csvToBean =
          new CsvToBeanBuilder<HpoLine>(reader)
              .withSkipLines(4)
              .withSeparator('\t')
              .withType(HpoLine.class)
              .withThrowExceptions(false)
              .build();
      hpoLines = csvToBean.parse();
      handleCsvParseExceptions(csvToBean.getCapturedExceptions());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return hpoLines;
  }

  private static List<IncompletePenetranceLine> readIPFile(Path inputIp) {
    List<IncompletePenetranceLine> incompletePenetranceLines;
    try (Reader reader = Files.newBufferedReader(inputIp, UTF_8)) {

      CsvToBean<IncompletePenetranceLine> csvToBean =
          new CsvToBeanBuilder<IncompletePenetranceLine>(reader)
              .withSeparator('\t')
              .withType(IncompletePenetranceLine.class)
              .withThrowExceptions(false)
              .build();
      incompletePenetranceLines = csvToBean.parse();
      handleCsvParseExceptions(csvToBean.getCapturedExceptions());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return incompletePenetranceLines;
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
      List<OmimLine> omimLines,
      List<CgdLine> cgdLines,
      Map<String, Set<String>> omimHpoMapping,
      List<IncompletePenetranceLine> incompletePenetrance) {
    Map<String, GeneInheritanceValue> geneInheritanceValues = new LinkedHashMap<>();
    for (OmimLine omimLine : omimLines) {
      Set<HpoInheritanceMode> hpoInheritanceModes =
          convertoToHpoBasedInheritance(omimLine.getPhenotypes(), omimHpoMapping);
      if (!omimLine.getPhenotypes().isEmpty()) {
        EnumSet<InheritanceMode> inheritanceModes =
            getInheritanceModesList(omimLine.getPhenotypes());
        geneInheritanceValues.put(
            omimLine.getGene(),
            GeneInheritanceValue.builder()
                .hpoInheritanceModes(hpoInheritanceModes)
                .geneSymbol(omimLine.getGene())
                .inheritanceModes(inheritanceModes)
                .isIncompletePenetrance(isIncompletePenetrance(omimLine.getGene(), incompletePenetrance))
                .build());
      }
    }
    for (CgdLine cgdLine : cgdLines) {
      Set<InheritanceMode> inheritanceModes = mapCgdInheritanceMode(cgdLine.getInheritance());
      GeneInheritanceValue inheritance =
          GeneInheritanceValue.builder()
              .hpoInheritanceModes(Collections.emptySet())
              .geneSymbol(cgdLine.getGene())
              .inheritanceModes(inheritanceModes)
              .isIncompletePenetrance(isIncompletePenetrance(cgdLine.getGene(), incompletePenetrance))
              .build();
      geneInheritanceValues.putIfAbsent(cgdLine.getGene(), inheritance);
    }
    return geneInheritanceValues.values();
  }

  private static boolean isIncompletePenetrance(String gene,
      List<IncompletePenetranceLine> incompletePenetrance) {
    for(IncompletePenetranceLine incompletePenetranceLine : incompletePenetrance){
      if(incompletePenetranceLine.getGene().equals(gene) && incompletePenetranceLine.getSource().equals("EntrezGene")){
        return true;
      }
    }
    return false;
  }

  private static Map<String, Set<String>> convertHpoLines(List<HpoLine> hpoLines) {
    Map<String, Set<String>> mapping = new HashMap<>();
    hpoLines.stream()
        .filter(line -> line.getDatabaseId().startsWith("OMIM:"))
        .forEach(
            line -> {
              String omimId = line.getDatabaseId().replace("OMIM:", "");
              Set<String> hpoIds;
              if (mapping.containsKey(omimId)) {
                hpoIds = mapping.get(omimId);
              } else {
                hpoIds = new HashSet<>();
              }
              hpoIds.add(line.getHpoId().replace(":", "_"));
              mapping.put(omimId, hpoIds);
            });
    return mapping;
  }

  private static Set<HpoInheritanceMode> convertoToHpoBasedInheritance(
      Set<Phenotype> phenotypes, Map<String, Set<String>> omimHpoMapping) {
    Set<HpoInheritanceMode> result = new HashSet<>();
    phenotypes.stream()
        .filter(phenotype -> omimHpoMapping.containsKey(phenotype.getOmimId()))
        .filter(phenotype -> !phenotype.getInheritanceModes().isEmpty())
        .forEach(
            phenotype ->
                omimHpoMapping
                    .get(phenotype.getOmimId())
                    .forEach(
                        hpo ->
                            result.add(
                                HpoInheritanceMode.builder()
                                    .hpoId(hpo)
                                    .inheritanceModes(phenotype.getInheritanceModes())
                                    .build())));
    return result;
  }

  private static EnumSet<InheritanceMode> getInheritanceModesList(Set<Phenotype> phenotypes) {
    EnumSet<InheritanceMode> inheritanceModusList = EnumSet.noneOf(InheritanceMode.class);
    for (Phenotype phenotype : phenotypes) {
      inheritanceModusList.addAll(phenotype.getInheritanceModes());
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
      phenotypeValuesToString(geneInheritanceValue.getHpoInheritanceModes()),
      geneInheritanceValue.isIncompletePenetrance() ? "1" : "",
      geneInheritanceValue.getSource()
    };
  }

  private static String phenotypeValuesToString(Set<HpoInheritanceMode> hpoInheritanceModes) {
    StringBuilder result = new StringBuilder();
    hpoInheritanceModes.stream()
        .sorted(Comparator.comparing(HpoInheritanceMode::getHpoId))
        .forEach(
            hpoInheritanceMode -> {
              if (result.length() != 0) {
                result.append(",");
              }
              result.append(
                  String.format(
                      "%s:%s",
                      hpoInheritanceMode.getHpoId(),
                      inheritanceModesToString(hpoInheritanceMode.getInheritanceModes())));
            });
    return result.toString();
  }

  private static String inheritanceModesToString(Set<InheritanceMode> inheritanceModes) {
    StringBuilder result = new StringBuilder();
    if (inheritanceModes != null) {
      inheritanceModes.stream()
          .filter(Objects::nonNull)
          .forEach(
              inheritanceMode -> {
                if (result.length() != 0) {
                  result.append(VALUE_SEPARATOR);
                }
                result.append(inheritanceMode.toString());
              });
    }
    return result.toString();
  }
}
