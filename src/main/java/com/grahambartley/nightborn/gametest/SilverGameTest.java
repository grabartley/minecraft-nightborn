package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.gear.GearBlocks;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.gear.SilverOreGeneration;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.PlacedFeature;

public class SilverGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aMobSwingingSilverOrAStakeHitsVampiresHarder(final TestContext context) {
    final List<Item> weapons =
        List.of(
            GearItems.WOODEN_STAKE,
            GearItems.SILVER_SWORD,
            GearItems.SILVER_AXE,
            GearItems.SILVER_STAKE,
            Items.IRON_SWORD);
    final List<Float> bonus = List.of(4.0F, 4.0F, 4.0F, 8.0F, 0.0F);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(6, 2, 4));
    final ZombieEntity attacker = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 2, 4));
    attacker.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          for (int i = 0; i < weapons.size(); i++) {
            attacker.setStackInHand(Hand.MAIN_HAND, new ItemStack(weapons.get(i)));
            final float vampireLoss = swing(attacker, vampire);
            final float humanLoss = swing(attacker, human);
            context.assertEquals(
                vampireLoss - humanLoss, bonus.get(i), weapons.get(i) + " bonus on a vampire");
          }
          TestPlayers.finish(context, human, vampire);
        });
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void silverOreNeedsIronAndDropsRawSilver(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final BlockPos pos = context.getAbsolutePos(BlockPos.ORIGIN);
    for (final Block ore : List.of(GearBlocks.SILVER_ORE, GearBlocks.DEEPSLATE_SILVER_ORE)) {
      final BlockState state = ore.getDefaultState();
      context.assertTrue(new ItemStack(Items.IRON_PICKAXE).isSuitableFor(state), ore + " iron");
      context.assertFalse(new ItemStack(Items.STONE_PICKAXE).isSuitableFor(state), ore + " stone");
      final List<ItemStack> drops =
          Block.getDroppedStacks(state, world, pos, null, null, new ItemStack(Items.IRON_PICKAXE));
      context.assertTrue(
          drops.size() == 1 && drops.get(0).isOf(GearItems.RAW_SILVER), ore + " drops raw silver");
    }
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void silverSmeltsIntoIngots(final TestContext context) {
    final ServerWorld world = context.getWorld();
    for (final Item input : List.of(GearItems.RAW_SILVER, GearBlocks.SILVER_ORE.asItem())) {
      final SingleStackRecipeInput stack = new SingleStackRecipeInput(new ItemStack(input));
      context.assertTrue(
          smelted(world, RecipeType.SMELTING, stack).isOf(GearItems.SILVER_INGOT),
          input + " smelts");
      context.assertTrue(
          smelted(world, RecipeType.BLASTING, stack).isOf(GearItems.SILVER_INGOT),
          input + " blasts");
    }
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void silverOreIsPlacedInOverworldBiomes(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final PlacedFeature feature =
        world
            .getRegistryManager()
            .get(RegistryKeys.PLACED_FEATURE)
            .get(SilverOreGeneration.ORE_SILVER);
    context.assertTrue(feature != null, "the silver placed feature loads");
    final boolean placed =
        world
            .getBiome(context.getAbsolutePos(BlockPos.ORIGIN))
            .value()
            .getGenerationSettings()
            .isFeatureAllowed(feature);
    context.assertTrue(placed, "the overworld biome places silver ore");
    context.complete();
  }

  private static <T extends AbstractCookingRecipe> ItemStack smelted(
      final ServerWorld world, final RecipeType<T> type, final SingleStackRecipeInput input) {
    return world
        .getRecipeManager()
        .getFirstMatch(type, input, world)
        .map(entry -> entry.value().getResult(world.getRegistryManager()))
        .orElse(ItemStack.EMPTY);
  }

  private static float swing(final ZombieEntity attacker, final ServerPlayerEntity target) {
    target.setHealth(target.getMaxHealth());
    target.timeUntilRegen = 0;
    final float before = target.getHealth();
    attacker.tryAttack(target);
    return before - target.getHealth();
  }
}
