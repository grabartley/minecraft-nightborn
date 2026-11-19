package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.coating.Coatings;
import com.grahambartley.nightborn.faith.Kindling;
import com.grahambartley.nightborn.faith.Prayers;
import com.grahambartley.nightborn.faith.Rebukes;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerCooldowns;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.trap.MarkedGrounds;
import com.grahambartley.nightborn.weakened.Weakened;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class FaithfulGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "faithful";
  static final int LIMIT = TestPlayers.READY + 40;

  static final Identifier FAITHFUL = id("faithful");
  static final Identifier VANISH = id("core/vanish");

  @BeforeBatch(batchId = BATCH)
  public void clearPrayers(final ServerWorld world) {
    MarkedGrounds.clearAll();
    Prayers.clearAll();
  }

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  private static void learn(final ServerPlayerEntity hunter, final String... skills) {
    TreeProgress progress =
        TreeProgress.EMPTY
            .withEarned(80)
            .buy(id("hunter/tracking"), 1, null)
            .buy(id("hunter/faith"), 1, FAITHFUL);
    for (final String skill : skills) {
      progress = progress.buy(id("faithful/" + skill), 1, null);
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
        Powers.cast(hunter, id("faithful/" + power)), PowerUse.Verdict.READY, power + " cast");
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void mendHealsAHurtVillagerNearby(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(3, 2, 3));
    villager.setAiDisabled(true);
    villager.setHealth(5.0F);
    learn(hunter, "mend");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          cast(context, hunter, "mend");
          context.assertTrue(villager.getHealth() >= 11.0F, "mend heals the villager 6 health");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void consecrateWeakensAVampireAndBarsVanish(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    final VampireEntity vampire = vampire(context, new BlockPos(3, 2, 3));
    learn(hunter, "rebuke", "consecrate");
    SkillAttachment.set(
        vampire,
        SkillTree.VAMPIRE,
        TreeProgress.EMPTY
            .withEarned(20)
            .buy(id("core/thick_blood"), 1, null)
            .buy(VANISH, 1, null));
    context.runAtTick(TestPlayers.READY, () -> cast(context, hunter, "consecrate"));
    context.runAtTick(
        TestPlayers.READY + 15,
        () -> {
          context.assertTrue(
              Weakened.sources(vampire).strength(MarkedGrounds.WEAKENED_SOURCE) > 0,
              "the hallowed ground weakens the vampire");
          context.assertTrue(
              MarkedGrounds.bars(vampire, VANISH), "vanish is barred on hallowed ground");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void judgementBurnsAVampireInThePillar(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    final VampireEntity vampire = vampire(context, new BlockPos(4, 2, 4));
    learn(hunter, "rebuke", "mend", "consecrate", "sanctuary", "judgement");
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          hunter.lookAt(
              EntityAnchorArgumentType.EntityAnchor.EYES,
              Vec3d.ofCenter(context.getAbsolutePos(new BlockPos(4, 1, 4))).add(0.0, 0.5, 0.0));
          cast(context, hunter, "judgement");
        });
    context.runAtTick(
        TestPlayers.READY + 25,
        () -> {
          context.assertTrue(
              vampire.getHealth() < vampire.getMaxHealth(), "the pillar burns the vampire");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void rebukeThrowsBackAndSilencesAVampireInFront(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 3));
    final VampireEntity vampire = vampire(context, new BlockPos(4, 2, 3));
    learn(hunter, "rebuke");
    hunter.setStackInHand(Hand.MAIN_HAND, new ItemStack(GearItems.SILVER_CRUCIFIX));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          hunter.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, vampire.getEyePos());
          cast(context, hunter, "rebuke");
          context.assertTrue(Rebukes.silenced(vampire), "the vampire's powers are stopped");
          context.assertTrue(
              vampire.getVelocity().horizontalLength() > 0.1, "the vampire is thrown back");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = LIMIT)
  public void blessingAndBloodFireMarkTheHuntersBlade(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(1, 2, 1));
    learn(hunter, "blessing", "blood_fire");
    hunter.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final float before = hunter.getHealth();
          cast(context, hunter, "blessing");
          cast(context, hunter, "blood_fire");
          final ItemStack blade = hunter.getMainHandStack();
          context.assertTrue(
              Coatings.of(blade).filter(coat -> coat.blessed()).isPresent(),
              "the blade is blessed");
          context.assertTrue(
              Kindling.burning(blade, context.getWorld().getTime()).isPresent(),
              "the blade is alight");
          context.assertEquals(hunter.getHealth(), before - 4.0F, "blood fire costs 4 health");
          TestPlayers.finish(context, hunter);
        });
  }
}
