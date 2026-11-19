package com.grahambartley.nightborn.guild.raiding;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;

public final class RaidingParties extends PersistentState {

  static final String ID = Nightborn.MOD_ID + "_raiding_parties";
  static final String RAIDS_KEY = "raids";
  static final String ENDED_KEY = "ended";
  static final Codec<Map<UUID, Long>> ENDED_CODEC =
      Codec.unboundedMap(Uuids.STRING_CODEC, Codec.LONG);

  private final Map<UUID, RaidingParty> raids = new HashMap<>();
  private final Map<UUID, Long> ended = new HashMap<>();

  public static RaidingParties get(final MinecraftServer server) {
    return server
        .getOverworld()
        .getPersistentStateManager()
        .getOrCreate(
            new PersistentState.Type<>(RaidingParties::new, RaidingParties::fromNbt, null), ID);
  }

  public Optional<RaidingParty> against(final UUID quarry) {
    return Optional.ofNullable(raids.get(quarry));
  }

  public Optional<RaidingParty> ledBy(final UUID leader) {
    return raids.values().stream()
        .filter(raid -> raid.leader().filter(leader::equals).isPresent())
        .findFirst();
  }

  public List<RaidingParty> all() {
    return List.copyOf(raids.values());
  }

  public void start(final RaidingParty raid) {
    raids.put(raid.quarry(), raid);
    markDirty();
  }

  public void put(final RaidingParty raid) {
    if (raids.containsKey(raid.quarry())) {
      raids.put(raid.quarry(), raid);
      markDirty();
    }
  }

  public Optional<RaidingParty> remove(final UUID quarry) {
    final RaidingParty removed = raids.remove(quarry);
    if (removed != null) {
      markDirty();
    }
    return Optional.ofNullable(removed);
  }

  public void endedFor(final UUID quarry, final long night) {
    ended.merge(quarry, night, Math::max);
    markDirty();
  }

  public Optional<Long> takeEnded(final UUID quarry) {
    final Long night = ended.remove(quarry);
    if (night != null) {
      markDirty();
    }
    return Optional.ofNullable(night);
  }

  public boolean hasEnded(final UUID quarry) {
    return ended.containsKey(quarry);
  }

  public void forget(final UUID quarry) {
    if (ended.remove(quarry) != null) {
      markDirty();
    }
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final NbtList list = new NbtList();
    raids
        .values()
        .forEach(
            raid -> RaidingParty.CODEC.encodeStart(NbtOps.INSTANCE, raid).ifSuccess(list::add));
    nbt.put(RAIDS_KEY, list);
    ENDED_CODEC.encodeStart(NbtOps.INSTANCE, ended).ifSuccess(value -> nbt.put(ENDED_KEY, value));
    return nbt;
  }

  public static RaidingParties fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final RaidingParties parties = new RaidingParties();
    for (final NbtElement entry : nbt.getList(RAIDS_KEY, NbtElement.COMPOUND_TYPE)) {
      RaidingParty.CODEC
          .parse(NbtOps.INSTANCE, entry)
          .resultOrPartial(error -> Nightborn.LOGGER.warn("Skipping a damaged raid: {}", error))
          .ifPresent(raid -> parties.raids.put(raid.quarry(), raid));
    }
    if (nbt.contains(ENDED_KEY)) {
      ENDED_CODEC
          .parse(NbtOps.INSTANCE, nbt.get(ENDED_KEY))
          .resultOrPartial(error -> Nightborn.LOGGER.warn("Skipping damaged raid ends: {}", error))
          .ifPresent(parties.ended::putAll);
    }
    return parties;
  }
}
