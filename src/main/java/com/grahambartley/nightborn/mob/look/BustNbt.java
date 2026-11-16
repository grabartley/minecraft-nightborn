package com.grahambartley.nightborn.mob.look;

import net.minecraft.nbt.NbtCompound;

public final class BustNbt {

  static final String KEY = "Bust";

  private BustNbt() {}

  public static void write(final NbtCompound nbt, final float bust) {
    nbt.putFloat(KEY, bust);
  }

  public static float read(final NbtCompound nbt, final VampireRig rig) {
    return rig.fitBust(nbt.contains(KEY) ? nbt.getFloat(KEY) : rig.middleBust());
  }
}
