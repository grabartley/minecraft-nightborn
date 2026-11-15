package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.coating.Coat;
import com.grahambartley.nightborn.coating.Coating;
import com.grahambartley.nightborn.coating.CoatingTerms;
import com.grahambartley.nightborn.coating.Coatings;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CoatingGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aHolyWaterCoatedSwordHitsVampiresHarderUntilItWearsOff(final TestContext context) {
    final ItemStack sword =
        context
            .getWorld()
            .getRecipeManager()
            .getFirstMatch(
                RecipeType.CRAFTING,
                CraftingRecipeInput.create(
                    2, 1, List.of(new ItemStack(Items.IRON_SWORD), HolyWater.bottle())),
                context.getWorld())
            .orElseThrow()
            .value()
            .craft(
                CraftingRecipeInput.create(
                    2, 1, List.of(new ItemStack(Items.IRON_SWORD), HolyWater.bottle())),
                context.getWorld().getRegistryManager());
    context.assertTrue(
        sword.isOf(Items.IRON_SWORD) && Coatings.of(sword).isPresent(), "the recipe coats it");
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(6, 2, 4));
    final ZombieEntity attacker = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 2, 4));
    attacker.setAiDisabled(true);
    attacker.setStackInHand(Hand.MAIN_HAND, sword);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final CoatingTerms terms =
              CoatingTerms.of(Coating.HOLY_WATER, ServerConfigService.current());
          final float bonus = swing(attacker, vampire) - swing(attacker, human);
          context.assertEquals(bonus, (float) terms.damage(), "coated bonus on a vampire");
          context.assertTrue(
              bonus < ServerConfigService.current().gear().silverDamage(), "below silver");
          context.assertTrue(
              Weakened.sources(vampire).sources().containsKey(HolyWater.WEAKENED_SOURCE),
              "the vampire is weakened");
          final ItemStack held = attacker.getMainHandStack();
          held.set(
              Coatings.COAT,
              new Coat(Coating.HOLY_WATER, terms.hits() - 1, Coatings.of(held).get().startedAt()));
          swing(attacker, vampire);
          context.assertTrue(Coatings.of(held).isEmpty(), "the last hit wears the coating off");
          context.assertEquals(swing(attacker, vampire), swing(attacker, human), "no bonus left");
          TestPlayers.finish(context, human, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void aGarlicCoatedArrowWeakensAVampireAndLosesItsCoatOnHit(final TestContext context) {
    final VampireEntity vampire = AmmoTargets.vampire(context, new BlockPos(4, 2, 5));
    final ItemStack coated = new ItemStack(Items.ARROW);
    coated.set(Coatings.COAT, Coat.fresh(Coating.GARLIC));
    final Vec3d from = context.getAbsolute(new Vec3d(4.5, 3.2, 1.5));
    final ArrowEntity arrow =
        new ArrowEntity(context.getWorld(), from.x, from.y, from.z, coated, null);
    arrow.setVelocity(0.0, 0.0, 3.0);
    context.getWorld().spawnEntity(arrow);
    context.waitAndRun(
        20,
        () -> {
          context.assertTrue(vampire.getHealth() < AmmoTargets.TOUGH, "the arrow hit");
          context.assertTrue(
              Weakened.sources(vampire).sources().containsKey(Coating.GARLIC.weakenedSource()),
              "the garlic weakened the vampire");
          context.assertFalse(
              arrow.getItemStack().contains(Coatings.COAT), "the fired arrow is plain again");
          context.complete();
        });
  }

  private static float swing(final ZombieEntity attacker, final ServerPlayerEntity target) {
    target.setHealth(target.getMaxHealth());
    target.timeUntilRegen = 0;
    final float before = target.getHealth();
    attacker.tryAttack(target);
    return before - target.getHealth();
  }
}
