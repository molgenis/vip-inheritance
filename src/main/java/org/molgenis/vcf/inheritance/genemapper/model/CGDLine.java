package org.molgenis.vcf.inheritance.genemapper.model;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CGDLine {
  @CsvBindByName(
      column = "ENTREZ GENE ID",
      required = true)
  String gene;

  @CsvBindByName(
      column = "INHERITANCE",
      required = true)
  String inheritance;

}
