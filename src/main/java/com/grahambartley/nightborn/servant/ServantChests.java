package com.grahambartley.nightborn.servant;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.PersistentState;

public final class ServantChests extends PersistentState {

  private static final String ID = "nightborn_servant_chests";
  private static final String MARKS = "marks";
  private static final Codec<Map<UUID, ChestMarks>> MARKS_CODEC =
      Codec.unboundedMap(Uuids.STRING_CODEC, ChestMarks.CODEC);

  private static final PersistentState.Type<ServantChests> TYPE =
      new PersistentState.Type<>(ServantChests::new, ServantChests::fromNbt, null);

  private final Map<UUID, ChestMarks> marks = new HashMap<>();

  ServantChests() {}

  public static ServantChests of(final MinecraftServer server) {
    return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, ID);
  }

  public ChestMarks marksOf(final UUID owner) {
    return marks.getOrDefault(owner, ChestMarks.NONE);
  }

  public ChestMark mark(final UUID owner, final GlobalPos chest) {
    final ChestMarks.Marked marked = marksOf(owner).mark(chest);
    put(owner, marked.marks());
    return marked.mark();
  }

  public void forget(final UUID owner, final GlobalPos chest) {
    put(owner, marksOf(owner).without(chest));
  }

  public void adopt(final Map<UUID, ChestMarks> older) {
    older.forEach(
        (owner, kept) -> {
          if (marksOf(owner).equals(ChestMarks.NONE)) {
            put(owner, kept);
          }
        });
  }

  private void put(final UUID owner, final ChestMarks updated) {
    if (updated.equals(ChestMarks.NONE)) {
      marks.remove(owner);
    } else {
      marks.put(owner, updated);
    }
    markDirty();
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    MARKS_CODEC
        .encodeStart(NbtOps.INSTANCE, marks)
        .result()
        .ifPresent(encoded -> nbt.put(MARKS, encoded));
    return nbt;
  }

  static ServantChests fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final ServantChests chests = new ServantChests();
    MARKS_CODEC.parse(NbtOps.INSTANCE, nbt.get(MARKS)).result().ifPresent(chests.marks::putAll);
    return chests;
  }
}
