package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.servant.ChestMarks;
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
  private static final Codec<Map<UUID, ChestMarks>> LEGACY_MARKS_CODEC =
      Codec.unboundedMap(
          Uuids.STRING_CODEC, ChestMarks.CODEC.optionalFieldOf("marks", ChestMarks.NONE).codec());

  private static final PersistentState.Type<FamiliarRegistry> TYPE =
      new PersistentState.Type<>(FamiliarRegistry::new, FamiliarRegistry::fromNbt, null);

  private final Map<UUID, FamiliarBond> bonds = new HashMap<>();
  private final Map<UUID, ChestMarks> legacyMarks = new HashMap<>();

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
    bonds.put(owner, new FamiliarBond(familiar));
    markDirty();
  }

  public Map<UUID, ChestMarks> takeLegacyMarks() {
    final Map<UUID, ChestMarks> taken = Map.copyOf(legacyMarks);
    if (!taken.isEmpty()) {
      legacyMarks.clear();
      markDirty();
    }
    return taken;
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
    LEGACY_MARKS_CODEC
        .parse(NbtOps.INSTANCE, nbt.get(BONDS))
        .result()
        .ifPresent(
            marks ->
                marks.forEach(
                    (owner, kept) -> {
                      if (!kept.equals(ChestMarks.NONE)) {
                        registry.legacyMarks.put(owner, kept);
                      }
                    }));
    return registry;
  }
}
