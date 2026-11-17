package com.grahambartley.nightborn.hunter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;

public final class FightRoll {

  public static final int MOST = 64;
  static final String KEY = "TookPart";

  private final Set<UUID> fighters = new LinkedHashSet<>();

  public void join(final UUID fighter) {
    if (fighters.size() < MOST) {
      fighters.add(fighter);
    }
  }

  public List<UUID> fighters() {
    return List.copyOf(fighters);
  }

  public static List<UUID> sharers(final Optional<UUID> finisher, final Collection<UUID> tookPart) {
    final Set<UUID> sharers = new LinkedHashSet<>(tookPart);
    finisher.ifPresent(sharers::add);
    return List.copyOf(sharers);
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
