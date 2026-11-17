package com.grahambartley.nightborn.gramophone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.decor.DecorBlocks;
import java.util.stream.Stream;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GramophoneBlockEntityTest {

  private static GramophoneBlockEntity gramophone() {
    return new GramophoneBlockEntity(BlockPos.ORIGIN, DecorBlocks.GRAMOPHONE.getDefaultState());
  }

  static Stream<Arguments> items() {
    return Stream.concat(
        MusicDiscs.all().stream().map(disc -> Arguments.of(disc, true)),
        Stream.of(
            Arguments.of(Items.MUSIC_DISC_CAT, true),
            Arguments.of(Items.MUSIC_DISC_PIGSTEP, true),
            Arguments.of(Items.STICK, false),
            Arguments.of(Items.JUKEBOX, false)));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @MethodSource("items")
  @DisplayName("an empty gramophone takes any music disc, vanilla or its own, and nothing else")
  void takesOnlyDiscs(final Item item, final boolean taken) {
    assertEquals(taken, gramophone().isValid(0, new ItemStack(item)));
  }

  @Test
  @DisplayName("a loaded gramophone takes no second disc and holds one at most")
  void holdsOneDisc() {
    final GramophoneBlockEntity gramophone = gramophone();
    gramophone.setStack(new ItemStack(Items.MUSIC_DISC_CAT));
    assertFalse(gramophone.isValid(0, new ItemStack(Items.MUSIC_DISC_PIGSTEP)));
    assertEquals(1, gramophone.getMaxCountPerStack());
    assertEquals(Items.MUSIC_DISC_CAT, gramophone.getStack().getItem());
  }

  @Test
  @DisplayName("a gramophone is ready to start only while it holds a disc that is not playing")
  void readyWithADisc() {
    final GramophoneBlockEntity gramophone = gramophone();
    assertFalse(gramophone.ready());
    gramophone.setStack(new ItemStack(Items.MUSIC_DISC_CAT));
    assertTrue(gramophone.ready());
    assertFalse(gramophone.playing());
  }

  @Test
  @DisplayName("taking the disc out gives back exactly that disc and leaves the gramophone empty")
  void givesItsDiscBack() {
    final GramophoneBlockEntity gramophone = gramophone();
    final ItemStack disc = new ItemStack(MusicDiscs.all().getFirst());
    gramophone.setStack(disc.copy());
    assertTrue(ItemStack.areEqual(disc, gramophone.decreaseStack(1)));
    assertTrue(gramophone.getStack().isEmpty());
    assertFalse(gramophone.ready());
    assertFalse(gramophone.ambient());
  }

  @Test
  @DisplayName("outside a world a gramophone starts nothing and signals nothing")
  void needsAWorldToPlay() {
    final GramophoneBlockEntity gramophone = gramophone();
    gramophone.setStack(new ItemStack(Items.MUSIC_DISC_CAT));
    assertFalse(gramophone.play());
    assertEquals(0, gramophone.comparatorOutput());
  }

  @ParameterizedTest(name = "hopper with room: {0}")
  @MethodSource("hoppers")
  @DisplayName("a hopper pulls the disc out only when it has room for it")
  void hoppersPullWhenTheyHaveRoom(final boolean room) {
    final SimpleInventory hopper = new SimpleInventory(1);
    if (!room) {
      hopper.setStack(0, new ItemStack(Items.STICK));
    }
    assertEquals(room, gramophone().canTransferTo(hopper, 0, new ItemStack(Items.MUSIC_DISC_CAT)));
  }

  static Stream<Boolean> hoppers() {
    return Stream.of(true, false);
  }
}
