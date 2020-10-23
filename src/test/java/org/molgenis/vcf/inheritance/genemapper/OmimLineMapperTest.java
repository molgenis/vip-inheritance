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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.molgenis.vcf.inheritance.genemapper.model.GeneInheritanceValue;
import org.molgenis.vcf.inheritance.genemapper.model.Phenotype;

class OmimLineMapperTest {
  @Test
  void parseOmimLine() {
    String input =
        "chr50\t123456\t223456\t50p.1\t50p.2\t123458\tTEST3\tGamma-aminobutyric acid (GABA) A receptor, delta\tTEST8\t5923\ttest3\t\t"
            + "Epilepsy, juvenile myoclonic, susceptibility to, 123458 (3), Autosomal dominant\tGabrd (MGI:95622)";
    List<GeneInheritanceValue> expected = new ArrayList<>();
    Phenotype pheno =
        Phenotype.builder()
            .name("Epilepsy__juvenile_myoclonic__susceptibility_to")
            .inheritanceModes(Set.of(AD))
            .build();
    expected.add(
        GeneInheritanceValue.builder()
            .geneSymbol("TEST3")
            .inheritanceModes(Set.of(AD))
            .phenotypes(Collections.singletonList(pheno))
            .build());
    assertEquals(expected, OmimLineMapper.parseOmimLine(input));
  }

  @Test
  void parseOmimLineAllValues() {
    String input =
        "chr50\t123456\t223456\t50p.1\t50p.2\t123458\tTEST3\tGamma-aminobutyric acid (GABA) A receptor, delta\tTEST8\t5923\ttest3\t\t"
            + "Epilepsy, juvenile myoclonic, susceptibility to, 123458 (3), Y-LINKED,X-LINKED DOMINANT,X-LINKED RECESSIVE,?X-LINKED RECESSIVE,X-LINKED,AUTOSOMAL RECESSIVE,AUTOSOMAL DOMINANT,?AUTOSOMAL DOMINANT,PSEUDOAUTOSOMAL RECESSIVE,PSEUDOAUTOSOMAL DOMINANT,ISOLATED CASES,DIGENIC,DIGENIC RECESSIVE,DIGENIC DOMINANT,MITOCHONDRIAL,MULTIFACTORIAL,SOMATIC MUTATION,SOMATIC MOSAICISM,INHERITED CHROMOSOMAL IMBALANCE\tGabrd (MGI:95622)";
    List<GeneInheritanceValue> expected = new ArrayList<>();
    Phenotype pheno =
        Phenotype.builder()
            .name("Epilepsy__juvenile_myoclonic__susceptibility_to")
            .inheritanceModes(Set.of(AD,Q_AD,AR,XD,XR,QXR,XL,YL,PD,PR,IC,DG,MF,SM,DGR,DGD,MT,SMM,ICI))
            .build();
    expected.add(
        GeneInheritanceValue.builder()
            .geneSymbol("TEST3")
            .inheritanceModes(Set.of(AD,Q_AD,AR,XD,XR,QXR,XL,YL,PD,PR,IC,DG,MF,SM,DGR,DGD,MT,SMM,ICI))
            .phenotypes(Collections.singletonList(pheno))
            .build());
    assertEquals(expected, OmimLineMapper.parseOmimLine(input));
  }

  @Test
  void parseOmimMultiGeneLine() {
    String input =
        "chr30\t123456\t223456\t30p.1\t30p.2\t123456\tTEST1, TEST2\ttest string\tTEST1\t1548\ttest1\t\t"
            + "Mental retardation, autosomal dominant 42, 123456 (3), Autosomal dominant\tGnb1 (MGI:95781)";
    Phenotype pheno =
        Phenotype.builder()
            .name("Mental_retardation__autosomal_dominant_42")
            .inheritanceModes(Set.of(AD))
            .build();
    List<GeneInheritanceValue> expected =
        Arrays.asList(
            GeneInheritanceValue.builder()
                .geneSymbol("TEST1")
                .inheritanceModes(Set.of(AD))
                .phenotypes(Collections.singletonList(pheno))
                .build(),
            GeneInheritanceValue.builder()
                .geneSymbol("TEST2")
                .inheritanceModes(Set.of(AD))
                .phenotypes(Collections.singletonList(pheno))
                .build());

    assertEquals(expected, OmimLineMapper.parseOmimLine(input));
  }

  @Test
  void parseOmimMultiPhenoLine() {
    String input =
        "chr30\t123456\t223456\t30p.1\t30p.2\t123456\tTEST1\ttest string\tTEST1\t1548\ttest1\t\t"
            + "Mental retardation, autosomal dominant 42, 123456 (3), Autosomal dominant; "
            + "Leukemia, acute lymphoblastic, somatic, 123457 (3), Somatic Mutation\tGnb1 (MGI:95781)";
    List<GeneInheritanceValue> expected = new ArrayList<>();
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
    expected.add(
        GeneInheritanceValue.builder()
            .geneSymbol("TEST1")
            .inheritanceModes(Set.of(AD,SM))
            .phenotypes(Arrays.asList(pheno1,pheno2))
            .build());

    assertEquals(expected, OmimLineMapper.parseOmimLine(input));
  }

  @Test
  void parseOmimMultiPhenoLineMissingMode() {
    String input =
        "chr30\t123456\t223456\t30p.1\t30p.2\t123456\tTEST1\ttest string\tTEST1\t1548\ttest1\t\t"
            + "Mental retardation, autosomal dominant 42, 123456 (3), Autosomal dominant; "
            + "Leukemia, acute lymphoblastic, somatic, 123457 (3)\tGnb1 (MGI:95781)";
    List<GeneInheritanceValue> expected = new ArrayList<>();
    Phenotype pheno =
        Phenotype.builder()
            .name("Mental_retardation__autosomal_dominant_42")
            .inheritanceModes(Set.of(AD))
            .build();
    expected.add(
        GeneInheritanceValue.builder()
            .geneSymbol("TEST1")
            .inheritanceModes(Set.of(AD))
            .phenotypes(Collections.singletonList(pheno))
            .build());

    assertEquals(expected, OmimLineMapper.parseOmimLine(input));
  }

  @Test
  void parseOmimBracketPhenoLine() {
    String input =
        "chr50\t123456\t223456\t50p.1\t50p.2\t123458\tTEST3\tGamma-aminobutyric acid (GABA) A receptor, delta\tTEST8\t5923\ttest3\t\t"
            + "{Epilepsy, generalized, with febrile seizures plus, type 5, susceptibility to}, 123456 (3), Autosomal dominant\tGabrd (MGI:95622)";
    List<GeneInheritanceValue> expected = new ArrayList<>();
    Phenotype pheno =
        Phenotype.builder()
            .name("{Epilepsy__generalized__with_febrile_seizures_plus__type_5__susceptibility_to}")
            .inheritanceModes(Set.of(AD))
            .build();
    expected.add(
        GeneInheritanceValue.builder()
            .geneSymbol("TEST3")
            .inheritanceModes(Set.of(AD))
            .phenotypes(Collections.singletonList(pheno))
            .build());

    assertEquals(expected, OmimLineMapper.parseOmimLine(input));
  }
}
