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
public class HpoLine {
  @CsvBindByName(
      column = "#DatabaseID",
      required = true)
  String databaseId;

  @CsvBindByName(
      column = "HPO_ID",
      required = true)
  String hpoId;
}