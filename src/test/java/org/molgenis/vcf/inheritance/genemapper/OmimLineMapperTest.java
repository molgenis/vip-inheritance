package org.molgenis.vcf.inheritance.genemapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.molgenis.vcf.inheritance.genemapper.model.GeneInheritanceValue;
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
    List<Phenotype> expected = Collections.singletonList(pheno);
    assertEquals(expected, textToPhenotypeConverter.convert(input));
  }

  @Test
  void parseOmimLineAllValues() {
    String input ="Epilepsy, juvenile myoclonic, susceptibility to, 123458 (3), Y-LINKED,X-LINKED DOMINANT,X-LINKED RECESSIVE,?X-LINKED RECESSIVE,X-LINKED,AUTOSOMAL RECESSIVE,AUTOSOMAL DOMINANT,?AUTOSOMAL DOMINANT,PSEUDOAUTOSOMAL RECESSIVE,PSEUDOAUTOSOMAL DOMINANT,ISOLATED CASES,DIGENIC,DIGENIC RECESSIVE,DIGENIC DOMINANT,MITOCHONDRIAL,MULTIFACTORIAL,SOMATIC MUTATION,SOMATIC MOSAICISM,INHERITED CHROMOSOMAL IMBALANCE";
    Phenotype pheno =
        Phenotype.builder()
            .name("Epilepsy__juvenile_myoclonic__susceptibility_to")
            .inheritanceModes(Set.of(AD,AR,XD,XR,XL,YL,PD,PR,IC,DG,MF,SM,DGR,DGD,MT,SMM,ICI))
            .build();
    List<Phenotype> expected = Collections.singletonList(pheno);
    assertEquals(expected, textToPhenotypeConverter.convert(input));
  }

  @Test
  void parseOmimMultiGeneLine() {
    String input =
        "Mental retardation, autosomal dominant 42, 123456 (3), Autosomal dominant";
    Phenotype pheno =
        Phenotype.builder()
            .name("Mental_retardation__autosomal_dominant_42")
            .inheritanceModes(Set.of(AD))
            .build();
    List<Phenotype> expected = Collections.singletonList(pheno);
    assertEquals(expected, textToPhenotypeConverter.convert(input));
  }

  @Test
  void parseOmimMultiPhenoLine() {
    String input ="Mental retardation, autosomal dominant 42, 123456 (3), Autosomal dominant; "
            + "Leukemia, acute lymphoblastic, somatic, 123457 (3), Somatic Mutation";

    Phenotype pheno1 =
        Phenotype.builder()
            .name("Mental_retardation__autosomal_dominant_42")
            .inheritanceModes(Set.of(AD))
            .build();
    Phenotype pheno2 =
        Phenotype.builder()
            .name("Leukemia__acute_lymphoblastic__somatic")
            .inheritanceModes(Set.of(SM))
            .build();
    List<Phenotype> expected = Arrays.asList(pheno1, pheno2);
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
    List<Phenotype> expected = Collections.singletonList(pheno);

    assertEquals(expected, textToPhenotypeConverter.convert(input));
  }

  @Test
  void parseOmimBracketPhenoLine() {
    String input =
        "{Epilepsy, generalized, with febrile seizures plus, type 5, susceptibility to}, 123456 (3), Autosomal dominant";
    Phenotype pheno =
        Phenotype.builder()
            .name("{Epilepsy__generalized__with_febrile_seizures_plus__type_5__susceptibility_to}")
            .inheritanceModes(Set.of(AD))
            .build();
    List<Phenotype> expected = Collections.singletonList(pheno);
    assertEquals(expected, textToPhenotypeConverter.convert(input));
  }
}
