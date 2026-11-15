package com.grahambartley.nightborn.vomit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
