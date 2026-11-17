package com.grahambartley.nightborn.eldest;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;

public final class FightRoll {

  static final String KEY = "Fighters";

  private final Set<UUID> fighters = new LinkedHashSet<>();

  public boolean join(final UUID fighter) {
    return fighters.add(fighter);
  }

  public void clear() {
    fighters.clear();
  }

  public boolean has(final UUID fighter) {
    return fighters.contains(fighter);
  }

  public Set<UUID> fighters() {
    return Set.copyOf(fighters);
  }

  public void write(final NbtCompound nbt) {
    final NbtList list = new NbtList();
    fighters.forEach(fighter -> list.add(NbtHelper.fromUuid(fighter)));
    nbt.put(KEY, list);
  }

  public void read(final NbtCompound nbt) {
    fighters.clear();
    for (final NbtElement element : nbt.getList(KEY, NbtElement.INT_ARRAY_TYPE)) {
      join(NbtHelper.toUuid(element));
    }
  }
}
