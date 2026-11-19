package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerCooldowns;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.trap.Baits;
import com.grahambartley.nightborn.trap.Grounding;
import com.grahambartley.nightborn.trap.KillingGrounds;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TrapperGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "trapper";
  static final int ARMED = 35;
  static final int LIMIT = TestPlayers.READY + ARMED + 30;

  static final Identifier TRAPPER = id("trapper");
  static final Identifier SNARE = id("trapper/snare");
  static final Identifier FLASH_TRAP = id("trapper/flash_trap");
  static final Identifier NET_SHOT = id("trapper/net_shot");
  static final Identifier KILLING_GROUND = id("trapper/killing_ground");
  static final Identifier VANISH = id("core/vanish");

  @BeforeBatch(batchId = BATCH)
  public void clearGrounds(final ServerWorld world) {
    KillingGrounds.clearAll();
    Baits.clearAll();
  }

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  private static void learn(final ServerPlayerEntity hunter, final String... skills) {
    TreeProgress progress = TreeProgress.EMPTY.withEarned(60).buy(id("hunter/tracking"), 1, null);
    for (int i = 0; i < skills.length; i++) {
      progress = progress.buy(id("trapper/" + skills[i]), 1, i == 0 ? TRAPPER : null);
    }
    SkillAttachment.set(hunter, SkillTree.HUNTER, progress);
    PowerCooldownAttachment.set(hunter, PowerCooldowns.NONE);
  }

  private static VampireEntity vampire(final TestContext context, final BlockPos at) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, at);
    vampire.setAiDisabled(true);
    return vampire;
  }

  private static void lookAtFloor(
      final TestContext context, final ServerPlayerEntity hunter, final BlockPos floor) {
    hunter.lookAt(
        EntityAnchorArgumentType.EntityAnchor.EYES,
        Vec3d.ofCenter(context.getAbsolutePos(floor)).add(0.0, 0.5, 0.0));
  }

  private static void cast(
      final TestContext context, final ServerPlayerEntity hunter, final Identifier power) {
    context.assertEquals(Powers.cast(hunter, power), PowerUse.Verdict.READY, power + " cast");
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void snarePinsAVampireThatStepsOnIt(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    learn(hunter, "field_craft", "snare");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          lookAtFloor(context, hunter, new BlockPos(3, 1, 3));
          cast(context, hunter, SNARE);
        });
    context.runAtTick(TestPlayers.READY + ARMED, () -> vampire(context, new BlockPos(3, 2, 3)));
    context.runAtTick(
        TestPlayers.READY + ARMED + 10,
        () -> {
          final VampireEntity caught =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      VampireEntity.class, context.getTestBox(), LivingEntity::isAlive)
                  .getFirst();
          context.assertTrue(Holds.pinned(caught), "the vampire is pinned by the snare");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void flashTrapBurnsAndBlindsAVampireThatComesNear(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    learn(hunter, "field_craft", "snare", "flash_trap");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          lookAtFloor(context, hunter, new BlockPos(3, 1, 3));
          cast(context, hunter, FLASH_TRAP);
        });
    context.runAtTick(TestPlayers.READY + ARMED, () -> vampire(context, new BlockPos(5, 2, 3)));
    context.runAtTick(
        TestPlayers.READY + ARMED + 10,
        () -> {
          final VampireEntity burned =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      VampireEntity.class, context.getTestBox(), LivingEntity::isAlive)
                  .getFirst();
          context.assertTrue(
              burned.getHealth() < burned.getMaxHealth(), "the flash burns the vampire");
          context.assertTrue(
              burned.hasStatusEffect(StatusEffects.BLINDNESS), "the flash blinds the vampire");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void netShotDragsABatToTheGround(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 3));
    final VampireEntity bat = vampire(context, new BlockPos(5, 2, 3));
    learn(hunter, "field_craft", "snare", "net_shot");
    SkillAttachment.set(
        bat,
        SkillTree.VAMPIRE,
        TreeProgress.EMPTY
            .withEarned(20)
            .buy(id("beast/beast_blood"), 1, id("beast"))
            .buy(id("beast/bat_form"), 1, null));
    BloodAttachment.set(bat, 20);
    context.runAtTick(TestPlayers.READY - 5, () -> BatForm.enter(bat));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(BatForm.isBat(bat), "the vampire is a bat before the net");
          hunter.lookAt(
              EntityAnchorArgumentType.EntityAnchor.EYES, bat.getBoundingBox().getCenter());
          cast(context, hunter, NET_SHOT);
        });
    context.runAtTick(
        TestPlayers.READY + 10,
        () -> {
          context.assertFalse(BatForm.isBat(bat), "the net forces the vampire out of bat form");
          context.assertTrue(Grounding.on(bat), "the net keeps the vampire on the ground");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void killingGroundBarsVanishInside(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    final VampireEntity vampire = vampire(context, new BlockPos(4, 2, 4));
    learn(hunter, "field_craft", "snare", "net_shot", "flash_trap", "killing_ground");
    SkillAttachment.set(
        vampire,
        SkillTree.VAMPIRE,
        TreeProgress.EMPTY
            .withEarned(20)
            .buy(id("core/thick_blood"), 1, null)
            .buy(VANISH, 1, null));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(
              Powers.check(vampire, VANISH) != PowerUse.Verdict.BARRED,
              "vanish is free before the ground is marked");
          cast(context, hunter, KILLING_GROUND);
          context.assertEquals(
              Powers.check(vampire, VANISH), PowerUse.Verdict.BARRED, "vanish is barred inside");
          TestPlayers.finish(context, hunter);
        });
  }
}
