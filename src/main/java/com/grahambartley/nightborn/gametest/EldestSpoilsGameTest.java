package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.eldest.EldestSpoils;
import com.grahambartley.nightborn.eldest.EldestTrims;
import com.grahambartley.nightborn.eldest.PetBatEntity;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class EldestSpoilsGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final BlockPos BESIDE = new BlockPos(4, 2, 2);

  private static void upgrades(final TestContext context, final Item silver, final Item ashen) {
    final SmithingRecipeInput input =
        new SmithingRecipeInput(
            ItemStack.EMPTY, new ItemStack(silver), new ItemStack(EldestSpoils.ASH));
    final ItemStack made =
        context
            .getWorld()
            .getRecipeManager()
            .getFirstMatch(RecipeType.SMITHING, input, context.getWorld())
            .map(recipe -> recipe.value().craft(input, context.getWorld().getRegistryManager()))
            .orElse(ItemStack.EMPTY);
    context.assertTrue(made.isOf(ashen), silver + " and ash make " + made + ", not " + ashen);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void ashTurnsSilverWeaponsAshenAtASmithingTable(final TestContext context) {
    upgrades(context, GearItems.SILVER_SWORD, EldestSpoils.ASHEN_SILVER_SWORD);
    upgrades(context, GearItems.SILVER_AXE, EldestSpoils.ASHEN_SILVER_AXE);
    upgrades(context, GearItems.SILVER_STAKE, EldestSpoils.ASHEN_SILVER_STAKE);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void theTrimsTheSongAndTheirRecipesLoad(final TestContext context) {
    final var registries = context.getWorld().getRegistryManager();
    context.assertTrue(
        registries.get(RegistryKeys.TRIM_PATTERN).contains(EldestTrims.FANG), "the fang trim");
    context.assertTrue(
        registries.get(RegistryKeys.TRIM_PATTERN).contains(EldestTrims.SIGIL), "the sigil trim");
    context.assertTrue(
        registries.get(RegistryKeys.JUKEBOX_SONG).contains(EldestSpoils.SONG), "the disc's song");
    for (final String recipe :
        List.of(
            "fang_armor_trim_smithing_template",
            "fang_armor_trim_smithing_template_smithing_trim",
            "sigil_armor_trim_smithing_template",
            "sigil_armor_trim_smithing_template_smithing_trim")) {
      context.assertTrue(
          context
              .getWorld()
              .getRecipeManager()
              .get(Identifier.of(Nightborn.MOD_ID, recipe))
              .isPresent(),
          recipe + " loaded");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void anEldestEndedByAStakeAlwaysLeavesItsAsh(final TestContext context) {
    final EldestEntity eldest = EldestGameTest.eldest(context);
    eldest.damage(NightbornDamage.stake(context.getWorld(), null), Float.MAX_VALUE);
    context.runAtTick(
        5,
        () -> {
          final int ash =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      ItemEntity.class,
                      context.getTestBox().expand(1.0),
                      item -> item.getStack().isOf(EldestSpoils.ASH))
                  .stream()
                  .mapToInt(item -> item.getStack().getCount())
                  .sum();
          context.assertTrue(!eldest.isAlive(), "the stake ends it");
          context.assertTrue(ash >= 2 && ash <= 4, "it leaves two to four ash, not " + ash);
          context.killAllEntities(ItemEntity.class);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 200)
  public void theBladeHealsItsWielderAndTheFullSetAddsHealth(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, BESIDE);
    final PigEntity pig = context.spawnMob(EntityType.PIG, new BlockPos(4, 2, 4));
    pig.setAiDisabled(true);
    final var settings = ServerConfigService.current().ritual();
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final float plain = hunter.getMaxHealth();
          hunter.setStackInHand(Hand.MAIN_HAND, new ItemStack(EldestSpoils.BLADE));
          hunter.setHealth(10.0F);
          pig.damage(context.getWorld().getDamageSources().playerAttack(hunter), 5.0F);
          final float dealt = pig.getMaxHealth() - pig.getHealth();
          final float healed = dealt * settings.bladeLifeSteal() / 100.0F;
          context.assertTrue(dealt > 0.0F, "the blow lands");
          context.assertTrue(
              Math.abs(hunter.getHealth() - (10.0F + healed)) < 0.01F,
              "the blade gives back " + healed + ", health is " + hunter.getHealth());
          hunter.equipStack(EquipmentSlot.HEAD, new ItemStack(EldestSpoils.CROWN));
          hunter.equipStack(EquipmentSlot.CHEST, new ItemStack(EldestSpoils.MANTLE));
          hunter.equipStack(EquipmentSlot.LEGS, new ItemStack(EldestSpoils.GREAVES));
          hunter.equipStack(EquipmentSlot.FEET, new ItemStack(EldestSpoils.BOOTS));
          context.runAtTick(
              TestPlayers.READY + 25,
              () -> {
                context.assertTrue(
                    hunter.getMaxHealth() == plain + settings.setHealth(),
                    "the full set raises " + plain + " to " + hunter.getMaxHealth());
                hunter.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
              });
          context.runAtTick(
              TestPlayers.READY + 50,
              () -> {
                context.assertTrue(
                    hunter.getMaxHealth() == plain, "putting the blade away takes it back");
                TestPlayers.finish(context, hunter);
              });
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 200)
  public void aTameBatBelongsToWhoeverSetsItFreeAndComesBackToThem(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.human(context, BESIDE);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final ItemStack stack = new ItemStack(EldestSpoils.TAME_BAT);
          owner.setStackInHand(Hand.MAIN_HAND, stack);
          context.useStackOnBlock(owner, stack, new BlockPos(4, 1, 4), Direction.UP);
          final List<PetBatEntity> bats =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      PetBatEntity.class, context.getTestBox().expand(1.0), bat -> true);
          bats.forEach(bat -> bat.setAiDisabled(true));
          context.assertTrue(bats.size() == 1, "one bat is set free, not " + bats.size());
          final PetBatEntity bat = bats.getFirst();
          context.assertTrue(owner.getUuid().equals(bat.owner()), "it belongs to who freed it");
          context.assertTrue(stack.isEmpty(), "the item is used up");
          context.assertTrue(
              bat.isInvulnerableTo(context.getWorld().getDamageSources().generic()),
              "nothing ordinary can hurt it");
          bat.interact(owner, Hand.MAIN_HAND);
          context.assertTrue(bat.isAlive(), "it stays out unless its owner sneaks");
          owner.setSneaking(true);
          bat.interact(owner, Hand.MAIN_HAND);
          context.assertTrue(bat.isRemoved(), "its sneaking owner picks it up");
          context.assertTrue(
              owner.getInventory().count(EldestSpoils.TAME_BAT) == 1, "and gets the item back");
          TestPlayers.finish(context, owner);
        });
  }
}
