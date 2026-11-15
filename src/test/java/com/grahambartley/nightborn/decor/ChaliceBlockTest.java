package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodQuality;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ChaliceBlockTest {

  static Stream<Arguments> heldItems() {
    return Stream.of(
        Arguments.of(ItemStack.EMPTY, ChalicePour.Held.EMPTY_HAND),
        Arguments.of(BloodBottleItem.of(BloodQuality.PLAYER), ChalicePour.Held.BLOOD_BOTTLE),
        Arguments.of(BloodBottleItem.of(BloodQuality.ANIMAL), ChalicePour.Held.BLOOD_BOTTLE),
        Arguments.of(new ItemStack(Items.GLASS_BOTTLE), ChalicePour.Held.GLASS_BOTTLE),
        Arguments.of(new ItemStack(Items.POTION), ChalicePour.Held.OTHER),
        Arguments.of(new ItemStack(Items.GOLD_INGOT), ChalicePour.Held.OTHER));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("heldItems")
  void sortsWhatThePlayerHolds(final ItemStack stack, final ChalicePour.Held held) {
    assertEquals(held, ChaliceBlock.held(stack));
  }

  @Test
  void isPlacedEmpty() {
    assertEquals(ChaliceBlood.EMPTY, DecorBlocks.CHALICE.getDefaultState().get(ChaliceBlock.BLOOD));
  }
}
