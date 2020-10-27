package org.molgenis.vcf.inheritance.matcher;

import static java.util.Arrays.asList;
import static org.molgenis.vcf.inheritance.matcher.SampleUtils.hasVariant;
import static org.molgenis.vcf.inheritance.matcher.SampleUtils.isDeNovo;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.molgenis.vcf.inheritance.matcher.model.CompoundResult;
import org.molgenis.vcf.inheritance.matcher.model.Sample;

public class VariantListUtils {

  private static final String INFO_DESCRIPTION_PREFIX =
      "Consequence annotations from Ensembl VEP. Format: ";

  static Map<String, Set<VariantContext>> getVariantsPerGene(VCFFileReader vcfReader) {
    Map<String, Set<VariantContext>> result = new HashMap<>();
    int vepGeneIndex = getGeneIndex(vcfReader.getFileHeader());
    for (VariantContext vc : vcfReader) {
      // FIXME extract vep info id from header
      List<String> vepValues = vc.getAttributeAsStringList("CSQ", "");
      for (String vepValue : vepValues) {
        String[] vepSplit = vepValue.split("\\|", -1);
        String gene = vepSplit[vepGeneIndex];
        Set<VariantContext> variants;
        if (result.containsKey(gene)) {
          variants = result.get(gene);
        } else {
          variants = new HashSet<>();
        }
        variants.add(vc);
        result.put(gene, variants);
      }
    }
    return result;
  }

  public static Set<String> getGenesForVariant(VariantContext vc, VCFHeader header) {
    // FIXME: provide index instead of header
    int vepGeneIndex = getGeneIndex(header);
    List<String> vepValues = vc.getAttributeAsStringList("CSQ", "");
    Set<String> genes = new HashSet<>();
    for (String vepValue : vepValues) {
      String[] vepSplit = vepValue.split("\\|", -1);
      String gene = vepSplit[vepGeneIndex];
      genes.add(gene);
    }
    return genes;
  }

  public static Set<VariantContext> getVariantsInGeneForSample(
      Map<String, Set<VariantContext>> variantsMap, String gene, Sample sample) {
    Set<VariantContext> variants = variantsMap.get(gene);
    return variants.stream()
        .filter(vc -> hasVariantInGene(vc, sample))
        .collect(Collectors.toSet());
  }

  public static CompoundResult getCompound(
      VariantContext vc,
      Allele allele,
      Map<String, Set<VariantContext>> variantsMap,
      Sample sample,
      Sample father,
      Sample mother,
      VCFHeader header) {
    Set<String> genes = getGenesForVariant(vc, header);
    Set<Variant> otherVariants = new HashSet<>();
    for (String gene : genes) {
      List<VariantContext> variants =
          getVariantsInGeneForSample(variantsMap, gene, sample).stream()
              .filter(variant -> !variant.equals(vc))
              .collect(Collectors.toList());
      if (variants.size() == 1) return CompoundResult.builder().isCompound(false).build();
      boolean fatherHasVariant = false;
      boolean motherHasVariant = false;
      for (VariantContext other : variants) {
        boolean isOtherDeNovo = isDeNovo(other, allele, sample, father, mother);
        for (Allele otherAllele : other.getAlternateAlleles()) {
          fatherHasVariant = hasVariant(vc, otherAllele, father);
          motherHasVariant = hasVariant(vc, otherAllele, mother);
          if (fatherHasVariant && motherHasVariant
              || (isOtherDeNovo && (fatherHasVariant || motherHasVariant))) {
            otherVariants.add(
                Variant.builder().allele(otherAllele).gene(gene).variantContext(other).build());
          }
        }
      }
    }
    if (otherVariants.isEmpty()) {
      return CompoundResult.builder().isCompound(false).build();
    } else {
      return CompoundResult.builder().isCompound(true).variantContextList(otherVariants).build();
    }
  }

  private static boolean canMap(VCFInfoHeaderLine vcfInfoHeaderLine) {
    // match on the description since the INFO ID is configurable (default: CSQ)
    String description = vcfInfoHeaderLine.getDescription();
    return description.startsWith(INFO_DESCRIPTION_PREFIX);
  }

  private static int getGeneIndex(VCFHeader header) {
    for (VCFInfoHeaderLine vcfInfoHeaderLine : header.getInfoHeaderLines())
      if (canMap(vcfInfoHeaderLine)) {
        List<String> nestedInfo = getNestedInfoIds(vcfInfoHeaderLine);
        if (nestedInfo.contains("SYMBOL")) {
          return nestedInfo.indexOf("SYMBOL");
        }
      }
    throw new UnsupportedOperationException("Missing VEP(SYMBOL) information.");
  }

  private static List<String> getNestedInfoIds(VCFInfoHeaderLine vcfInfoHeaderLine) {
    String description = vcfInfoHeaderLine.getDescription();
    String[] infoIds = description.substring(INFO_DESCRIPTION_PREFIX.length()).split("\\|", -1);
    return asList(infoIds);
  }

  private static boolean hasVariantInGene(VariantContext vc, Sample sample) {
    List<Allele> alleles = vc.getAlternateAlleles();
    for (Allele allele : alleles) {
      if (hasVariant(vc, allele, sample)) {
        return true;
      }
    }
    return false;
  }
}
