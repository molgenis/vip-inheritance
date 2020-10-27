package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.model.Person;
import org.molgenis.vcf.inheritance.matcher.model.Sample;

public class SampleUtils {
  public static Sample getSample(Person index, Map<String, Sample> pedigree, String personId) {
    Sample sample = pedigree.get(personId);
    if(sample != null && sample.getIndex() == -1){
      //FIXME: log warning
      sample = null;
    }
    return sample;
  }

  public static Sample getFather(Person index, Map<String, Sample> pedigree) {
    return getSample(index, pedigree, index.getPaternalId());
  }

  public static Sample getMother(Person index, Map<String, Sample> pedigree) {
    return getSample(index, pedigree, index.getMaternalId());
  }


  public static boolean isDeNovo(VariantContext vc, Allele allele, Sample index, Sample father, Sample mother) {
    if(!hasVariant(vc,allele,index)){
      return false;
    }
    return hasVariant(vc, allele, father) && hasVariant(vc, allele, mother);
  }

  public static boolean isHmz(VariantContext vc, Sample sample) {
    Genotype genotype = vc
        .getGenotype(sample.getPerson().getIndividualId());
    return genotype.isHom();
  }

  public static boolean hasVariant(VariantContext vc, Allele allele, Sample sample) {
    Genotype genotype = vc
        .getGenotype(sample.getPerson().getIndividualId());
    return genotype.getAlleles().contains(allele);
  }
}
