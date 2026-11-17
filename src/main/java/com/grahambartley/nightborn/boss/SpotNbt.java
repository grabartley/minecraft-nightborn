package com.grahambartley.nightborn.boss;

import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Vec3d;

public final class SpotNbt {

  private SpotNbt() {}

  public static NbtList list(final Vec3d vec) {
    final NbtList list = new NbtList();
    list.add(NbtDouble.of(vec.x));
    list.add(NbtDouble.of(vec.y));
    list.add(NbtDouble.of(vec.z));
    return list;
  }

  public static Optional<Vec3d> vec(final NbtCompound nbt, final String key) {
    final NbtList list = nbt.getList(key, NbtElement.DOUBLE_TYPE);
    return list.size() == 3
        ? Optional.of(new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2)))
        : Optional.empty();
  }
}
