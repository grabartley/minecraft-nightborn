package com.grahambartley.nightborn.mob.look;

import java.util.Set;

public interface RigWearer {

  default Set<VampireRig> rigs() {
    return VampireRig.COMMON;
  }
}
