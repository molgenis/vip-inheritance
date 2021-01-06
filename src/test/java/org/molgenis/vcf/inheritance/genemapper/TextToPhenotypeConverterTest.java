package org.molgenis.vcf.inheritance.genemapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.*;

import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.genemapper.model.Phenotype;

class TextToPhenotypeConverterTest {

  private TextToPhenotypeConverter textToPhenotypeConverter;

  @BeforeEach
  void setUp() {
    textToPhenotypeConverter = new TextToPhenotypeConverter();
  }

  @Test
  void parseOmimLine() {
    String input =
        "Epilepsy, juvenile myoclonic, susceptibility to, 123458 (3), Autosomal dominant";

    Phenotype pheno =
        Phenotype.builder()
            .name("Epilepsy__juvenile_myoclonic__susceptibility_to")
            .inheritanceModes(Set.of(AD))
            .build();
    Set<Phenotype> expected = Collections.singleton(pheno);
    assertEquals(expected, textToPhenotypeConverter.convert(input));
  }

  @Test
  void parseOmimLineAllValues() {
    String input ="Epilepsy, juvenile myoclonic, susceptibility to, 123458 (3),X-LINKED DOMINANT,X-LINKED RECESSIVE,?X-LINKED RECESSIVE,X-LINKED,AUTOSOMAL RECESSIVE,AUTOSOMAL DOMINANT,?AUTOSOMAL DOMINANT";
    Phenotype pheno =
        Phenotype.builder()
            .name("Epilepsy__juvenile_myoclonic__susceptibility_to")
            .inheritanceModes(Set.of(AD,AR,XD,XR,XL))
            .build();
    Set<Phenotype> expected = Collections.singleton(pheno);
    assertEquals(expected, textToPhenotypeConverter.convert(input));
  }

  @Test
  void parseOmimMultiGeneLine() {
    String input =
        "Mental retardation, autosomal dominant 42, 123456 (3), Autosomal recessive";
    Phenotype pheno =
        Phenotype.builder()
            .name("Mental_retardation__autosomal_dominant_42")
            .inheritanceModes(Set.of(AR))
            .build();
    Set<Phenotype> expected = Collections.singleton(pheno);
    assertEquals(expected, textToPhenotypeConverter.convert(input));
  }

  @Test
  void parseOmimMultiPhenoLine() {
    String input ="Mental retardation, autosomal dominant 42, 123456 (3), Autosomal dominant; "
            + "Leukemia, acute lymphoblastic, somatic, 123457 (3), X-Linked";

    Phenotype pheno1 =
        Phenotype.builder()
            .name("Mental_retardation__autosomal_dominant_42")
            .inheritanceModes(Set.of(AD))
            .build();
    Phenotype pheno2 =
        Phenotype.builder()
            .name("Leukemia__acute_lymphoblastic__somatic")
            .inheritanceModes(Set.of(XL))
            .build();
    Set<Phenotype> expected = Set.of(pheno1, pheno2);
    assertEquals(expected, textToPhenotypeConverter.convert(input));
  }

  @Test
  void parseOmimMultiPhenoLineMissingMode() {
    String input =
        "Mental retardation, autosomal dominant 42, 123456 (3), Autosomal dominant; "
            + "Leukemia, acute lymphoblastic, somatic, 123457 (3)";

    Phenotype pheno =
        Phenotype.builder()
            .name("Mental_retardation__autosomal_dominant_42")
            .inheritanceModes(Set.of(AD))
            .build();
    Set<Phenotype> expected = Collections.singleton(pheno);

    assertEquals(expected, textToPhenotypeConverter.convert(input));
  }

  @Test
  void parseOmimBracketPhenoLine() {
    String input =
        "{Epilepsy, generalized, with febrile seizures plus, type 5, susceptibility to}, 123456 (3), X-Linked";
    Phenotype pheno =
        Phenotype.builder()
            .name("{Epilepsy__generalized__with_febrile_seizures_plus__type_5__susceptibility_to}")
            .inheritanceModes(Set.of(XL))
            .build();
    Set<Phenotype> expected = Collections.singleton(pheno);
    assertEquals(expected, textToPhenotypeConverter.convert(input));
  }
}
