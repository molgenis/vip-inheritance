package org.molgenis.vcf.inheritance.matcher;

import static org.molgenis.vcf.inheritance.matcher.SampleUtils.isHmz;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.*;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.Sample;

public class InheritanceMatcherNoParent {

  public static Set<InheritanceMode> getMatchingModes(VariantContext vc, Sample index) {
    Set<InheritanceMode> matchingModes = new HashSet<>();
    matchingModes.addAll(Arrays.asList(AD));
    if(vc.getContig().equals("X")){
      matchingModes.add(XL);
    }
    if(isHmz(vc,index)){
      matchingModes.add(AR);
    }
    //FIXME: compounds
    return matchingModes;
  }
}
