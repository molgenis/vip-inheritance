package org.molgenis.vcf.inheritance.genemapper;

import com.opencsv.bean.AbstractBeanField;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode;
import org.molgenis.vcf.inheritance.genemapper.model.Phenotype;

public class TextToPhenotypeConverter extends AbstractBeanField<List<Phenotype>, Object> {

  public static final String SUBVALUE_SEPARATOR = ";";
  public static final int COMMENTS_INDEX = 12;
  public static final String PATTERN = "(.*),\\s((\\d*)\\s(\\(\\d*\\))),(.*)";

  @Override
  protected Set<Phenotype> convert(String phenotypeValue){
    String[] phenotypes = phenotypeValue.split(SUBVALUE_SEPARATOR);
    EnumSet<InheritanceMode> inheritanceModes = EnumSet.noneOf(InheritanceMode.class);
    return processPhenotypes(phenotypes, inheritanceModes);
  }

  private static Set<Phenotype> processPhenotypes(
      String[] phenotypes, Set<InheritanceMode> inheritanceModes) {
    Set<Phenotype> phenotypeList = new HashSet<>();

    for (String phenotypeString : phenotypes) {
      phenotypeString = phenotypeString.trim();
      Pattern p = Pattern.compile(PATTERN);
      Matcher m = p.matcher(phenotypeString);
      if (m.matches()) {
        String phenotypeName = replaceIllegalChars(m.group(1));
        String omimId = m.group(3);
        String[] inheritance = m.group(5).split(",");
        Set<InheritanceMode> phenoInheritanceModes = mapInheritanceModes(inheritance);
        inheritanceModes.addAll(phenoInheritanceModes);
        if (phenotypeName.length() != 0) {
          phenotypeList.add(
              Phenotype.builder().omimId(omimId).inheritanceModes(phenoInheritanceModes).build());
        }
      }
    }
    return phenotypeList;
  }

  private static String replaceIllegalChars(String value) {
    return value.replace(",", "_").replace(" ", "_");
  }

  private static Set<InheritanceMode> mapInheritanceModes(String[] values) {
    EnumSet<InheritanceMode> modes = EnumSet.noneOf(InheritanceMode.class);
    for (String value : values) {
      value = preprocessValue(value);
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
        case "X-LINKED":
          modes.add(InheritanceMode.XL);
          break;
        case "AUTOSOMAL RECESSIVE":
          modes.add(InheritanceMode.AR);
          break;
        case "AUTOSOMAL DOMINANT":
          modes.add(InheritanceMode.AD);
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

  private static String preprocessValue(String value) {
    value = value.toUpperCase().trim();
    if(value.startsWith("?")){
      value = value.substring(1);
    }
    return value;
  }
}
