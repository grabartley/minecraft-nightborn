package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class VampireCoreGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireHasDoubleAHumansHealthUntilMadeHuman(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        3,
        () -> {
          context.assertEquals(vampire.getMaxHealth(), 2 * human.getMaxHealth(), "double health");
          VampireTransitions.makeHuman(vampire);
          context.assertEquals(vampire.getMaxHealth(), human.getMaxHealth(), "human again");
          TestPlayers.finish(context, vampire, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void phantomsNeverTargetAVampire(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 2, 4));
    final PhantomEntity phantom = context.spawnEntity(EntityType.PHANTOM, new BlockPos(4, 3, 4));
    phantom.setAiDisabled(true);
    context.assertFalse(phantom.canTarget(vampire), "a phantom ignores a vampire");
    context.assertTrue(phantom.canTarget(human), "a phantom still hunts a human");
    TestPlayers.finish(context, vampire, human);
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void timeAwakeNeverCountsForAVampire(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        25,
        () -> {
          final var rest = Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST);
          context.assertEquals(vampire.getStatHandler().getStat(rest), 0, "a vampire never tires");
          context.assertTrue(human.getStatHandler().getStat(rest) > 0, "a human does tire");
          TestPlayers.finish(context, vampire, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVanillaMapShowsHumansAndHidesVampires(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 2, 4));
    final ItemStack map =
        FilledMapItem.createMap(
            context.getWorld(), (int) human.getX(), (int) human.getZ(), (byte) 0, true, false);
    vampire.getInventory().insertStack(map.copy());
    human.getInventory().insertStack(map.copy());
    final MapState state = FilledMapItem.getMapState(map, context.getWorld());
    state.update(human, map);
    state.update(vampire, map);
    int markers = 0;
    for (final var ignored : state.getDecorations()) {
      markers++;
    }
    context.assertEquals(markers, 1, "only the human shows on the map");
    TestPlayers.finish(context, vampire, human);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void foodRestoresNoBloodButCanStillBeEaten(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    BloodAttachment.set(vampire, 10);
    context.assertTrue(vampire.canConsume(false), "a vampire can still eat");
    final ItemStack bread = new ItemStack(Items.BREAD);
    vampire.eatFood(context.getWorld(), bread, bread.get(DataComponentTypes.FOOD));
    context.assertEquals(BloodAttachment.get(vampire), 10, "food restores no blood");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampiresHungerBarStaysOutOfThePicture(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    vampire.getHungerManager().setFoodLevel(3);
    context.runAtTick(
        3,
        () -> {
          context.assertEquals(
              vampire.getHungerManager().getFoodLevel(), 20, "blood replaces the hunger bar");
          TestPlayers.finish(context, vampire);
        });
  }
}
