package org.molgenis.vcf.inheritance.matcher.model;

import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class InheritanceSampleAlleleResult {
  @NonNull Set<InheritanceMode> matchingModes;
  @NonNull CompoundResult compounds;
  @NonNull boolean denovo;
}
