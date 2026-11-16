package com.grahambartley.nightborn.mob.look;

public interface Figure {

  float FLAT = 1.0F;

  default float bust() {
    return FLAT;
  }
}
