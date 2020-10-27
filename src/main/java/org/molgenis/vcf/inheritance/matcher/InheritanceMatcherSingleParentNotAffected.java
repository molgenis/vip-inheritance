package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.Sample;

public class InheritanceMatcherSingleParentNotAffected {

  public static Set<InheritanceMode> getMatchingModes(VariantContext vc, Sample index,
      Sample parent) {
    return null;
  }
}
