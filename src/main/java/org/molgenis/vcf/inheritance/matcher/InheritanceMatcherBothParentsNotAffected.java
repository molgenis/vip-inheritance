package org.molgenis.vcf.inheritance.matcher;

import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.AR;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.XL;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.HashSet;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Sex;

public class InheritanceMatcherBothParentsNotAffected {

  public static Set<InheritanceMode> getMatchingModes(VariantContext vc, Allele allele, Sample index,
      Sample father, Sample mother) {
    Set<InheritanceMode> result = new HashSet<>();
    boolean denovo = SampleUtils.isDeNovo(vc, allele, index, father, mother);
    if(!denovo){
      if(SampleUtils.isHmz(vc,index) || SampleUtils.isHmz(vc, father) || SampleUtils.isHmz(vc, mother)){
        result.add(AR);
      }
    }
    if(index.getPerson().getSex() == Sex.MALE){
      if(!SampleUtils.hasVariant(vc, allele, father)){
        result.add(XL);
      }
    }
    return result;//FIXME
  }
}
