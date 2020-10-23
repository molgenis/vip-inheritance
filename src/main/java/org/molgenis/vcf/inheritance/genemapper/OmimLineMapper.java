package org.molgenis.vcf.inheritance.genemapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode;
import org.molgenis.vcf.inheritance.genemapper.model.GeneInheritanceValue;
import org.molgenis.vcf.inheritance.genemapper.model.Phenotype;

public class OmimLineMapper {
  public static final String SUBVALUE_SEPARATOR = ";";
  public static final String VALUE_SEPARATOR = ",";
  public static final int COMMENTS_INDEX = 12;
  public static final int GENE_INDEX = 6;

  private OmimLineMapper() {}

  static List<GeneInheritanceValue> parseOmimLine(String line) {
    List<GeneInheritanceValue> geneInheritanceValues = new ArrayList<>();
    String[] split = line.split("\t", -1);
    String[] genes = split[GENE_INDEX].split(VALUE_SEPARATOR);
    String[] phenotypes = split[COMMENTS_INDEX].split(SUBVALUE_SEPARATOR);
    Set<InheritanceMode> inheritanceModes = new HashSet<>();
    List<Phenotype> phenotypeList = processPhenotypes(phenotypes, inheritanceModes);

    if (!inheritanceModes.isEmpty()) {
      for (String gene : genes) {
        GeneInheritanceValue geneInheritanceValue =
            GeneInheritanceValue.builder()
                .geneSymbol(gene.trim())
                .inheritanceModes(inheritanceModes)
                .phenotypes(phenotypeList)
                .build();
        geneInheritanceValues.add(geneInheritanceValue);
      }
    }
    return geneInheritanceValues;
  }

  private static List<Phenotype> processPhenotypes(
      String[] phenotypes, Set<InheritanceMode> inheritanceModes) {
    List<Phenotype> phenotypeList = new ArrayList<>();

    for (String phenotypeString : phenotypes) {
      phenotypeString = phenotypeString.trim();
      Pattern p = Pattern.compile("(.*),\\s(\\d*\\s\\(\\d*\\)),(.*)");
      Matcher m = p.matcher(phenotypeString);
      if (m.matches()) {
        String phenotypeName = replaceIlligalChars(m.group(1));
        String[] inheritance = m.group(3).split(",");
        Set<InheritanceMode> phenoInheritanceModes;
        phenoInheritanceModes = mapInheritanceModes(inheritance);
        inheritanceModes.addAll(phenoInheritanceModes);
        if (phenotypeName.length() != 0) {
          phenotypeList.add(
              Phenotype.builder().name(phenotypeName).inheritanceModes(phenoInheritanceModes).build());
        }
      }
    }
    return phenotypeList;
  }

  private static String replaceIlligalChars(String value) {
    return value.replace(",", "_").replace(" ", "_");
  }

  private static Set<InheritanceMode> mapInheritanceModes(String[] values) {
    Set<InheritanceMode> modes = new HashSet<>();
    for (String value : values) {
      value = value.toUpperCase().trim();
      switch (value) {
        case "Y-LINKED":
          modes.add(InheritanceMode.YL);
          break;
        case "X-LINKED DOMINANT":
          modes.add(InheritanceMode.XD);
          break;
        case "X-LINKED RECESSIVE":
          modes.add(InheritanceMode.XR);
          break;
        case "?X-LINKED RECESSIVE":
          modes.add(InheritanceMode.QXR);
          break;
        case "X-LINKED":
          modes.add(InheritanceMode.XL);
          break;
        case "AUTOSOMAL RECESSIVE":
          modes.add(InheritanceMode.AR);
          break;
        case "AUTOSOMAL DOMINANT":
          modes.add(InheritanceMode.AD);
          break;
        case "?AUTOSOMAL DOMINANT":
          modes.add(InheritanceMode.Q_AD);
          break;
        case "PSEUDOAUTOSOMAL RECESSIVE":
          modes.add(InheritanceMode.PR);
          break;
        case "PSEUDOAUTOSOMAL DOMINANT":
          modes.add(InheritanceMode.PD);
          break;
        case "ISOLATED CASES":
          modes.add(InheritanceMode.IC);
          break;
        case "DIGENIC":
          modes.add(InheritanceMode.DG);
          break;
        case "DIGENIC RECESSIVE":
          modes.add(InheritanceMode.DGR);
          break;
        case "DIGENIC DOMINANT":
          modes.add(InheritanceMode.DGD);
          break;
        case "MITOCHONDRIAL":
          modes.add(InheritanceMode.MT);
          break;
        case "MULTIFACTORIAL":
          modes.add(InheritanceMode.MF);
          break;
        case "SOMATIC MUTATION":
          modes.add(InheritanceMode.SM);
          break;
        case "SOMATIC MOSAICISM":
          modes.add(InheritanceMode.SMM);
          break;
        case "INHERITED CHROMOSOMAL IMBALANCE":
          modes.add(InheritanceMode.ICI);
          break;
        default:
          throw new IllegalStateException(value);
      }
    }
    return modes;
  }
}
