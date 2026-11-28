package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.DismembermentSettings;
import com.grahambartley.nightborn.dismember.BodyPart;
import com.grahambartley.nightborn.dismember.Dismemberment;
import com.grahambartley.nightborn.dismember.Wounds;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.Arrays;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class DismembermentGameTest implements FabricGameTest {

  static final String SURE_CUT_BATCH = "dismemberment-sure-cut";
  static final double ARM_OFFSET = 0.25;
  static final double ARM_HEIGHT_SHARE = 0.56;

  private static int chanceBefore;

  @BeforeBatch(batchId = SURE_CUT_BATCH)
  public void everyOpenBlowCuts(final ServerWorld world) {
    chanceBefore = ServerConfigService.current().dismemberment().severChance();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(DismembermentSettings.SEVER_CHANCE, 100));
  }

  @AfterBatch(batchId = SURE_CUT_BATCH)
  public void restoreChance(final ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(DismembermentSettings.SEVER_CHANCE, chanceBefore));
  }

  @GameTest(templateName = GoreGameTest.ARENA, batchId = SURE_CUT_BATCH)
  public void twoPlayersStrikingInOneTickTakeOnePartAtMost(final TestContext context) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(3, 2, 3));
    hunter.setAiDisabled(true);
    for (final EquipmentSlot slot : EquipmentSlot.values()) {
      hunter.equipStack(slot, ItemStack.EMPTY);
    }
    hunter.setYaw(0.0F);
    hunter.setBodyYaw(0.0F);
    hunter.setHealth(hunter.getMaxHealth() * 0.55F);
    final ServerPlayerEntity right = swordAt(context, hunter, -ARM_OFFSET);
    final ServerPlayerEntity left = swordAt(context, hunter, ARM_OFFSET);
    final float blow = hunter.getMaxHealth() * 0.25F;
    hunter.damage(context.getWorld().getDamageSources().playerAttack(right), blow);
    hunter.timeUntilRegen = 0;
    hunter.damage(context.getWorld().getDamageSources().playerAttack(left), blow);
    final long lost =
        Arrays.stream(BodyPart.values())
            .filter(part -> Dismemberment.missing(hunter, part))
            .count();
    context.assertTrue(hunter.isAlive(), "the hunter lives through both blows");
    context.assertEquals(1L, lost, "parts lost to two blows in one tick");
    context.assertTrue(
        Dismemberment.missing(hunter, BodyPart.RIGHT_ARM),
        "the first blow takes the arm it was aimed at");
    context.complete();
  }

  @GameTest(templateName = GoreGameTest.ARENA, tickLimit = 180)
  public void aHumanBleedsToOneHeartAndAWoundCanBeBound(final TestContext context) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(3, 2, 3));
    hunter.setAiDisabled(true);
    context.waitAndRun(
        5L,
        () -> {
          hunter.setHealth(3.0F);
          Dismemberment.sever(hunter, BodyPart.LEFT_ARM);
          context.assertTrue(Wounds.bleeding(hunter), "a fresh cut bleeds");
        });
    context.waitAndRun(
        145L,
        () -> {
          context.assertEquals(hunter.getHealth(), 2.0F, "health after bleeding a while");
          context.assertTrue(Wounds.bleeding(hunter), "the wound is still open");
          Wounds.close(hunter);
          context.assertFalse(Wounds.bleeding(hunter), "a bound wound stops bleeding");
          context.assertTrue(
              Dismemberment.missing(hunter, BodyPart.LEFT_ARM), "the arm is still gone");
          context.complete();
        });
  }

  private static ServerPlayerEntity swordAt(
      final TestContext context, final HunterEntity hunter, final double side) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
    player.refreshPositionAndAngles(
        hunter.getX() + side, hunter.getY(), hunter.getZ() + 2.0, 180.0F, 0.0F);
    player.lookAt(
        EntityAnchorArgumentType.EntityAnchor.EYES,
        new Vec3d(
            hunter.getX() + side,
            hunter.getY() + hunter.getHeight() * ARM_HEIGHT_SHARE,
            hunter.getZ()));
    return player;
  }
}
