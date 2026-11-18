package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.clothing.ClothingItems;
import com.grahambartley.nightborn.clothing.ClothingPiece;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillShop;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class AdvancementGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final BlockPos STAND = new BlockPos(4, 2, 4);

  record Expected(
      String path, String parent, AdvancementFrame frame, int experience, boolean hidden) {}

  static final List<Expected> TREE =
      List.of(
          new Expected("root", null, AdvancementFrame.TASK, 0, false),
          new Expected("love_bites", "root", AdvancementFrame.TASK, 0, false),
          new Expected("stake_holder", "root", AdvancementFrame.TASK, 0, false),
          new Expected("sworn_in", "stake_holder", AdvancementFrame.GOAL, 50, false),
          new Expected("holy_moly", "root", AdvancementFrame.TASK, 0, false),
          new Expected("silver_lining", "root", AdvancementFrame.TASK, 0, false),
          new Expected("garlic_breath", "root", AdvancementFrame.TASK, 0, false),
          new Expected("house_call", "root", AdvancementFrame.TASK, 0, false),
          new Expected("dressed_to_kill", "root", AdvancementFrame.TASK, 0, false),
          new Expected("close_call", "love_bites", AdvancementFrame.GOAL, 50, false),
          new Expected("no_reflection_on_you", "love_bites", AdvancementFrame.GOAL, 50, false),
          new Expected("first_blood", "no_reflection_on_you", AdvancementFrame.TASK, 0, false),
          new Expected(
              "fresh_off_the_neck", "no_reflection_on_you", AdvancementFrame.TASK, 0, false),
          new Expected("bad_taste", "no_reflection_on_you", AdvancementFrame.TASK, 0, true),
          new Expected("sun_dried", "no_reflection_on_you", AdvancementFrame.TASK, 0, false),
          new Expected("fly_by_night", "no_reflection_on_you", AdvancementFrame.TASK, 0, false),
          new Expected(
              "strings_attached", "no_reflection_on_you", AdvancementFrame.CHALLENGE, 100, false),
          new Expected("day_shift", "no_reflection_on_you", AdvancementFrame.TASK, 0, false),
          new Expected("old_blood", "no_reflection_on_you", AdvancementFrame.GOAL, 50, false),
          new Expected(
              "good_help_is_hard_to_find", "no_reflection_on_you", AdvancementFrame.TASK, 0, true),
          new Expected("lord_of_the_manner", "house_call", AdvancementFrame.CHALLENGE, 100, false));

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theTabIsTheTreeThePrdDescribes(final TestContext context) {
    final long loaded =
        context.getWorld().getServer().getAdvancementLoader().getAdvancements().stream()
            .filter(entry -> entry.id().getNamespace().equals(Nightborn.MOD_ID))
            .filter(entry -> !entry.id().getPath().startsWith("recipes/"))
            .count();
    context.assertEquals((int) loaded, TREE.size(), "advancements in the tab");
    for (final Expected expected : TREE) {
      final Advancement advancement = TestAdvancements.entry(context, expected.path()).value();
      final AdvancementDisplay display = advancement.display().orElseThrow();
      context.assertTrue(
          Objects.equals(
              advancement.parent().map(Identifier::getPath).orElse(null), expected.parent()),
          expected.path() + " has parent " + expected.parent());
      context.assertEquals(display.getFrame(), expected.frame(), expected.path() + " frame");
      context.assertEquals(
          advancement.rewards().experience(), expected.experience(), expected.path() + " xp");
      context.assertEquals(display.isHidden(), expected.hidden(), expected.path() + " hidden");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void joiningEarnsTheRootAndTurningEarnsNoReflection(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, STAND);
    context.runAtTick(
        5,
        () -> {
          context.assertTrue(TestAdvancements.done(context, human, "root"), "root on joining");
          context.assertFalse(
              TestAdvancements.done(context, human, "no_reflection_on_you"), "still human");
          VampireTransitions.turn(human);
          context.assertTrue(
              TestAdvancements.done(context, human, "no_reflection_on_you"), "turning earns it");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void curingBiteSicknessEarnsCloseCall(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, STAND);
    BiteSickness.start(human, ServerConfigService.current().turning());
    context.assertFalse(TestAdvancements.done(context, human, "close_call"), "sick, not cured");
    BiteSickness.cure(human);
    context.assertTrue(TestAdvancements.done(context, human, "close_call"), "cured");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aSecondBloodlineEarnsOldBloodInTwoSteps(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    SkillAttachment.set(vampire, SkillTree.VAMPIRE, TreeProgress.EMPTY.withEarned(20));
    SkillShop.buy(vampire, Identifier.of(Nightborn.MOD_ID, "core/thick_blood"));
    context.assertEquals(
        SkillShop.buy(vampire, Identifier.of(Nightborn.MOD_ID, "noble/noble_blood")),
        SkillPurchase.Verdict.ALLOWED,
        "first bloodline bought");
    final AdvancementProgress halfway = TestAdvancements.progress(context, vampire, "old_blood");
    context.assertFalse(halfway.isDone(), "one bloodline is not enough");
    context.assertTrue(halfway.isAnyObtained(), "one bloodline shows as progress");
    context.assertEquals(
        SkillShop.buy(vampire, Identifier.of(Nightborn.MOD_ID, "beast/beast_blood")),
        SkillPurchase.Verdict.ALLOWED,
        "second bloodline bought");
    context.assertTrue(TestAdvancements.done(context, vampire, "old_blood"), "two bloodlines");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void garlicStringsCountUpToStringsAttached(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    MilestoneCriterion.reach(vampire, Milestone.GARLIC_STRINGS, 2);
    final AdvancementProgress two = TestAdvancements.progress(context, vampire, "strings_attached");
    context.assertFalse(two.isDone(), "two strings are not three");
    context.assertEquals(
        two.getProgressBarPercentage(), 2.0F / 3.0F, "two of three steps show as progress");
    MilestoneCriterion.reach(vampire, Milestone.GARLIC_STRINGS, 3);
    context.assertTrue(
        TestAdvancements.done(context, vampire, "strings_attached"), "three strings at once");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void onlyAVillagerCountsForFreshOffTheNeck(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    final CowEntity cow = context.spawnEntity(EntityType.COW, STAND.east());
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, STAND.west());
    cow.setAiDisabled(true);
    villager.setAiDisabled(true);
    MilestoneCriterion.reach(vampire, Milestone.FED, cow);
    context.assertTrue(TestAdvancements.done(context, vampire, "first_blood"), "any creature");
    context.assertFalse(
        TestAdvancements.done(context, vampire, "fresh_off_the_neck"), "a cow is no villager");
    MilestoneCriterion.reach(vampire, Milestone.FED, villager);
    context.assertTrue(
        TestAdvancements.done(context, vampire, "fresh_off_the_neck"), "a villager counts");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void garlicCarriedIntoAVampireHomeEarnsGarlicBreath(final TestContext context) {
    final ServerPlayerEntity bare = TestPlayers.human(context, STAND);
    final ServerPlayerEntity pocket = TestPlayers.human(context, STAND);
    final ServerPlayerEntity hand = TestPlayers.human(context, STAND);
    pocket.getInventory().insertStack(new ItemStack(GearItems.GARLIC));
    hand.setStackInHand(Hand.OFF_HAND, new ItemStack(GearItems.GARLIC));
    for (final ServerPlayerEntity player : List.of(bare, pocket, hand)) {
      MilestoneCriterion.reach(player, Milestone.STRUCTURE_ENTERED);
      context.assertTrue(
          TestAdvancements.done(context, player, "house_call"), "entering earns House Call");
    }
    context.assertFalse(
        TestAdvancements.done(context, bare, "garlic_breath"), "no garlic, no Garlic Breath");
    context.assertTrue(
        TestAdvancements.done(context, pocket, "garlic_breath"), "garlic in the inventory");
    context.assertTrue(
        TestAdvancements.done(context, hand, "garlic_breath"), "garlic in the off hand");
    TestPlayers.finish(context, bare, pocket, hand);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void craftingAnySilverWeaponEarnsSilverLining(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, STAND);
    Criteria.RECIPE_CRAFTED.trigger(
        human, Identifier.of(Nightborn.MOD_ID, "silver_ingot_from_nuggets"), List.of());
    context.assertFalse(
        TestAdvancements.done(context, human, "silver_lining"), "an ingot is no weapon");
    Criteria.RECIPE_CRAFTED.trigger(
        human, Identifier.of(Nightborn.MOD_ID, "silver_axe"), List.of());
    context.assertTrue(TestAdvancements.done(context, human, "silver_lining"), "a silver axe");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, tickLimit = 30)
  public void aFullSetOfGothicClothingEarnsDressedToKill(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, STAND);
    human.equipStack(EquipmentSlot.HEAD, new ItemStack(ClothingItems.of(ClothingPiece.TOP_HAT)));
    human.equipStack(
        EquipmentSlot.CHEST, new ItemStack(ClothingItems.of(ClothingPiece.FROCK_COAT)));
    human.equipStack(EquipmentSlot.LEGS, new ItemStack(ClothingItems.of(ClothingPiece.BREECHES)));
    context.runAtTick(
        5,
        () -> {
          context.assertFalse(
              TestAdvancements.done(context, human, "dressed_to_kill"), "three of four pieces");
          human.equipStack(
              EquipmentSlot.FEET, new ItemStack(ClothingItems.of(ClothingPiece.RIDING_BOOTS)));
        });
    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              TestAdvancements.done(context, human, "dressed_to_kill"), "the full set");
          TestPlayers.finish(context, human);
        });
  }
}
