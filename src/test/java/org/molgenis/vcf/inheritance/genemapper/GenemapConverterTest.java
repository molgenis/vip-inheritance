package org.molgenis.vcf.inheritance.genemapper;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.AD;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.AR;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.XLD;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.XL;

import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.genemapper.model.CgdLine;
import org.molgenis.vcf.inheritance.genemapper.model.GeneInheritanceValue;
import org.molgenis.vcf.inheritance.genemapper.model.HpoInheritanceMode;
import org.molgenis.vcf.inheritance.genemapper.model.OmimLine;
import org.molgenis.vcf.inheritance.genemapper.model.Phenotype;

class GenemapConverterTest {

  @BeforeEach
  void restLoggingEvents() {
    TestAppender.reset();
  }

  @Test
  void testMissingGeneException() throws NoSuchFieldException {
    CsvRequiredFieldEmptyException csvParsingException = mock(CsvRequiredFieldEmptyException.class);
    Field field = OmimLine.class.getDeclaredField("gene");
    when(csvParsingException.getDestinationField()).thenReturn(field);
    when(csvParsingException.getLine()).thenReturn(new String[] {"non", "comment", "line"});
    GenemapConverter.handleCsvParseExceptions(Collections.singletonList(csvParsingException));
    assertEquals(0, TestAppender.events.size());
  }

  @Test
  void testCommentParseException() throws NoSuchFieldException {
    CsvRequiredFieldEmptyException csvParsingException = mock(CsvRequiredFieldEmptyException.class);
    when(csvParsingException.getLine())
        .thenReturn(new String[] {"#This", "is", "a", "comment", "line"});
    GenemapConverter.handleCsvParseExceptions(Collections.singletonList(csvParsingException));
    assertEquals(0, TestAppender.events.size());
  }

  @Test
  void testOtherException() throws NoSuchFieldException {
    CsvRequiredFieldEmptyException csvParsingException = mock(CsvRequiredFieldEmptyException.class);
    Field field = OmimLine.class.getDeclaredField("phenotypes");
    when(csvParsingException.getDestinationField()).thenReturn(field);
    when(csvParsingException.getLine()).thenReturn(new String[] {"non", "comment", "line"});
    when(csvParsingException.getMessage()).thenReturn("test message");
    when(csvParsingException.getLineNumber()).thenReturn(123l);
    GenemapConverter.handleCsvParseExceptions(Collections.singletonList(csvParsingException));
    assertAll(
        () -> assertEquals(1, TestAppender.events.size()),
        () -> assertEquals("123,test message", TestAppender.events.get(0).getMessage()));
  }

  @Test
  void convertToGeneInheritanceValue() {
    Phenotype phenotype1 = Phenotype.builder().omimId("123").inheritanceModes(Set.of(XLD)).build();
    Phenotype phenotype2 =
        Phenotype.builder().omimId("1234").inheritanceModes(Set.of(AR, XLD)).build();
    Phenotype phenotype3 =
        Phenotype.builder().omimId("12345").inheritanceModes(Set.of(AR, AD)).build();
    OmimLine line1 =
        OmimLine.builder().gene("ENS1234567").phenotypes(Set.of(phenotype1, phenotype2)).build();
    OmimLine line2 = OmimLine.builder().gene("ENS1234568").phenotypes(Set.of(phenotype3)).build();
    CgdLine cgdLine = CgdLine.builder().gene("ENS1234569").inheritance("XL").build();
    Set<GeneInheritanceValue> expected = new HashSet<>();
    expected.add(
        GeneInheritanceValue.builder()
            .hpoInheritanceModes(
                Set.of(
                    HpoInheritanceMode.builder()
                        .hpoId("HP_0123")
                        .inheritanceModes(Set.of(XLD, AR))
                        .build(),
                    HpoInheritanceMode.builder()
                        .hpoId("HP_0124")
                        .inheritanceModes(Set.of(XLD))
                        .build()))
            .geneSymbol("ENS1234567")
            .inheritanceModes(Set.of(XLD, AR))
            .build());
    expected.add(
        GeneInheritanceValue.builder()
            .hpoInheritanceModes(
                Set.of(
                    HpoInheritanceMode.builder()
                        .hpoId("HP_012345")
                        .inheritanceModes(Set.of(AR, AD))
                        .build()))
            .geneSymbol("ENS1234568")
            .inheritanceModes(Set.of(AR, AD))
            .build());
    expected.add(
        GeneInheritanceValue.builder()
            .hpoInheritanceModes(emptySet())
            .geneSymbol("ENS1234569")
            .inheritanceModes(Set.of(XL))
            .build());

    assertEquals(
        expected,
        new HashSet<>(
            GenemapConverter.convertToGeneInheritanceValue(
                asList(line1, line2),
                Collections.singletonList(cgdLine),
                Map.of(
                    "123",
                    Set.of("HP_0124"),
                    "1234",
                    Set.of("HP_0123"),
                    "12345",
                    Set.of("HP_012345")))));
  }
}
