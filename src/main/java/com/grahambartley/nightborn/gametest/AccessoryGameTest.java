package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.accessory.AccessoryItems;
import com.grahambartley.nightborn.accessory.AccessoryMove;
import com.grahambartley.nightborn.accessory.AccessorySlot;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class AccessoryGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final BlockPos SPOT = new BlockPos(4, 2, 4);
  static final int WORN = 5;

  @GameTest(templateName = ARENA, tickLimit = WORN + 10)
  public void aCapeOnTheBackGivesItsBatwing(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final double before = human.getAttributeValue(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE);
    final ItemStack cape = new ItemStack(AccessoryItems.of("high_collared_cape"));
    cape.addEnchantment(
        context
            .getWorld()
            .getRegistryManager()
            .get(RegistryKeys.ENCHANTMENT)
            .getEntry(
                RegistryKey.of(
                    RegistryKeys.ENCHANTMENT, Identifier.of(Nightborn.MOD_ID, "batwing")))
            .orElseThrow(),
        3);
    slot(human, AccessorySlot.BACK).setStack(0, cape);
    context.runAtTick(
        WORN,
        () -> {
          context.assertEquals(
              before + 3.0,
              human.getAttributeValue(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE),
              "Batwing III on a cape worn on the back");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVeilLeftOnTheHeadMovesToTheFace(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    human.equipStack(EquipmentSlot.HEAD, new ItemStack(AccessoryItems.of("mourning_veil")));
    AccessoryMove.fromArmour(human);
    context.assertTrue(human.getEquippedStack(EquipmentSlot.HEAD).isEmpty(), "the head is free");
    context.assertTrue(
        slot(human, AccessorySlot.FACE).getStack(0).isOf(AccessoryItems.of("mourning_veil")),
        "the veil is now on the face");
    TestPlayers.finish(context, human);
  }

  private static TrinketInventory slot(final ServerPlayerEntity wearer, final AccessorySlot slot) {
    return TrinketsApi.getTrinketComponent(wearer)
        .orElseThrow()
        .getInventory()
        .get(slot.group())
        .get(slot.slotName());
  }
}
