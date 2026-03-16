package org.molgenis.vcf.inheritance.genemapper;

import java.util.LinkedHashSet;
import java.util.Set;
import org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CgdMapper {
  private static final Logger LOGGER = LoggerFactory.getLogger(CgdMapper.class);

  private CgdMapper(){}

  static Set<InheritanceMode> mapCgdInheritanceMode(String value) {
    Set<InheritanceMode> result = new LinkedHashSet<>();
    String[] split = value.split("/", -1);//support AD/AR
    for (String mode : split) {
        switch (mode.trim()) {
            case "XL" -> result.add(InheritanceMode.XL);
            case "AR" -> result.add(InheritanceMode.AR);
            case "AD" -> result.add(InheritanceMode.AD);
            case "YL" -> result.add(InheritanceMode.YL);
            default -> LOGGER.debug("Unsupported CGD inheritance value: '{}'", mode);
        }
    }
    return result;
  }
}
