package org.molgenis.vcf.inheritance.genemapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.molgenis.vcf.inheritance.genemapper.App;
import org.springframework.boot.SpringApplication;
import org.springframework.util.ResourceUtils;

class AppIT {

  @TempDir
  Path sharedTempDir;

  @Test
  void test() throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:input_genemap.txt").toString();
    String outputFile = sharedTempDir.resolve("expected.tsv").toString();

    String[] args = {"-i", inputFile, "-o", outputFile};
    SpringApplication.run(App.class, args);

    String actual = Files.readString(Path.of(outputFile));

    Path expectedOutputFile = ResourceUtils.getFile("classpath:expected.tsv").toPath();
    String expectedOutput = Files.readString(expectedOutputFile).replaceAll("\\R", "\n");

    assertEquals(expectedOutput, actual);
  }
}
