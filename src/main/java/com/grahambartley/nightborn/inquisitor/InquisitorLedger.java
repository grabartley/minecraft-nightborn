package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.Nightborn;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

public final class InquisitorLedger extends PersistentState {

  public enum Owed {
    END_VISIT,
    COME_BACK,
    CLEAR_NOTORIETY,
    PAY,
    ENDED
  }

  static final String ID = Nightborn.MOD_ID + "_inquisitor_ledger";
  private static final PersistentState.Type<InquisitorLedger> TYPE =
      new PersistentState.Type<>(InquisitorLedger::new, InquisitorLedger::fromNbt, null);

  private final Map<UUID, List<Owed>> owed = new HashMap<>();

  public static InquisitorLedger of(final MinecraftServer server) {
    return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, ID);
  }

  public void owe(final UUID player, final List<Owed> what) {
    if (what.isEmpty()) {
      return;
    }
    owed.computeIfAbsent(player, id -> new ArrayList<>()).addAll(what);
    markDirty();
  }

  public List<Owed> settle(final UUID player) {
    final List<Owed> due = owed.remove(player);
    if (due == null) {
      return List.of();
    }
    markDirty();
    return List.copyOf(due);
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    owed.forEach(
        (player, due) -> {
          final NbtList list = new NbtList();
          due.forEach(entry -> list.add(NbtString.of(entry.name())));
          nbt.put(player.toString(), list);
        });
    return nbt;
  }

  static InquisitorLedger fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final InquisitorLedger ledger = new InquisitorLedger();
    for (final String key : nbt.getKeys()) {
      final UUID player;
      try {
        player = UUID.fromString(key);
      } catch (final IllegalArgumentException damaged) {
        Nightborn.LOGGER.warn("Skipping debts for a damaged player id {}", key);
        continue;
      }
      final List<Owed> due = new ArrayList<>();
      for (final NbtElement element : nbt.getList(key, NbtElement.STRING_TYPE)) {
        try {
          due.add(Owed.valueOf(element.asString()));
        } catch (final IllegalArgumentException unknown) {
          Nightborn.LOGGER.warn("Skipping an unknown debt {} for {}", element.asString(), key);
        }
      }
      ledger.owed.put(player, due);
    }
    return ledger;
  }
}
