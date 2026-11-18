package com.grahambartley.nightborn.nemesis;

import com.grahambartley.nightborn.Nightborn;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

public final class NemesisLedger extends PersistentState {

  static final String ID = Nightborn.MOD_ID + "_nemeses";
  private static final PersistentState.Type<NemesisLedger> TYPE =
      new PersistentState.Type<>(NemesisLedger::new, NemesisLedger::fromNbt, null);

  static final String ENDED = "ended";
  static final String RELEASED = "released";

  private final Map<UUID, List<NemesisEntry>> held = new HashMap<>();
  private final Set<UUID> ended = new HashSet<>();
  private final Map<UUID, Integer> released = new HashMap<>();

  public static NemesisLedger of(final MinecraftServer server) {
    return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, ID);
  }

  public List<NemesisEntry> of(final UUID player) {
    return List.copyOf(held.getOrDefault(player, List.of()));
  }

  public Set<UUID> quarries() {
    return Set.copyOf(held.keySet());
  }

  public Optional<NemesisEntry> entry(final UUID player, final UUID mob) {
    return held.getOrDefault(player, List.of()).stream()
        .filter(entry -> entry.mob().equals(mob))
        .findFirst();
  }

  public boolean add(final UUID player, final NemesisEntry entry, final int limit) {
    final List<NemesisEntry> list = held.getOrDefault(player, List.of());
    if (list.size() >= limit || entry(player, entry.mob()).isPresent()) {
      return false;
    }
    held.computeIfAbsent(player, id -> new ArrayList<>()).add(entry);
    markDirty();
    return true;
  }

  public void put(final UUID player, final NemesisEntry entry) {
    final List<NemesisEntry> list = held.get(player);
    if (list == null) {
      return;
    }
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).mob().equals(entry.mob())) {
        if (!list.get(i).equals(entry)) {
          list.set(i, entry);
          markDirty();
        }
        return;
      }
    }
  }

  public Optional<NemesisEntry> remove(final UUID player, final UUID mob) {
    final List<NemesisEntry> list = held.get(player);
    if (list == null) {
      return Optional.empty();
    }
    final Optional<NemesisEntry> gone =
        list.stream().filter(entry -> entry.mob().equals(mob)).findFirst();
    gone.ifPresent(
        entry -> {
          list.remove(entry);
          if (list.isEmpty()) {
            held.remove(player);
          }
          markDirty();
        });
    return gone;
  }

  public Optional<NemesisEntry> end(final UUID player, final UUID mob) {
    final Optional<NemesisEntry> gone = remove(player, mob);
    gone.ifPresent(entry -> ended.add(mob));
    return gone;
  }

  public boolean ended(final UUID mob) {
    return ended.contains(mob);
  }

  public Optional<Integer> released(final UUID mob) {
    return Optional.ofNullable(released.get(mob));
  }

  public List<NemesisEntry> forget(final UUID player) {
    final List<NemesisEntry> gone = held.remove(player);
    if (gone == null) {
      return List.of();
    }
    gone.forEach(entry -> released.put(entry.mob(), entry.token()));
    markDirty();
    return List.copyOf(gone);
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    held.forEach(
        (player, entries) -> {
          final NbtList list = new NbtList();
          entries.forEach(
              entry ->
                  NemesisEntry.CODEC
                      .encodeStart(NbtOps.INSTANCE, entry)
                      .result()
                      .ifPresent(list::add));
          nbt.put(player.toString(), list);
        });
    final NbtList gone = new NbtList();
    ended.forEach(mob -> gone.add(NbtHelper.fromUuid(mob)));
    nbt.put(ENDED, gone);
    final NbtCompound kept = new NbtCompound();
    released.forEach((mob, token) -> kept.putInt(mob.toString(), token));
    nbt.put(RELEASED, kept);
    return nbt;
  }

  static NemesisLedger fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final NemesisLedger ledger = new NemesisLedger();
    for (final NbtElement mob : nbt.getList(ENDED, NbtElement.INT_ARRAY_TYPE)) {
      ledger.ended.add(NbtHelper.toUuid(mob));
    }
    final NbtCompound kept = nbt.getCompound(RELEASED);
    for (final String mob : kept.getKeys()) {
      try {
        ledger.released.put(UUID.fromString(mob), kept.getInt(mob));
      } catch (final IllegalArgumentException damaged) {
        Nightborn.LOGGER.warn("Skipping a damaged released nemesis {}", mob);
      }
    }
    for (final String key : nbt.getKeys()) {
      if (key.equals(ENDED) || key.equals(RELEASED)) {
        continue;
      }
      final UUID player;
      try {
        player = UUID.fromString(key);
      } catch (final IllegalArgumentException damaged) {
        Nightborn.LOGGER.warn("Skipping nemeses for a damaged player id {}", key);
        continue;
      }
      final List<NemesisEntry> entries = new ArrayList<>();
      for (final NbtElement element : nbt.getList(key, NbtElement.COMPOUND_TYPE)) {
        NemesisEntry.CODEC
            .parse(NbtOps.INSTANCE, element)
            .resultOrPartial(
                problem -> Nightborn.LOGGER.warn("Skipping a damaged nemesis: {}", problem))
            .ifPresent(entries::add);
      }
      if (!entries.isEmpty()) {
        ledger.held.put(player, entries);
      }
    }
    return ledger;
  }
}
