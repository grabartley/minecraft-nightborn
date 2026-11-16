package com.grahambartley.nightborn.mob.look;

import net.minecraft.nbt.NbtCompound;

public interface Figure {

  float FLAT = 1.0F;
  String BUST_KEY = "Bust";

  default float bust() {
    return FLAT;
  }

  static void write(final NbtCompound nbt, final float bust) {
    nbt.putFloat(BUST_KEY, bust);
  }

  static float read(final NbtCompound nbt, final VampireRig rig) {
    return rig.fitBust(nbt.contains(BUST_KEY) ? nbt.getFloat(BUST_KEY) : FLAT);
  }
}
