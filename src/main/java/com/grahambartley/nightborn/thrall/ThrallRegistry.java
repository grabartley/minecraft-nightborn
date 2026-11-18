package com.grahambartley.nightborn.thrall;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;

public final class ThrallRegistry extends PersistentState {

  private static final String ID = "nightborn_thralls";
  private static final String OWNERS = "owners";
  private static final Codec<Map<UUID, List<UUID>>> OWNERS_CODEC =
      Codec.unboundedMap(Uuids.STRING_CODEC, Uuids.STRING_CODEC.listOf());

  private static final PersistentState.Type<ThrallRegistry> TYPE =
      new PersistentState.Type<>(ThrallRegistry::new, ThrallRegistry::fromNbt, null);

  private final Map<UUID, Set<UUID>> owners = new HashMap<>();

  ThrallRegistry() {}

  public static ThrallRegistry of(final MinecraftServer server) {
    return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, ID);
  }

  public int count(final UUID owner) {
    return owners.getOrDefault(owner, Set.of()).size();
  }

  public Set<UUID> thralls(final UUID owner) {
    return Set.copyOf(owners.getOrDefault(owner, Set.of()));
  }

  public boolean keeps(final UUID owner, final UUID thrall) {
    return owners.getOrDefault(owner, Set.of()).contains(thrall);
  }

  public void bind(final UUID owner, final UUID thrall) {
    owners.computeIfAbsent(owner, key -> new HashSet<>()).add(thrall);
    markDirty();
  }

  public void free(final UUID owner, final UUID thrall) {
    final Set<UUID> thralls = owners.get(owner);
    if (thralls != null && thralls.remove(thrall)) {
      if (thralls.isEmpty()) {
        owners.remove(owner);
      }
      markDirty();
    }
  }

  public int freeOwner(final UUID owner) {
    final Set<UUID> thralls = owners.remove(owner);
    if (thralls == null) {
      return 0;
    }
    markDirty();
    return thralls.size();
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final Map<UUID, List<UUID>> saved = new HashMap<>();
    owners.forEach((owner, thralls) -> saved.put(owner, List.copyOf(thralls)));
    OWNERS_CODEC
        .encodeStart(NbtOps.INSTANCE, saved)
        .result()
        .ifPresent(encoded -> nbt.put(OWNERS, encoded));
    return nbt;
  }

  static ThrallRegistry fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final ThrallRegistry registry = new ThrallRegistry();
    OWNERS_CODEC
        .parse(NbtOps.INSTANCE, nbt.get(OWNERS))
        .result()
        .ifPresent(
            saved ->
                saved.forEach(
                    (owner, thralls) -> registry.owners.put(owner, new HashSet<>(thralls))));
    return registry;
  }
}
