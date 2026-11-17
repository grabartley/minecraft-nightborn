package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

public class HolyWaterGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final double SPLASH_HEIGHT = 1.5;

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void splashedHolyWaterScaldsAndWeakensAVampire(final TestContext context) {
    final VampireEntity vampire = AmmoTargets.vampire(context, new BlockPos(4, 2, 4));
    splash(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        30,
        () -> {
          context.assertTrue(vampire.getHealth() < AmmoTargets.TOUGH, "holy water scalds");
          context.assertTrue(
              Weakened.sources(vampire).sources().containsKey(HolyWater.WEAKENED_SOURCE),
              "holy water weakens");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void splashedHolyWaterLeavesAnythingElseAlone(final TestContext context) {
    final PigEntity pig =
        AmmoTargets.tough(context.spawnEntity(EntityType.PIG, new BlockPos(4, 2, 4)));
    splash(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        30,
        () -> {
          context.assertEquals(pig.getHealth(), AmmoTargets.TOUGH, "the pig is unhurt");
          context.assertTrue(Weakened.sources(pig).isEmpty(), "the pig is not weakened");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void drinkingHolyWaterCuresBiteSickness(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    BiteSickness.start(human, ServerConfigService.current().turning());
    final ItemStack left = HolyWater.bottle().finishUsing(context.getWorld(), human);
    context.assertFalse(BiteSicknessAttachment.isSick(human), "holy water cured the sickness");
    context.assertTrue(left.isOf(Items.GLASS_BOTTLE), "the bottle is left empty");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void drinkingHolyWaterScaldsAVampire(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final float before = vampire.getHealth();
          HolyWater.bottle().finishUsing(context.getWorld(), vampire);
          context.assertTrue(vampire.getHealth() < before, "the drinker is scalded");
          context.assertTrue(
              Weakened.sources(vampire).sources().containsKey(HolyWater.WEAKENED_SOURCE),
              "the drinker is weakened");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aSilverNuggetBrewsWaterIntoHolyWater(final TestContext context) {
    final var brewing = context.getWorld().getBrewingRecipeRegistry();
    final ItemStack holy =
        brewing.craft(
            new ItemStack(GearItems.SILVER_NUGGET),
            PotionContentsComponent.createStack(Items.POTION, Potions.WATER));
    context.assertTrue(HolyWater.isHolyWater(holy), "water and silver brew holy water");
    final ItemStack splash = brewing.craft(new ItemStack(Items.GUNPOWDER), holy);
    context.assertTrue(
        splash.isOf(Items.SPLASH_POTION) && HolyWater.isHolyWater(splash),
        "gunpowder makes it splash");
    final ItemStack lingering = brewing.craft(new ItemStack(Items.DRAGON_BREATH), splash);
    context.assertTrue(
        lingering.isOf(Items.LINGERING_POTION) && HolyWater.isHolyWater(lingering),
        "dragon's breath makes it linger");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aClericBlessesWaterAtApprenticeLevel(final TestContext context) {
    final var factories =
        TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(VillagerProfession.CLERIC).get(2);
    final var villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(4, 2, 4));
    villager.setAiDisabled(true);
    boolean blesses = false;
    for (final TradeOffers.Factory factory : factories) {
      final TradeOffer offer = factory.create(villager, Random.create(0));
      blesses |= offer != null && HolyWater.isHolyWater(offer.getSellItem());
    }
    context.assertTrue(blesses, "an apprentice cleric can bless water");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void holyWaterCannotTipArrows(final TestContext context) {
    context.assertTrue(
        tipped(context, PotionContentsComponent.createStack(Items.LINGERING_POTION, Potions.WATER)),
        "lingering water still tips arrows");
    context.assertFalse(
        tipped(
            context, PotionContentsComponent.createStack(Items.LINGERING_POTION, HolyWater.POTION)),
        "lingering holy water tips no arrows");
    context.complete();
  }

  private static boolean tipped(final TestContext context, final ItemStack lingering) {
    final ItemStack arrow = new ItemStack(Items.ARROW);
    final CraftingRecipeInput input =
        CraftingRecipeInput.create(
            3, 3, List.of(arrow, arrow, arrow, arrow, lingering, arrow, arrow, arrow, arrow));
    return context
        .getWorld()
        .getRecipeManager()
        .getFirstMatch(RecipeType.CRAFTING, input, context.getWorld())
        .isPresent();
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void holyWaterThrownAtAVampireEarnsHolyMoly(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(1, 2, 1));
    final VampireEntity vampire = AmmoTargets.vampire(context, new BlockPos(4, 2, 4));
    splash(context, new BlockPos(4, 2, 4)).setOwner(hunter);
    context.runAtTick(
        30,
        () -> {
          context.assertTrue(vampire.getHealth() < AmmoTargets.TOUGH, "holy water scalds");
          context.assertTrue(
              TestAdvancements.done(context, hunter, "holy_moly"), "the thrower earns Holy Moly");
          TestPlayers.finish(context, hunter);
        });
  }

  private static PotionEntity splash(final TestContext context, final BlockPos target) {
    final PotionEntity potion =
        context.spawnEntity(
            EntityType.POTION, Vec3d.ofBottomCenter(target).add(0.0, SPLASH_HEIGHT, 0.0));
    potion.setItem(PotionContentsComponent.createStack(Items.SPLASH_POTION, HolyWater.POTION));
    potion.setVelocity(0.0, -0.5, 0.0);
    return potion;
  }
}
