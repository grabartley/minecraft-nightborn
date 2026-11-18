package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.PersistentState;

public final class HouseholdRoll extends PersistentState {

  static final String ID = Nightborn.MOD_ID + "_households";
  static final String HOUSEHOLDS_KEY = "households";
  static final String HOME_KEY = "home";
  static final String RECORD_KEY = "record";

  private final HouseholdLedger ledger = new HouseholdLedger();

  public static HouseholdRoll get(final MinecraftServer server) {
    return server
        .getOverworld()
        .getPersistentStateManager()
        .getOrCreate(
            new PersistentState.Type<>(HouseholdRoll::new, HouseholdRoll::fromNbt, null), ID);
  }

  public void enlist(final GlobalPos home, final UUID vampire) {
    dirtyIf(ledger.enlist(home, vampire));
  }

  public void forget(final GlobalPos home, final UUID vampire) {
    dirtyIf(ledger.forget(home, vampire));
  }

  public void headSleepsAt(final GlobalPos home, final GlobalPos coffin) {
    dirtyIf(ledger.change(home, record -> record.headSleepsAt(coffin)));
  }

  public HouseholdRecord record(final GlobalPos home) {
    return ledger.record(home);
  }

  public Optional<GlobalPos> homeOfHeadCoffin(final GlobalPos coffin) {
    return ledger.homeOfHeadCoffin(coffin);
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final NbtList list = new NbtList();
    ledger
        .records()
        .forEach(
            (home, record) -> {
              final NbtCompound entry = new NbtCompound();
              GlobalPos.CODEC
                  .encodeStart(NbtOps.INSTANCE, home)
                  .ifSuccess(encoded -> entry.put(HOME_KEY, encoded));
              HouseholdRecord.CODEC
                  .encodeStart(NbtOps.INSTANCE, record)
                  .ifSuccess(encoded -> entry.put(RECORD_KEY, encoded));
              list.add(entry);
            });
    nbt.put(HOUSEHOLDS_KEY, list);
    return nbt;
  }

  public static HouseholdRoll fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final HouseholdRoll roll = new HouseholdRoll();
    for (final NbtElement element : nbt.getList(HOUSEHOLDS_KEY, NbtElement.COMPOUND_TYPE)) {
      final NbtCompound entry = (NbtCompound) element;
      final Optional<GlobalPos> home =
          GlobalPos.CODEC.parse(NbtOps.INSTANCE, entry.get(HOME_KEY)).result();
      final Optional<HouseholdRecord> record =
          HouseholdRecord.CODEC.parse(NbtOps.INSTANCE, entry.get(RECORD_KEY)).result();
      if (home.isEmpty() || record.isEmpty()) {
        Nightborn.LOGGER.warn("Skipping a damaged household entry: {}", entry);
        continue;
      }
      roll.ledger.change(home.get(), ignored -> record.get());
    }
    return roll;
  }

  private void dirtyIf(final boolean changed) {
    if (changed) {
      markDirty();
    }
  }
}
