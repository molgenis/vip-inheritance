package org.molgenis.vcf.inheritance.matcher.model;

import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.molgenis.vcf.inheritance.matcher.Variant;

@Value
@Builder
public class CompoundResult {
  @NonNull  boolean isCompound;
  Set<Variant> variantContextList;
}
