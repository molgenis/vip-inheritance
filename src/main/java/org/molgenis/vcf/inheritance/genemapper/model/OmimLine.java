package org.molgenis.vcf.inheritance.genemapper.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.molgenis.vcf.inheritance.genemapper.TextToPhenotypeConverter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OmimLine {
  @CsvBindByName(
      column = "Ensembl Gene ID",
      required = true)
  String gene;

  @CsvCustomBindByName(
      column = "Phenotypes",
      converter = TextToPhenotypeConverter.class)
  Set<Phenotype> phenotypes;
}