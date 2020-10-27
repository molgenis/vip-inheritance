package org.molgenis.vcf.inheritance.matcher.model;

import htsjdk.variant.variantcontext.Allele;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class InheritanceSampleResult {
  @NonNull Person index;
  @NonNull Map<Allele, InheritanceSampleAlleleResult> inheritanceSampleAlleleResults;
}
