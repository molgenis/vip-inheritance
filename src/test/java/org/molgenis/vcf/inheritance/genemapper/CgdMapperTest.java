package org.molgenis.vcf.inheritance.genemapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.AD;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.AR;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.XL;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode;

class CgdMapperTest {

  @ParameterizedTest
  @MethodSource
  void mapCgdInheritanceMode(String input, InheritanceMode inheritanceMode) {
    assertEquals(CgdMapper.mapCgdInheritanceMode(input), inheritanceMode);
  }

  private static Stream<Arguments> mapCgdInheritanceMode() {
    return Stream.of(Arguments.of("AR", AR), Arguments.of("AD", AD), Arguments.of("XL", XL));
  }
}
