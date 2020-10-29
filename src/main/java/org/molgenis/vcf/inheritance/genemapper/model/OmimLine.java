package org.molgenis.vcf.inheritance.genemapper.model;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvCustomBindByName;
import java.util.List;
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
  @CsvBindAndSplitByName(
      column = "Gene Symbols",
      required = true,
      elementType = String.class,
      splitOn = ",")
  List<String> genes;

  @CsvCustomBindByName(
      column = "Phenotypes",
      converter = TextToPhenotypeConverter.class)
  List<Phenotype> phenotypes;
}