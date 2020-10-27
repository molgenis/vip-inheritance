package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Variant {
  @NonNull Allele allele;
  @NonNull String gene;
  @NonNull VariantContext variantContext;
}
