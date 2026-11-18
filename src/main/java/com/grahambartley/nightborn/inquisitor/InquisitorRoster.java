package com.grahambartley.nightborn.inquisitor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;

public final class InquisitorRoster {

  static final String FOES_KEY = "Foes";
  static final String FALLEN_KEY = "Fallen";

  private final Set<UUID> foes = new LinkedHashSet<>();
  private final Set<UUID> fallen = new LinkedHashSet<>();

  public void meet(final UUID foe) {
    foes.add(foe);
  }

  public boolean fell(final UUID foe) {
    return foes.contains(foe) && fallen.add(foe);
  }

  public boolean met(final UUID foe) {
    return foes.contains(foe);
  }

  public int standing() {
    return (int) foes.stream().filter(foe -> !fallen.contains(foe)).count();
  }

  public void write(final NbtCompound nbt) {
    nbt.put(FOES_KEY, list(foes));
    nbt.put(FALLEN_KEY, list(fallen));
  }

  public void read(final NbtCompound nbt) {
    foes.clear();
    fallen.clear();
    nbt.getList(FOES_KEY, NbtElement.INT_ARRAY_TYPE)
        .forEach(element -> foes.add(NbtHelper.toUuid(element)));
    nbt.getList(FALLEN_KEY, NbtElement.INT_ARRAY_TYPE)
        .forEach(element -> fallen.add(NbtHelper.toUuid(element)));
  }

  private static NbtList list(final Set<UUID> ids) {
    final NbtList list = new NbtList();
    ids.forEach(id -> list.add(NbtHelper.fromUuid(id)));
    return list;
  }
}
