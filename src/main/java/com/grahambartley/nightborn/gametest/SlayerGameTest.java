package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.hold.HoldKind;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.mob.MobBite;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerCooldowns;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.slayer.Charges;
import com.grahambartley.nightborn.slayer.Guards;
import com.grahambartley.nightborn.slayer.SlayerMarks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SlayerGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "slayer";
  static final int LIMIT = TestPlayers.READY + 40;

  static final Identifier SLAYER = id("slayer");

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  private static void learn(final ServerPlayerEntity hunter, final String... skills) {
    TreeProgress progress =
        TreeProgress.EMPTY
            .withEarned(80)
            .buy(id("hunter/tracking"), 1, null)
            .buy(id("hunter/bite_ward"), 1, SLAYER);
    for (final String skill : skills) {
      progress = progress.buy(id("slayer/" + skill), 1, null);
    }
    SkillAttachment.set(hunter, SkillTree.HUNTER, progress);
    PowerCooldownAttachment.set(hunter, PowerCooldowns.NONE);
  }

  private static VampireEntity vampire(final TestContext context, final BlockPos at) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, at);
    vampire.setAiDisabled(true);
    return vampire;
  }

  private static void cast(
      final TestContext context, final ServerPlayerEntity hunter, final String power) {
    context.assertEquals(
        Powers.cast(hunter, id("slayer/" + power)), PowerUse.Verdict.READY, power + " cast");
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void stakeThrowWoundsAndStaggersAVampire(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    final VampireEntity vampire = vampire(context, new BlockPos(1, 2, 5));
    hunter.getInventory().insertStack(new ItemStack(GearItems.WOODEN_STAKE));
    learn(hunter, "stake_throw");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          hunter.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, vampire.getEyePos());
          cast(context, hunter, "stake_throw");
        });
    context.runAtTick(
        TestPlayers.READY + 8,
        () -> {
          context.assertTrue(
              vampire.getHealth() < vampire.getMaxHealth(), "the thrown stake wounds the vampire");
          context.assertEquals(
              Holds.held(vampire).orElse(null), HoldKind.STAGGER, "the vampire staggers");
          context.assertTrue(vampire.isAlive(), "a thrown stake never stakes");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void sidestepSlipsAVampireBlowAndStaggersIt(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(4, 2, 5));
    final VampireEntity vampire = vampire(context, new BlockPos(4, 2, 7));
    learn(hunter, "sidestep");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final float health = hunter.getHealth();
          cast(context, hunter, "sidestep");
          hunter.damage(hunter.getDamageSources().mobAttack(vampire), 4.0F);
          context.assertEquals(hunter.getHealth(), health, "nothing touches the dashing hunter");
          context.assertTrue(Holds.opened(vampire), "the vampire that swung is staggered open");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void riposteTurnsBackACharge(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(4, 2, 2));
    final VampireEntity vampire = vampire(context, new BlockPos(4, 2, 3));
    learn(hunter, "sidestep", "riposte");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          cast(context, hunter, "riposte");
          Charges.start(vampire);
          Guards.tick(hunter);
          context.assertTrue(Holds.opened(vampire), "the charging vampire is staggered open");
          context.assertTrue(
              hunter.distanceTo(vampire) > 2.0F, "the charging vampire is thrown back");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void heartstrikeDownsAPinnedVampire(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    final VampireEntity vampire = vampire(context, new BlockPos(1, 2, 4));
    hunter.setStackInHand(Hand.MAIN_HAND, new ItemStack(GearItems.WOODEN_STAKE));
    learn(hunter, "sidestep", "riposte", "heartstrike");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setHealth(vampire.getMaxHealth() * 0.3F);
          Holds.hold(hunter, vampire, HoldKind.PIN, 100);
          hunter.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, vampire.getEyePos());
          cast(context, hunter, "heartstrike");
          context.assertTrue(Downed.helpless(vampire), "the pinned vampire is downed");
          context.assertTrue(hunter.distanceTo(vampire) < 2.0F, "the hunter lunged to it");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void lastStandWardsADyingHunter(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    final VampireEntity vampire = vampire(context, new BlockPos(1, 2, 2));
    learn(hunter, "tough_as_oak", "last_stand");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          hunter.damage(hunter.getDamageSources().generic(), hunter.getMaxHealth() * 0.8F);
          context.assertTrue(
              SlayerMarks.STEADFAST.on(hunter), "the hunter cannot be bitten or mesmerised");
          context.assertFalse(
              MobBite.bite(vampire, hunter, 2.0F, SoundEvents.ENTITY_GENERIC_DRINK),
              "a vampire mob's bite is refused");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void onslaughtDownsABadlyHurtVampire(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    final VampireEntity vampire = vampire(context, new BlockPos(1, 2, 3));
    learn(hunter, "sidestep", "riposte", "heartstrike", "tough_as_oak", "last_stand", "onslaught");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          cast(context, hunter, "onslaught");
          vampire.setHealth(vampire.getMaxHealth() * 0.25F);
          vampire.damage(hunter.getDamageSources().playerAttack(hunter), 1.0F);
          context.assertTrue(Downed.helpless(vampire), "the hurt vampire is downed");
          TestPlayers.finish(context, hunter);
        });
  }
}
