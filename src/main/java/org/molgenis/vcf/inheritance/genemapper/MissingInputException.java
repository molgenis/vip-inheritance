package org.molgenis.vcf.inheritance.genemapper;

public class MissingInputException extends RuntimeException {

  private static final String MESSAGE = "At least one input file, omim or cgd, is required.";

  @Override
  public String getMessage() {
    return MESSAGE;
  }
}
