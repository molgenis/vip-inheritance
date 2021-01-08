package org.molgenis.vcf.inheritance.genemapper;

import java.util.HashSet;
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
    String[] split = value.split("/");//support AD/AR
    for (String mode : split) {
      switch (mode.trim()) {
        case "XL":
          result.add(InheritanceMode.XL);
          break;
        case "AR":
          result.add(InheritanceMode.AR);
          break;
        case "AD":
          result.add(InheritanceMode.AD);
          break;
        default:
          LOGGER.debug("Unsupported CGD inheritance value: '{}'", mode);
      }
    }
    return result;
  }
}
