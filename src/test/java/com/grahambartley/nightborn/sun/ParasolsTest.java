package com.grahambartley.nightborn.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ParasolsTest {

  static Stream<Arguments> stacks() {
    final ItemStack open = new ItemStack(Parasols.PARASOL);
    open.set(Parasols.OPEN, true);
    final ItemStack shut = new ItemStack(Parasols.PARASOL);
    shut.set(Parasols.OPEN, false);
    final ItemStack flaggedStick = new ItemStack(Items.STICK);
    flaggedStick.set(Parasols.OPEN, true);
    return Stream.of(
        Arguments.of("an open parasol", open, true),
        Arguments.of("a closed parasol", shut, false),
        Arguments.of("a new parasol", new ItemStack(Parasols.PARASOL), false),
        Arguments.of("anything else", flaggedStick, false),
        Arguments.of("an empty hand", ItemStack.EMPTY, false));
  }

  @ParameterizedTest(name = "{0}: open {2}")
  @MethodSource("stacks")
  @DisplayName("only a parasol marked open counts as open")
  void isOpen(final String name, final ItemStack stack, final boolean open) {
    assertEquals(open, Parasols.isOpen(stack));
    assertEquals(0.0F, Parasols.openness(stack, null));
  }

  @ParameterizedTest(name = "{0}: open after folding false")
  @MethodSource("stacks")
  @DisplayName("a dropped parasol folds shut")
  void folds(final String name, final ItemStack stack, final boolean open) {
    final ItemStack dropped = stack.copy();
    Parasols.fold(dropped);
    assertFalse(Parasols.isOpen(dropped));
  }

  @Test
  @DisplayName("a parasol is held one at a time and lasts its full durability")
  void oneAtATime() {
    assertEquals(1, Parasols.PARASOL.getMaxCount());
    assertEquals(ParasolWear.DURABILITY, new ItemStack(Parasols.PARASOL).getMaxDamage());
    assertTrue(new ItemStack(Parasols.PARASOL).isDamageable());
    assertFalse(new ItemStack(Parasols.PARASOL).isStackable());
  }
}
