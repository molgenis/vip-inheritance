package org.molgenis.vcf.inheritance.matcher;

import static org.molgenis.vcf.inheritance.matcher.SampleUtils.getFather;
import static org.molgenis.vcf.inheritance.matcher.SampleUtils.getMother;
import static org.molgenis.vcf.inheritance.matcher.SampleUtils.isDeNovo;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.CompoundResult;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceResult;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceSampleAlleleResult;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceSampleResult;
import org.molgenis.vcf.inheritance.matcher.model.Person;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.ped.PedReader;
import org.molgenis.vcf.inheritance.matcher.ped.PedToSamplesMapper;

public class InheritanceMatcher {

  // FIXME: spring boot app, but we already have one... separate repo? multimodule project
  public static void main(String[] args) {
    Path inputVcfPath = Path.of("src/test/resources/inheritance/input.vcf");
    Path inputPedPath = Path.of("src/test/resources/inheritance/pedigree_parents.ped");
    new InheritanceMatcher().run(inputVcfPath, inputPedPath);
  }

  public void run(Path inputVcfPath, Path inputPedPath) {
    Map<String, Sample> pedigree = null;
    try (VCFFileReader vcfFileReader = createReader(inputVcfPath)) {
      VCFHeader header = vcfFileReader.getFileHeader();
      ArrayList<String> vcfSamples = header.getSampleNamesInOrder();
      Map<String, Set<VariantContext>> variantsMap =
          VariantListUtils.getVariantsPerGene(vcfFileReader);
      try {
        PedReader pedReader = new PedReader(new FileReader(inputPedPath.toFile()));
        pedigree = parse(pedReader, vcfSamples);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      for (VariantContext vc : vcfFileReader) {
        System.out.println(matchInheritance(vc, pedigree, variantsMap, header));
      }
    }
  }

  private InheritanceResult matchInheritance(
      VariantContext vc,
      Map<String, Sample> pedigree,
      Map<String, Set<VariantContext>> variantsMap,
      VCFHeader header) {
    Map<Sample, InheritanceSampleResult> inheritanceSampleResultMap = new HashMap<>();
    for (Sample index : pedigree.values()) {
      if (index.getIndex() != -1) {
        Person indexPerson = index.getPerson();
        if (isAffected(indexPerson)) {
          Map<Allele, InheritanceSampleAlleleResult> inheritanceSampleAlleleResults =
              new HashMap<>();
          Sample mother = getMother(indexPerson, pedigree);
          Sample father = getFather(indexPerson, pedigree);
          for (Allele allele : vc.getAlternateAlleles()) {
            boolean isDenovo = isDeNovo(vc, allele, index, father, mother);
            CompoundResult compounds =
                compounds = getCompounds(vc, allele, variantsMap, index, father, mother, header);
            Set<InheritanceMode> inheritanceModes =
                matchInheritanceModes(vc, allele, index, mother, father);
            inheritanceSampleAlleleResults.put(
                allele,
                InheritanceSampleAlleleResult.builder()
                    .compounds(compounds)
                    .denovo(isDenovo)
                    .matchingModes(inheritanceModes)
                    .build());
          }
          // FIXME: nullchecks for parents
          inheritanceSampleResultMap.put(
              index,
              InheritanceSampleResult.builder()
                  .index(indexPerson)
                  .inheritanceSampleAlleleResults(inheritanceSampleAlleleResults)
                  .build());
        }
      }
    }
    return InheritanceResult.builder().inheritanceSampleResults(inheritanceSampleResultMap).build();
  }

  private CompoundResult getCompounds(
      VariantContext vc,
      Allele allele,
      Map<String, Set<VariantContext>> variantsMap,
      Sample sample,
      Sample father,
      Sample mother,
      VCFHeader header) {
    CompoundResult compoundResult = null;
    if (mother != null && father != null) {
      compoundResult =
          VariantListUtils.getCompound(vc, allele, variantsMap, sample, father, mother, header);
    } else {
      // TODO
    }
    return compoundResult;
  }

  private Set<InheritanceMode> matchInheritanceModes(
      VariantContext vc, Allele allele, Sample index, Sample mother, Sample father) {
    Set<InheritanceMode> modes = new HashSet<>();
    if (mother != null && father != null) {
      modes = matchInheritanceBothParents(vc, allele, index, father, mother);
    } else if (mother == null && father == null) {
      modes = matchInheritanceNoParent(vc, index);
    } else if (mother != null) {
      modes = matchInheritanceSingleParent(vc, index, mother);
    } else if (father != null) {
      modes = matchInheritanceSingleParent(vc, index, father);
    }
    return modes;
  }

  private Set<InheritanceMode> matchInheritanceNoParent(VariantContext vc, Sample index) {
    return InheritanceMatcherNoParent.getMatchingModes(vc, index);
  }

  private Set<InheritanceMode> matchInheritanceSingleParent(
      VariantContext vc, Sample index, Sample parent) {
    if (isAffected(parent.getPerson())) {
      return InheritanceMatcherSingleParentAffected.getMatchingModes(vc, index, parent);
    } else {
      return InheritanceMatcherSingleParentNotAffected.getMatchingModes(vc, index, parent);
    }
  }

  private boolean isAffected(Person person) {
    return person.getAffectedStatus() == AffectedStatus.AFFECTED;
  }

  private Set<InheritanceMode> matchInheritanceBothParents(
      VariantContext vc, Allele allele, Sample index, Sample father, Sample mother) {

    if (isAffected(mother.getPerson()) && isAffected(father.getPerson())) {
      // FIXME
      throw new UnsupportedOperationException("no branch for both parents affected!");
    } else if (isAffected(mother.getPerson()) || isAffected(father.getPerson())) {
      return InheritanceMatcherBothParentsOneAffected.getMatchingModes(
          vc, allele, index, father, mother);
    } else {
      return InheritanceMatcherBothParentsNotAffected.getMatchingModes(
          vc, allele, index, father, mother);
    }
  }

  static Map<String, Sample> parse(PedReader reader, ArrayList<String> vcfSamples) {
    final Map<String, Sample> pedigreePersons = new HashMap<>();
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(reader.iterator(), 0), false)
        .map(PedToSamplesMapper::map)
        .forEach(
            person ->
                pedigreePersons.put(
                    person.getIndividualId(),
                    new Sample(person, vcfSamples.indexOf(person.getIndividualId()))));
    return pedigreePersons;
  }

  private VCFFileReader createReader(Path vcfPath) {
    return new VCFFileReader(vcfPath, false);
  }
}
