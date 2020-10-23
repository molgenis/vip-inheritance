package org.molgenis.vcf.inheritance.genemapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.genemapper.model.GeneInheritanceValue;

class OmimLineMapperTest {

  @Test
  void parseOmimMultiGeneLine() {
    String input =
        "{Epilepsy, juvenile myoclonic, susceptibility to}, 613060 (3), Autosomal dominant; {Epilepsy, idiopathic generalized, 10}, 613060 (3), Autosomal dominant; {Epilepsy, generalized, with febrile seizures plus, type 5, susceptibility to}, 613060 (3), Autosomal dominant";
    GeneInheritanceValue expected = GeneInheritanceValue.builder().build();

    assertEquals(expected, "");
  }

  @Test
  void parseOmimBracketPhenoLine() {
  }

  @Test
  void parseOmimLine() {
  }
}