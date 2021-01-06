package org.molgenis.vcf.inheritance.genemapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.util.ResourceUtils;

class AppIT {

  @TempDir
  Path sharedTempDir;

  @Test
  void test() throws IOException {
    String omimFile = ResourceUtils.getFile("classpath:input_genemap.txt").toString();
    String cgdFile = ResourceUtils.getFile("classpath:input_cgd.txt.gz").toString();
    String outputFile = sharedTempDir.resolve("expected.tsv").toString();

    String[] args = {"-i", omimFile, "-c", cgdFile, "-o", outputFile, "-f"};
    SpringApplication.run(App.class, args);

    String actual = Files.readString(Path.of(outputFile));

    Path expectedOutputFile = ResourceUtils.getFile("classpath:expected.tsv").toPath();
    String expectedOutput = Files.readString(expectedOutputFile).replaceAll("\\R", "\n");

    assertEquals(expectedOutput, actual);
  }
}
