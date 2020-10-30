package org.molgenis.vcf.inheritance.genemapper.model;

import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Phenotype{
  @NonNull String name;
  Set<InheritanceMode> inheritanceModes;
}
