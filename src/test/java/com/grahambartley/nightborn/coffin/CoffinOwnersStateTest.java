package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CoffinOwnersStateTest {

  private static final UUID ANNA = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID BRAM = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final GlobalPos CRYPT =
      GlobalPos.create(World.OVERWORLD, new BlockPos(10, 64, -3));
  private static final GlobalPos LAIR = GlobalPos.create(World.NETHER, new BlockPos(-7, 40, 2));

  @Test
  @DisplayName(
      "owners are saved as each owner's id against the dimension and position of the coffin head")
  void writesEachOwnerAgainstItsCoffin() {
    final CoffinOwnersState state = new CoffinOwnersState();
    state.assign(ANNA, CRYPT);
    final NbtCompound saved = state.writeNbt(new NbtCompound(), null);
    final NbtCompound entry =
        saved.getCompound(CoffinOwnersState.OWNERS_KEY).getCompound(ANNA.toString());
    assertEquals("minecraft:overworld", entry.getString("dimension"));
    assertEquals(new BlockPos(10, 64, -3), posOf(entry));
  }

  @Test
  @DisplayName("saving and loading keeps every owner and coffin, in every dimension")
  void keepsOwnersThroughSaveAndLoad() {
    final CoffinOwnersState state = new CoffinOwnersState();
    state.assign(ANNA, CRYPT);
    state.assign(BRAM, LAIR);
    final CoffinOwnersState loaded =
        CoffinOwnersState.fromNbt(state.writeNbt(new NbtCompound(), null), null);
    assertEquals(Optional.of(CRYPT), loaded.coffinOf(ANNA));
    assertEquals(Optional.of(BRAM), loaded.ownerOf(LAIR));
  }

  static Stream<Arguments> damage() {
    return Stream.of(
        Arguments.of("an owner id that is not a UUID", "not-a-uuid", NbtString.of("x"), false),
        Arguments.of("a coffin that is not a position", BRAM.toString(), NbtString.of("x"), false),
        Arguments.of("a coffin already owned by another", BRAM.toString(), null, false));
  }

  @ParameterizedTest(name = "{0} is skipped and the rest is kept")
  @MethodSource("damage")
  @DisplayName("a damaged entry costs only itself")
  void skipsDamagedEntries(
      final String name, final String key, final NbtString value, final boolean kept) {
    final CoffinOwnersState state = new CoffinOwnersState();
    state.assign(ANNA, CRYPT);
    final NbtCompound saved = state.writeNbt(new NbtCompound(), null);
    final NbtCompound owners = saved.getCompound(CoffinOwnersState.OWNERS_KEY);
    owners.put(key, value == null ? owners.getCompound(ANNA.toString()).copy() : value);
    final CoffinOwnersState loaded = CoffinOwnersState.fromNbt(saved, null);
    assertEquals(Optional.of(CRYPT), loaded.coffinOf(ANNA));
    assertEquals(kept, loaded.coffinOf(BRAM).isPresent());
  }

  @Test
  @DisplayName("only a real change marks the store for saving")
  void marksDirtyOnlyOnChange() {
    final CoffinOwnersState state = new CoffinOwnersState();
    state.assign(ANNA, CRYPT);
    state.setDirty(false);
    state.assign(BRAM, CRYPT);
    state.release(BRAM);
    state.releaseCoffin(LAIR);
    assertEquals(false, state.isDirty());
    state.releaseCoffin(CRYPT);
    assertEquals(true, state.isDirty());
  }

  private static BlockPos posOf(final NbtCompound entry) {
    final int[] pos = entry.getIntArray("pos");
    return new BlockPos(pos[0], pos[1], pos[2]);
  }
}
