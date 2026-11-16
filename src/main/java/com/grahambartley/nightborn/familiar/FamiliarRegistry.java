package com.grahambartley.nightborn.familiar;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;

public final class FamiliarRegistry extends PersistentState {

  private static final String ID = "nightborn_familiars";
  private static final String BONDS = "bonds";
  private static final Codec<Map<UUID, FamiliarBond>> BONDS_CODEC =
      Codec.unboundedMap(Uuids.STRING_CODEC, FamiliarBond.CODEC);

  private static final PersistentState.Type<FamiliarRegistry> TYPE =
      new PersistentState.Type<>(FamiliarRegistry::new, FamiliarRegistry::fromNbt, null);

  private final Map<UUID, FamiliarBond> bonds = new HashMap<>();

  FamiliarRegistry() {}

  public static FamiliarRegistry of(final MinecraftServer server) {
    return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, ID);
  }

  public Optional<FamiliarBond> bondOf(final UUID owner) {
    return Optional.ofNullable(bonds.get(owner));
  }

  public boolean keeps(final UUID owner, final UUID familiar) {
    return bondOf(owner).filter(bond -> bond.familiar().equals(familiar)).isPresent();
  }

  public void bind(final UUID owner, final UUID familiar) {
    bonds.put(owner, new FamiliarBond(familiar, ChestMarks.NONE));
    markDirty();
  }

  public void update(final UUID owner, final FamiliarBond bond) {
    bonds.put(owner, bond);
    markDirty();
  }

  public void free(final UUID owner, final UUID familiar) {
    if (keeps(owner, familiar)) {
      bonds.remove(owner);
      markDirty();
    }
  }

  public boolean freeOwner(final UUID owner) {
    if (bonds.remove(owner) == null) {
      return false;
    }
    markDirty();
    return true;
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    BONDS_CODEC
        .encodeStart(NbtOps.INSTANCE, bonds)
        .result()
        .ifPresent(encoded -> nbt.put(BONDS, encoded));
    return nbt;
  }

  static FamiliarRegistry fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final FamiliarRegistry registry = new FamiliarRegistry();
    BONDS_CODEC.parse(NbtOps.INSTANCE, nbt.get(BONDS)).result().ifPresent(registry.bonds::putAll);
    return registry;
  }
}
