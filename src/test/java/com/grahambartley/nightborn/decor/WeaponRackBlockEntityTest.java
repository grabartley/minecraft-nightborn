package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WeaponRackBlockEntityTest {

  private static WeaponRackBlockEntity rack() {
    return new WeaponRackBlockEntity(BlockPos.ORIGIN, DecorBlocks.WEAPON_RACK.getDefaultState());
  }

  @ParameterizedTest(name = "hook {0}")
  @ValueSource(ints = {0, 1})
  void givesBackExactlyWhatWasHung(final int hook) {
    final WeaponRackBlockEntity rack = rack();
    final ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
    sword.setDamage(17);

    assertTrue(rack.hang(hook, sword.copy()));
    final ItemStack back = rack.take(hook);

    assertTrue(ItemStack.areEqual(sword, back));
    assertTrue(rack.get(hook).isEmpty());
    assertTrue(rack.held().isEmpty());
  }

  @ParameterizedTest(name = "hook {0}")
  @ValueSource(ints = {0, 1})
  void neverHangsTwoWeaponsOnOneHook(final int hook) {
    final WeaponRackBlockEntity rack = rack();
    rack.hang(hook, new ItemStack(Items.IRON_SWORD));

    assertFalse(rack.hang(hook, new ItemStack(Items.BOW)));
    assertEquals(Items.IRON_SWORD, rack.get(hook).getItem());
    assertEquals(1, rack.held().size());
  }

  @ParameterizedTest(name = "hook {0}")
  @ValueSource(ints = {0, 1})
  void anEmptyHookGivesNothing(final int hook) {
    assertTrue(rack().take(hook).isEmpty());
  }
}
