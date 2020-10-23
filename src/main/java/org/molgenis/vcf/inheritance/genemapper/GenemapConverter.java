package org.molgenis.vcf.inheritance.genemapper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.molgenis.vcf.inheritance.genemapper.model.GeneInheritanceValue;
import org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode;
import org.molgenis.vcf.inheritance.genemapper.model.Phenotype;

public class GenemapConverter {

  private GenemapConverter() {}

  public static final String COMMENT_PREFIX = "#";

  public static void run(Path input, Path ouput) {
    try {
      List<String> omimlines = Files.readAllLines(input);
      BufferedWriter writer = new BufferedWriter(new FileWriter(ouput.toFile()));
      omimlines.stream()
          .filter(line -> !line.startsWith(COMMENT_PREFIX))
          .map(OmimLineMapper::parseOmimLine)
          .forEach(omimHits -> writeToFile(writer, omimHits));
      writer.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void writeToFile(
      BufferedWriter writer, List<GeneInheritanceValue> geneInheritanceValues) {
    for (GeneInheritanceValue geneInheritanceValue : geneInheritanceValues) {
      try {
        writer.write(omimHitToString(geneInheritanceValue));
        writer.write("\n");
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  private static String omimHitToString(GeneInheritanceValue geneInheritanceValue) {
    return String.format(
        "%s\t%s\t%s",
        geneInheritanceValue.getGeneSymbol(),
        inheritanceModesToString(geneInheritanceValue.getInheritanceModes()),
        phenotypeValuesToString(geneInheritanceValue.getPhenotypes()));
  }

  private static String phenotypeValuesToString(List<Phenotype> phenotypes) {
    StringBuilder result = new StringBuilder();
    for (Phenotype phenotype : phenotypes) {
      if (result.length() != 0) {
        result.append(";");
      }
      result.append(
          String.format(
              "%s:%s",
              phenotype.getName(), inheritanceModesToString(phenotype.getInheritanceModes())));
    }
    return result.toString();
  }

  private static String inheritanceModesToString(Set<InheritanceMode> inheritanceModes) {
    StringBuilder result = new StringBuilder();
    inheritanceModes.stream()
        .sorted()
        .forEach(
            inheritanceMode -> {
              if (result.length() != 0) {
                result.append("/");
              }
              result.append(inheritanceMode.toString());
            });
    return result.toString();
  }
}
