package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.gear.GarlicStringBlock;
import com.grahambartley.nightborn.gear.GearBlocks;
import com.grahambartley.nightborn.mirror.MirrorItems;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.VampirePose;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class HiddenVampireGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final int SCANNED = 25;

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void aVampireRecoilsWhenGarlicFirstReachesIt(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    vampire.setAiDisabled(true);
    context.assertTrue(vampire.action().isEmpty(), "no clip before the garlic");
    context.setBlockState(
        new BlockPos(4, 3, 1),
        GearBlocks.GARLIC_STRING.getDefaultState().with(GarlicStringBlock.HANGING, true));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(
              vampire.action(),
              VampirePose.RECOIL_GARLIC.animation(),
              "the garlic recoil is shown");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aHandMirrorIsRaisedAtWhoeverItIsUsedOn(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(3, 2, 4));
    villager.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertEquals(
              UseEntityCallback.EVENT
                  .invoker()
                  .interact(human, context.getWorld(), Hand.MAIN_HAND, villager, null),
              ActionResult.PASS,
              "an empty hand is left to vanilla");
          human.setStackInHand(Hand.MAIN_HAND, new ItemStack(MirrorItems.HAND_MIRROR));
          context.assertEquals(
              UseEntityCallback.EVENT
                  .invoker()
                  .interact(human, context.getWorld(), Hand.MAIN_HAND, villager, null),
              ActionResult.SUCCESS,
              "the mirror is raised");
          context.assertTrue(villager.getCustomer() == null, "no trade opens");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void aHandMirrorIsCraftedFromGlassAndAGoldIngot(final TestContext context) {
    final RecipeEntry<?> recipe =
        context
            .getWorld()
            .getRecipeManager()
            .get(Identifier.of("nightborn", "hand_mirror"))
            .orElseThrow();
    final ItemStack result = recipe.value().getResult(context.getWorld().getRegistryManager());
    context.assertTrue(result.isOf(MirrorItems.HAND_MIRROR), "the recipe makes a hand mirror");
    context.assertTrue(
        recipe.value().getIngredients().stream()
            .anyMatch(ingredient -> ingredient.test(new ItemStack(Items.GLASS))),
        "glass");
    context.assertTrue(
        recipe.value().getIngredients().stream()
            .anyMatch(ingredient -> ingredient.test(new ItemStack(Items.GOLD_INGOT))),
        "a gold ingot");
    context.complete();
  }
}
