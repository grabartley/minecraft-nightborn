package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.Nightborn;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public final class Villages extends PersistentState {

  static final String ID = Nightborn.MOD_ID + "_villages";
  private static final PersistentState.Type<Villages> TYPE =
      new PersistentState.Type<>(Villages::new, Villages::fromNbt, null);

  private final Map<Long, VillageRecord> records = new HashMap<>();

  public static Villages of(final ServerWorld world) {
    return world.getPersistentStateManager().getOrCreate(TYPE, ID);
  }

  public Optional<VillageRecord> at(final BlockPos bell) {
    return Optional.ofNullable(records.get(bell.asLong()));
  }

  public void put(final BlockPos bell, final VillageRecord record) {
    final long key = bell.asLong();
    if (record.idle()) {
      if (records.remove(key) != null) {
        markDirty();
      }
      return;
    }
    if (!record.equals(records.put(key, record))) {
      markDirty();
    }
  }

  public Map<BlockPos, VillageRecord> all() {
    final Map<BlockPos, VillageRecord> all = new HashMap<>();
    records.forEach((key, record) -> all.put(BlockPos.fromLong(key), record));
    return all;
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    records.forEach(
        (key, record) ->
            VillageRecord.CODEC
                .encodeStart(NbtOps.INSTANCE, record)
                .ifSuccess(encoded -> nbt.put(Long.toString(key), encoded)));
    return nbt;
  }

  static Villages fromNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final Villages villages = new Villages();
    for (final String key : nbt.getKeys()) {
      VillageRecord.CODEC
          .parse(NbtOps.INSTANCE, nbt.get(key))
          .resultOrPartial(error -> Nightborn.LOGGER.warn("Skipping a damaged village: {}", error))
          .ifPresent(record -> villages.records.put(Long.parseLong(key), record));
    }
    return villages;
  }
}
