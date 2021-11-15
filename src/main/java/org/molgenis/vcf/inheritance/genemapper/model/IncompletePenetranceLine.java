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
public class IncompletePenetranceLine {
  @CsvBindByName(
      column = "Gene",
      required = true)
  String gene;
}