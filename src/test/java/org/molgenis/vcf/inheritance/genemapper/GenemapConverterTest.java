package org.molgenis.vcf.inheritance.genemapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.AD;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.AR;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.DG;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.SM;
import static org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode.XD;

import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.genemapper.model.GeneInheritanceValue;
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
    Phenotype phenotype1 =
        Phenotype.builder().name("Blindness").inheritanceModes(Set.of(XD, DG, SM)).build();
    Phenotype phenotype2 =
        Phenotype.builder().name("Deafness").inheritanceModes(Set.of(AR, XD)).build();
    Phenotype phenotype3 =
        Phenotype.builder().name("Madness").inheritanceModes(Set.of(AR, AD)).build();
    OmimLine line1 =
        OmimLine.builder().gene("ENS1234567").phenotypes(Set.of(phenotype1, phenotype2)).build();
    OmimLine line2 = OmimLine.builder().gene("ENS1234568").phenotypes(Set.of(phenotype3)).build();

    List<GeneInheritanceValue> expected = new ArrayList<>();
    expected.add(
        GeneInheritanceValue.builder()
            .phenotypes(Set.of(phenotype1, phenotype2))
            .geneSymbol("ENS1234567")
            .inheritanceModes(Set.of(XD, DG, SM, AR))
            .build());
    expected.add(
        GeneInheritanceValue.builder()
            .phenotypes(Set.of(phenotype3))
            .geneSymbol("ENS1234568")
            .inheritanceModes(Set.of(AR, AD))
            .build());

    assertEquals(
        expected, GenemapConverter.convertToGeneInheritanceValue(Arrays.asList(line1, line2)));
  }
}
