package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.downed.DownedState.Phase;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Item;
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

public class StakeGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String PVP = "stake-pvp";

  @BeforeBatch(batchId = PVP)
  public void pvpOn(final ServerWorld world) {
    BatchPvp.set(world.getServer(), true);
  }

  @AfterBatch(batchId = PVP)
  public void restorePvp(final ServerWorld world) {
    BatchPvp.restore(world.getServer());
  }

  @GameTest(templateName = ARENA, batchId = PVP, tickLimit = TestPlayers.READY + 20)
  public void aStakeKillsADownedVampire(final TestContext context) {
    final ServerPlayerEntity hunter = armed(context, new BlockPos(2, 2, 4), GearItems.WOODEN_STAKE);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.damage(vampire.getDamageSources().generic(), 1000.0F);
          context.assertTrue(vampire.isAlive(), "a blow downs the vampire first");
          vampire.damage(hunter.getDamageSources().playerAttack(hunter), 1.0F);
          context.assertTrue(vampire.isDead(), "a stake finishes a downed vampire");
          TestPlayers.finish(context, hunter, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = PVP, tickLimit = TestPlayers.READY + 20)
  public void aStakeBlowThatWouldDownAVampireStakesItInstead(final TestContext context) {
    final List<Item> weapons = List.of(GearItems.WOODEN_STAKE, Items.IRON_SWORD);
    final ServerPlayerEntity staker = armed(context, new BlockPos(1, 2, 2), weapons.get(0));
    final ServerPlayerEntity swordsman = armed(context, new BlockPos(1, 2, 6), weapons.get(1));
    final ServerPlayerEntity staked = TestPlayers.vampire(context, new BlockPos(3, 2, 2));
    final ServerPlayerEntity downed = TestPlayers.vampire(context, new BlockPos(3, 2, 6));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          staked.setHealth(2.0F);
          downed.setHealth(2.0F);
          staked.damage(staker.getDamageSources().playerAttack(staker), 5.0F);
          downed.damage(swordsman.getDamageSources().playerAttack(swordsman), 5.0F);
          context.assertTrue(staked.isDead(), "the stake's downing blow kills");
          final DownedState state = Downed.state(downed);
          context.assertTrue(
              downed.isAlive() && state != null && state.isDowned(), "a sword only downs");
          TestPlayers.finish(context, staker, swordsman, staked, downed);
        });
  }

  @GameTest(templateName = ARENA, batchId = PVP, tickLimit = TestPlayers.READY + 20)
  public void aStakeHurtsAnEscapingVampireButASwordDoesNot(final TestContext context) {
    final ServerPlayerEntity staker = armed(context, new BlockPos(1, 2, 2), GearItems.WOODEN_STAKE);
    final ServerPlayerEntity swordsman = armed(context, new BlockPos(1, 2, 6), Items.IRON_SWORD);
    final ServerPlayerEntity first = TestPlayers.vampire(context, new BlockPos(3, 2, 2));
    final ServerPlayerEntity second = TestPlayers.vampire(context, new BlockPos(3, 2, 6));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          for (final ServerPlayerEntity vampire : List.of(first, second)) {
            vampire.setHealth(20.0F);
            vampire.setAttached(Downed.TYPE, new DownedState(Phase.ESCAPING, 200));
          }
          first.damage(staker.getDamageSources().playerAttack(staker), 5.0F);
          second.damage(swordsman.getDamageSources().playerAttack(swordsman), 5.0F);
          context.assertTrue(
              first.isAlive() && first.getHealth() < 20.0F, "a stake hurts while escaping");
          context.assertEquals(second.getHealth(), 20.0F, "a sword does not");
          TestPlayers.finish(context, staker, swordsman, first, second);
        });
  }

  @GameTest(templateName = ARENA, batchId = PVP, tickLimit = TestPlayers.READY + 20)
  public void aStakedVampireLosesATenthOfItsPointsAndTheStakerIsPaid(final TestContext context) {
    final ServerPlayerEntity hunter = armed(context, new BlockPos(2, 2, 4), GearItems.WOODEN_STAKE);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          SkillAttachment.set(vampire, SkillTree.VAMPIRE, TreeProgress.EMPTY.addPoints(20));
          vampire.setHealth(1.0F);
          vampire.damage(hunter.getDamageSources().playerAttack(hunter), 5.0F);
          context.assertTrue(vampire.isDead(), "the vampire is staked");
          context.assertEquals(
              SkillAttachment.get(vampire, SkillTree.VAMPIRE).earned(), 18, "it lost 10%");
          context.assertEquals(
              SkillAttachment.get(hunter, SkillTree.HUNTER).earned(), 3, "the staker is paid");
          TestPlayers.finish(context, hunter, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = PVP, tickLimit = TestPlayers.READY + 20)
  public void aVampireCanStakeAVampireButEarnsNoHunterPoints(final TestContext context) {
    final ServerPlayerEntity rival = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    rival.setStackInHand(Hand.MAIN_HAND, new ItemStack(GearItems.WOODEN_STAKE));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          SkillAttachment.set(rival, SkillTree.VAMPIRE, TreeProgress.EMPTY.addPoints(5));
          vampire.setHealth(1.0F);
          vampire.damage(rival.getDamageSources().playerAttack(rival), 5.0F);
          context.assertTrue(vampire.isDead(), "any stake kills, whoever holds it");
          context.assertEquals(
              SkillAttachment.get(rival, SkillTree.HUNTER).earned(),
              0,
              "a vampire earns no hunter points");
          TestPlayers.finish(context, rival, vampire);
        });
  }

  private static ServerPlayerEntity armed(
      final TestContext context, final BlockPos at, final Item weapon) {
    final ServerPlayerEntity player = TestPlayers.human(context, at);
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(weapon));
    return player;
  }
}
