package org.molgenis.vcf.inheritance.matcher.model;

import java.util.Map;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class InheritanceResult {
  @NonNull Map<Sample, InheritanceSampleResult> inheritanceSampleResults;
}
