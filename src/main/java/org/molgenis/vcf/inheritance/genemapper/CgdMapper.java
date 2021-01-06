package org.molgenis.vcf.inheritance.genemapper;

import org.molgenis.vcf.inheritance.genemapper.model.InheritanceMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CgdMapper {
  private static final Logger LOGGER = LoggerFactory.getLogger(CgdMapper.class);

  private CgdMapper(){}

  static InheritanceMode mapCgdInheritanceMode(String value) {
    InheritanceMode result = null;
    switch (value.trim()) {
      case "XL":
        result = InheritanceMode.XL;
        break;
      case "AR":
        result = InheritanceMode.AR;
        break;
      case "AD":
        result = InheritanceMode.AD;
        break;
      default:
        LOGGER.debug("Unsupported CGD inheritance value: '{}'", value);
    }
    return result;
  }
}
