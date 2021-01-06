package org.molgenis.vcf.inheritance.genemapper;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.AD;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.AR;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.XL;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode;

class CgdMapperTest {

  @ParameterizedTest
  @MethodSource
  void mapCgdInheritanceMode(String input, Set<InheritanceMode> inheritanceModes) {
    assertEquals(CgdMapper.mapCgdInheritanceMode(input), inheritanceModes);
  }

  private static Stream<Arguments> mapCgdInheritanceMode() {
    return Stream.of(
        Arguments.of("AR", singleton(AR)),
        Arguments.of("AD", singleton(AD)),
        Arguments.of("XL", singleton(XL)),
        Arguments.of("AD/Digenic", singleton(AD)),
        Arguments.of("AD/Maternal/AR", Set.of(AD,AR)));
  }
}
