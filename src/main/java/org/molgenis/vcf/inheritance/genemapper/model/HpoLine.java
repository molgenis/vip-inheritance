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
      column = "database_id",
      required = true)
  String databaseId;

  @CsvBindByName(
      column = "hpo_id",
      required = true)
  String hpoId;
}