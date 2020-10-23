package org.molgenis.vcf.inheritance.genemapper.model;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class GeneInheritanceValue {
  @NonNull String geneSymbol;
  List<Phenotype> phenotypes;
  Set<InheritanceMode> inheritanceModes;
}
