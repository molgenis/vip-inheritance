package org.molgenis.vcf.inheritance.genemapper.model;

import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class HpoInheritanceMode {
  String hpoId;
  @NonNull Set<InheritanceMode> inheritanceModes;
}
