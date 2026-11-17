package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HandLoanTest {

  static Stream<Arguments> held() {
    return Stream.of(
        Arguments.of(ItemStack.EMPTY, 0.085F),
        Arguments.of(new ItemStack(Items.STICK, 3), 0.5F),
        Arguments.of(new ItemStack(Items.ROSE_BUSH), 1.0F));
  }

  @ParameterizedTest(name = "{0} at drop chance {1}")
  @MethodSource("held")
  @DisplayName("what a vampire held before a pastime comes back, even after a reload")
  void keepsWhatWasHeld(final ItemStack held, final float chance) {
    final HandLoan loan = new HandLoan();
    loan.lend(held, chance);
    loan.lend(new ItemStack(Items.BOOK), 0.0F);
    final NbtCompound nbt = new NbtCompound();
    loan.write(nbt, DynamicRegistryManager.EMPTY);
    final HandLoan loaded = new HandLoan();
    loaded.read(nbt, DynamicRegistryManager.EMPTY);
    final HandLoan.Kept kept = loaded.take().orElseThrow();
    assertTrue(ItemStack.areEqual(held, kept.stack()));
    assertEquals(chance, kept.dropChance());
    assertTrue(loaded.take().isEmpty());
  }

  @ParameterizedTest(name = "lent {0}")
  @MethodSource("held")
  @DisplayName("a hand with nothing lent writes nothing and gives nothing back")
  void nothingLent(final ItemStack ignored, final float alsoIgnored) {
    final HandLoan loan = new HandLoan();
    final NbtCompound nbt = new NbtCompound();
    loan.write(nbt, DynamicRegistryManager.EMPTY);
    assertTrue(nbt.isEmpty());
    assertTrue(loan.take().isEmpty());
  }
}
