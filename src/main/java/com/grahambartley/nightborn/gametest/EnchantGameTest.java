package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodAttachment;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class EnchantGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void bloodlettingGivesAVampireBlood(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final PigEntity pig = context.spawnEntity(EntityType.PIG, new BlockPos(2, 2, 2));
    pig.setAiDisabled(true);
    final ItemStack sword = new ItemStack(Items.IRON_SWORD);
    sword.addEnchantment(
        context
            .getWorld()
            .getRegistryManager()
            .get(RegistryKeys.ENCHANTMENT)
            .getEntry(
                RegistryKey.of(
                    RegistryKeys.ENCHANTMENT, Identifier.of(Nightborn.MOD_ID, "bloodletting")))
            .orElseThrow(),
        3);
    vampire.setStackInHand(Hand.MAIN_HAND, sword);
    BloodAttachment.set(vampire, 10);
    for (int blow = 0; blow < 2; blow++) {
      EnchantmentHelper.onTargetDamaged(
          context.getWorld(), pig, context.getWorld().getDamageSources().playerAttack(vampire));
    }
    context.assertEquals(11, BloodAttachment.get(vampire), "two blows at III give one blood");
    TestPlayers.finish(context, vampire);
  }
}
