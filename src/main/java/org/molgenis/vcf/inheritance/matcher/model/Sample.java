package org.molgenis.vcf.inheritance.matcher.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Sample {
  @JsonProperty("person")
  @NonNull
  Person person;

  // index of the sample in the VCF, -1 means the sample is not available in the file.
  @JsonProperty("index")
  @NonNull
  int index;
}
