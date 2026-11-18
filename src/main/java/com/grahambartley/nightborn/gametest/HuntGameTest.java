package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.hunt.HuntAttachment;
import com.grahambartley.nightborn.hunt.HuntItems;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class HuntGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final long DAY = 1000L;
  static final String BELL_BY_DAY = "hunt_bell_by_day";

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aVampireWhoKillsAHunterGainsNotoriety(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(5, 2, 4));
    hunter.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          hunter.damage(vampire.getDamageSources().playerAttack(vampire), 1000.0F);
          context.assertTrue(hunter.isDead(), "the hunter is killed");
          context.assertEquals(
              HuntAttachment.get(vampire).notoriety(), 1.0, "a lone hunter adds one notoriety");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aGuildMasterKilledByAPlayerDropsHisSeal(final TestContext context) {
    final ServerPlayerEntity killer = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final GuildMasterEntity master =
        context.spawnEntity(GuildMobs.GUILD_MASTER, new BlockPos(5, 2, 4));
    master.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          master.damage(killer.getDamageSources().playerAttack(killer), 1000.0F);
          context.assertTrue(master.isDead(), "the Guild Master is killed");
          final List<ItemEntity> seals =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      ItemEntity.class,
                      context.getTestBox(),
                      item -> item.getStack().isOf(HuntItems.GUILD_MASTERS_SEAL));
          context.assertEquals(seals.size(), 1, "one seal drops");
          TestPlayers.finish(context, killer);
        });
  }

  @GameTest(templateName = EMPTY_STRUCTURE)
  public void theHuntersBellNeedsABellAndFourSeals(final TestContext context) {
    context.assertTrue(
        craft(context, 4).isOf(HuntItems.HUNTERS_BELL), "a bell and four seals make the bell");
    context.assertTrue(craft(context, 3).isEmpty(), "three seals are not enough");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BELL_BY_DAY, tickLimit = TestPlayers.READY + 20)
  public void theBellRungByDayKeepsItsSeal(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    vampire.setStackInHand(Hand.MAIN_HAND, new ItemStack(HuntItems.HUNTERS_BELL));
    vampire.getInventory().insertStack(new ItemStack(HuntItems.GUILD_MASTERS_SEAL, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.setTime((int) DAY);
          HuntItems.HUNTERS_BELL.use(context.getWorld(), vampire, Hand.MAIN_HAND);
          context.assertEquals(
              vampire.getInventory().count(HuntItems.GUILD_MASTERS_SEAL), 2, "no seal is used");
          context.assertFalse(
              vampire.hasAttached(HuntAttachment.CALL), "nobody answers the bell by day");
          TestPlayers.finish(context, vampire);
        });
  }

  private static ItemStack craft(final TestContext context, final int seals) {
    final List<ItemStack> grid = new ArrayList<>();
    for (int slot = 0; slot < 9; slot++) {
      grid.add(ItemStack.EMPTY);
    }
    grid.set(4, new ItemStack(Items.BELL));
    final int[] around = {1, 3, 5, 7};
    for (int seal = 0; seal < seals; seal++) {
      grid.set(around[seal], new ItemStack(HuntItems.GUILD_MASTERS_SEAL));
    }
    final CraftingRecipeInput input = CraftingRecipeInput.create(3, 3, grid);
    return context
        .getWorld()
        .getRecipeManager()
        .getFirstMatch(RecipeType.CRAFTING, input, context.getWorld())
        .map(recipe -> recipe.value().craft(input, context.getWorld().getRegistryManager()))
        .orElse(ItemStack.EMPTY);
  }
}
