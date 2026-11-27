package com.grahambartley.nightborn.guild.party;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public final class PitchedCamps extends PersistentState {

  public record Pitched(HunterParty.Camp camp, CampStay stay) {
    public static final Codec<Pitched> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        HunterParty.Camp.CODEC.fieldOf("camp").forGetter(Pitched::camp),
                        CampStay.CODEC.fieldOf("stay").forGetter(Pitched::stay))
                    .apply(instance, Pitched::new));
  }

  static final String ID = Nightborn.MOD_ID + "_pitched_camps";
  private static final PersistentState.Type<PitchedCamps> TYPE =
      new PersistentState.Type<>(PitchedCamps::new, PitchedCamps::fromNbt, null);

  private final Map<UUID, Pitched> camps = new HashMap<>();

  public static PitchedCamps of(final ServerWorld world) {
    return world.getPersistentStateManager().getOrCreate(TYPE, ID);
  }

  public void pitched(final UUID leader, final HunterParty.Camp camp, final CampStay stay) {
    camps.put(leader, new Pitched(camp, stay));
    markDirty();
  }

  public void struck(final UUID leader) {
    if (camps.remove(leader) != null) {
      markDirty();
    }
  }

  public Map<UUID, Pitched> all() {
    return Map.copyOf(camps);
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    camps.forEach(
        (leader, pitched) ->
            Pitched.CODEC
                .encodeStart(NbtOps.INSTANCE, pitched)
                .ifSuccess(encoded -> nbt.put(leader.toString(), encoded)));
    return nbt;
  }

  static PitchedCamps fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final PitchedCamps camps = new PitchedCamps();
    for (final String key : nbt.getKeys()) {
      Pitched.CODEC
          .parse(NbtOps.INSTANCE, nbt.get(key))
          .resultOrPartial(error -> Nightborn.LOGGER.warn("Skipping a damaged camp: {}", error))
          .ifPresent(pitched -> camps.camps.put(UUID.fromString(key), pitched));
    }
    return camps;
  }
}
