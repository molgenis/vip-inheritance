package org.molgenis.vcf.inheritance.genemapper;

import java.io.Serial;

public class MissingInputException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "At least one input file, omim or cgd, is required.";

    @Override
    public String getMessage() {
        return MESSAGE;
    }
}
