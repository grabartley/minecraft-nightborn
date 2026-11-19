package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerCooldowns;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Tracking;
import com.grahambartley.nightborn.skill.TrackingState;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SeekerGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "seeker";
  static final int LIMIT = TestPlayers.READY + 40;

  static final Identifier SEEKER = id("seeker");

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  private static void learn(final ServerPlayerEntity hunter, final String... skills) {
    TreeProgress progress = TreeProgress.EMPTY.withEarned(80).buy(id("hunter/tracking"), 1, null);
    for (final String skill : skills) {
      progress = progress.buy(id("seeker/" + skill), 1, progress.lines().isEmpty() ? SEEKER : null);
    }
    SkillAttachment.set(hunter, SkillTree.HUNTER, progress);
    PowerCooldownAttachment.set(hunter, PowerCooldowns.NONE);
  }

  private static VampireEntity vampire(final TestContext context, final BlockPos at) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, at);
    vampire.setAiDisabled(true);
    return vampire;
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void keenTrackingReachesFurtherAndShowsBloodTrails(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    learn(hunter, "keen_tracking", "keen_tracking", "keen_tracking");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertEquals(
              Powers.cast(hunter, id("hunter/tracking")), PowerUse.Verdict.READY, "tracking cast");
          final TrackingState state = Tracking.state(hunter);
          context.assertTrue(state != null, "tracking starts");
          context.assertEquals(state.range(), 160, "three ranks reach 96 blocks further");
          context.assertTrue(state.bloodTrails(), "the last rank shows blood trails");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void markQuarryMakesTheHuntersBlowsHurtMore(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    final VampireEntity marked = vampire(context, new BlockPos(1, 2, 5));
    final VampireEntity unmarked = vampire(context, new BlockPos(5, 2, 5));
    learn(hunter, "keen_tracking", "mark_quarry");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          hunter.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, marked.getEyePos());
          context.assertEquals(
              Powers.cast(hunter, id("seeker/mark_quarry")),
              PowerUse.Verdict.READY,
              "mark quarry cast");
          marked.damage(hunter.getDamageSources().playerAttack(hunter), 4.0F);
          unmarked.damage(hunter.getDamageSources().playerAttack(hunter), 4.0F);
          context.assertTrue(
              marked.getMaxHealth() - marked.getHealth()
                  > unmarked.getMaxHealth() - unmarked.getHealth(),
              "the marked vampire takes more from the hunter's blow");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void readTheSceneReadsABittenVictimOnly(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    final VampireEntity biter = vampire(context, new BlockPos(5, 2, 5));
    final PigEntity victim = context.spawnEntity(EntityType.PIG, new BlockPos(1, 2, 4));
    victim.setAiDisabled(true);
    learn(hunter, "keen_tracking", "read_the_scene");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          hunter.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, victim.getEyePos());
          context.assertEquals(
              Powers.cast(hunter, id("seeker/read_the_scene")),
              PowerUse.Verdict.NO_SCENE,
              "an unbitten creature has nothing to read");
          victim.damage(NightbornDamage.bite(context.getWorld(), biter), 2.0F);
          context.assertEquals(
              Powers.cast(hunter, id("seeker/read_the_scene")),
              PowerUse.Verdict.READY,
              "a bitten creature can be read");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void revelationStripsAVampiresVeil(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    final VampireEntity vampire = vampire(context, new BlockPos(5, 2, 5));
    learn(hunter, "keen_tracking", "mark_quarry", "sixth_sense", "true_sight", "revelation");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 200));
          context.assertEquals(
              Powers.cast(hunter, id("seeker/revelation")),
              PowerUse.Verdict.READY,
              "revelation cast");
        });
    context.runAtTick(
        TestPlayers.READY + 2,
        () -> {
          context.assertFalse(
              vampire.hasStatusEffect(StatusEffects.INVISIBILITY), "the veil is stripped away");
          TestPlayers.finish(context, hunter);
        });
  }
}
