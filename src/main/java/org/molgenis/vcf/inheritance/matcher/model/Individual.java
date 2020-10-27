package org.molgenis.vcf.inheritance.matcher.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Individual {
  @JsonProperty("id")
  @NonNull
  String id;
}
