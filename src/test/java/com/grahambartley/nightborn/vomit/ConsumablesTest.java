package com.grahambartley.nightborn.vomit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.UseAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ConsumablesTest {

  static Stream<Arguments> swallowed() {
    return Stream.of(
        Arguments.of(Items.BREAD, true),
        Arguments.of(Items.GOLDEN_APPLE, true),
        Arguments.of(Items.MUSHROOM_STEW, true),
        Arguments.of(Items.HONEY_BOTTLE, true),
        Arguments.of(Items.POTION, true),
        Arguments.of(Items.MILK_BUCKET, true),
        Arguments.of(Items.OMINOUS_BOTTLE, true),
        Arguments.of(Items.STICK, false),
        Arguments.of(Items.BOW, false));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @MethodSource("swallowed")
  @DisplayName("a vampire rejects anything eaten or drunk, and nothing else")
  void vampireRejects(final Item item, final boolean rejected) {
    assertEquals(rejected, Consumables.rejectedBy(new ItemStack(item), true));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("swallowed")
  @DisplayName("a human keeps everything down")
  void humanKeepsItDown(final Item item, final boolean ignored) {
    assertEquals(false, Consumables.rejectedBy(new ItemStack(item), false));
  }

  static Stream<Arguments> tagged() {
    return Stream.of(
        Arguments.of(true, false, false, true),
        Arguments.of(false, true, false, true),
        Arguments.of(true, true, false, true),
        Arguments.of(true, false, true, false),
        Arguments.of(false, true, true, false),
        Arguments.of(false, false, false, false),
        Arguments.of(false, false, true, false));
  }

  @ParameterizedTest(name = "swallowed {0}, reject tag {1}, consumable tag {2}: {3}")
  @MethodSource("tagged")
  @DisplayName("the reject tag adds items, and the consumables tag always wins")
  void tagsDecide(
      final boolean swallowed,
      final boolean rejectTagged,
      final boolean consumableTagged,
      final boolean rejected) {
    assertEquals(rejected, Consumables.rejects(swallowed, rejectTagged, consumableTagged));
  }

  static Stream<Arguments> useActions() {
    return Stream.of(
        Arguments.of(UseAction.EAT, true),
        Arguments.of(UseAction.DRINK, true),
        Arguments.of(UseAction.NONE, false),
        Arguments.of(UseAction.BLOCK, false),
        Arguments.of(UseAction.BOW, false),
        Arguments.of(UseAction.SPEAR, false),
        Arguments.of(UseAction.CROSSBOW, false),
        Arguments.of(UseAction.SPYGLASS, false),
        Arguments.of(UseAction.TOOT_HORN, false),
        Arguments.of(UseAction.BRUSH, false));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @MethodSource("useActions")
  @DisplayName("only eating and drinking count as swallowing")
  void swallowing(final UseAction action, final boolean swallowed) {
    assertEquals(swallowed, Consumables.swallowed(action));
  }

  static Stream<Arguments> leftBehind() {
    return Stream.of(
        Arguments.of(Items.BREAD, Items.AIR),
        Arguments.of(Items.MUSHROOM_STEW, Items.BOWL),
        Arguments.of(Items.SUSPICIOUS_STEW, Items.BOWL),
        Arguments.of(Items.HONEY_BOTTLE, Items.GLASS_BOTTLE),
        Arguments.of(Items.POTION, Items.GLASS_BOTTLE),
        Arguments.of(Items.MILK_BUCKET, Items.BUCKET),
        Arguments.of(Items.OMINOUS_BOTTLE, Items.AIR));
  }

  @ParameterizedTest(name = "{0} leaves {1}")
  @MethodSource("leftBehind")
  @DisplayName("what is brought up leaves its bowl, bottle or bucket, as eating it would")
  void leavesTheContainer(final Item item, final Item container) {
    final ItemStack left = Consumables.leftBehind(new ItemStack(item));
    assertEquals(container, left.isEmpty() ? Items.AIR : left.getItem());
    assertTrue(left.getCount() <= 1);
  }
}
