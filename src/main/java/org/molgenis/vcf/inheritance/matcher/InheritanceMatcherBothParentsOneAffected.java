package org.molgenis.vcf.inheritance.matcher;

import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.*;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.Sample;

public class InheritanceMatcherBothParentsOneAffected {

  public static Set<InheritanceMode> getMatchingModes(VariantContext vc, Allele allele, Sample index,
      Sample father, Sample mother) {
    return Set.of(AR,AD);//FIXME
  }
}
