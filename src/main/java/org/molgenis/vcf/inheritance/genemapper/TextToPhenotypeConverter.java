package org.molgenis.vcf.inheritance.genemapper;

import com.opencsv.bean.AbstractBeanField;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode;
import org.molgenis.vcf.inheritance.genemapper.model.Phenotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextToPhenotypeConverter extends AbstractBeanField<List<Phenotype>, Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextToPhenotypeConverter.class);

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
        if (!phenotypeName.isEmpty()) {
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
            case "X-LINKED DOMINANT" -> modes.add(InheritanceMode.XLD);
            case "X-LINKED RECESSIVE" -> modes.add(InheritanceMode.XLR);
            case "X-LINKED" -> modes.add(InheritanceMode.XL);
            case "Y-LINKED" -> modes.add(InheritanceMode.YL);
            case "MITOCHONDRIAL" -> modes.add(InheritanceMode.MT);
            case "AUTOSOMAL RECESSIVE" -> modes.add(InheritanceMode.AR);
            case "AUTOSOMAL DOMINANT" -> modes.add(InheritanceMode.AD);
            default -> LOGGER.info("Unsupported OMIM inheritance value: '{}'", value);
        }
    }
    return modes;
  }

  private static String preprocessValue(String value) {
    value = value.toUpperCase(Locale.ROOT).trim();
    if(value.startsWith("?")){
      value = value.substring(1);
    }
    return value;
  }
}
