package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.clothing.ClothingFabric;
import com.grahambartley.nightborn.clothing.ClothingItem;
import com.grahambartley.nightborn.clothing.ClothingItems;
import com.grahambartley.nightborn.clothing.ClothingPiece;
import com.grahambartley.nightborn.config.ClothingSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ClothingGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "clothing";
  static final String NIGHT_BATCH = "clothing-night";
  static final String NOON_BATCH = "clothing-noon";
  static final String SETTINGS_BATCH = "clothing-settings";
  static final BlockPos SPOT = new BlockPos(4, 2, 4);
  static final int WORN = 5;
  static final int NIGHT_VISION_CHECK = 10;
  static final int RED = 0xB02E26;
  static final double EPSILON = 1.0E-6;

  private record Power(String enchantment, EquipmentSlot slot, ClothingPiece piece) {}

  private record Raise(
      String enchantment,
      ClothingPiece piece,
      int level,
      RegistryEntry<EntityAttribute> attribute,
      double by) {}

  private static final List<Power> POWERS =
      List.of(
          new Power("nocturne", EquipmentSlot.HEAD, ClothingPiece.TOP_HAT),
          new Power("batwing", EquipmentSlot.CHEST, ClothingPiece.HIGH_COLLARED_CAPE),
          new Power("gliding_step", EquipmentSlot.LEGS, ClothingPiece.TROUSERS),
          new Power("hush", EquipmentSlot.FEET, ClothingPiece.RIDING_BOOTS));

  private static final List<Raise> RAISES =
      List.of(
          new Raise(
              "batwing",
              ClothingPiece.HIGH_COLLARED_CAPE,
              3,
              EntityAttributes.GENERIC_SAFE_FALL_DISTANCE,
              3.0),
          new Raise(
              "gliding_step",
              ClothingPiece.LONG_SKIRT,
              1,
              EntityAttributes.GENERIC_STEP_HEIGHT,
              0.4),
          new Raise(
              "hush", ClothingPiece.BUCKLED_SHOES, 2, EntityAttributes.PLAYER_SNEAKING_SPEED, 0.2));

  @BeforeBatch(batchId = NIGHT_BATCH)
  public void pinNight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = NIGHT_BATCH)
  public void unpinNight(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = NOON_BATCH)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = NOON_BATCH)
  public void unpinNoon(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = SETTINGS_BATCH)
  public void retuneClothing(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(ClothingSettings.NOCTURNE, false)
            .with(ClothingSettings.BATWING_BLOCKS, 2));
  }

  @AfterBatch(batchId = SETTINGS_BATCH)
  public void restoreClothing(final ServerWorld world) {
    WorldClock.release(world);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(ClothingSettings.NOCTURNE, ClothingSettings.NOCTURNE.defaultValue())
            .with(ClothingSettings.BATWING_BLOCKS, ClothingSettings.BATWING_BLOCKS.defaultValue()));
  }

  @GameTest(templateName = ARENA, batchId = SETTINGS_BATCH, tickLimit = WORN + 10)
  public void batwingFollowsTheServerSetting(final TestContext context) {
    wearingRaisesTheAttribute(
        context,
        new Raise(
            "batwing",
            ClothingPiece.FROCK_COAT,
            3,
            EntityAttributes.GENERIC_SAFE_FALL_DISTANCE,
            6.0));
  }

  @GameTest(templateName = ARENA, batchId = SETTINGS_BATCH, tickLimit = NIGHT_VISION_CHECK + 10)
  public void nocturneRestsWhenTheServerTurnsItOff(final TestContext context) {
    final ServerPlayerEntity human = wearNocturne(context);
    context.runAtTick(
        NIGHT_VISION_CHECK,
        () -> {
          context.assertFalse(
              human.hasStatusEffect(StatusEffects.NIGHT_VISION), "nocturne is turned off");
          TestPlayers.finish(context, human);
        });
  }

  @CustomTestProvider
  public Collection<TestFunction> perPiece() {
    final List<TestFunction> tests = new ArrayList<>();
    for (final ClothingPiece piece : ClothingPiece.values()) {
      tests.add(test("craftedfromitsrecipe", piece, ClothingGameTest::craftedFromItsRecipe));
      tests.add(test("dyedonlyifdyeable", piece, ClothingGameTest::dyedOnlyIfDyeable));
      tests.add(test("mendedwithitsfabric", piece, ClothingGameTest::mendedWithItsFabric));
      tests.add(test("takesarmourenchantments", piece, ClothingGameTest::takesArmourEnchantments));
      tests.add(test("takesitsslotpowersonly", piece, ClothingGameTest::takesItsSlotPowersOnly));
      tests.add(test("worninitsslot", piece, ClothingGameTest::wornInItsSlot));
    }
    return tests;
  }

  @CustomTestProvider
  public Collection<TestFunction> perPower() {
    return POWERS.stream()
        .map(
            power ->
                new TestFunction(
                    BATCH,
                    "clothinggametest.offeredbytheenchantingtable." + power.enchantment(),
                    ARENA,
                    20,
                    0L,
                    true,
                    context -> offeredByTheEnchantingTable(context, power)))
        .toList();
  }

  @CustomTestProvider
  public Collection<TestFunction> perRaise() {
    return RAISES.stream()
        .map(
            raise ->
                new TestFunction(
                    BATCH,
                    "clothinggametest.wearingraisestheattribute." + raise.enchantment(),
                    ARENA,
                    WORN + 10,
                    0L,
                    true,
                    context -> wearingRaisesTheAttribute(context, raise)))
        .toList();
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = NIGHT_VISION_CHECK + 10)
  public void nocturneLetsTheWearerSeeAtNight(final TestContext context) {
    final ServerPlayerEntity human = wearNocturne(context);
    context.runAtTick(
        NIGHT_VISION_CHECK,
        () -> {
          final StatusEffectInstance sight = human.getStatusEffect(StatusEffects.NIGHT_VISION);
          context.assertTrue(sight != null, "a nocturne hat sees by night");
          context.assertTrue(
              sight.isAmbient() && !sight.shouldShowParticles() && !sight.shouldShowIcon(),
              "night sight shows no swirls and no icon, as a vampire's does");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = NOON_BATCH, tickLimit = NIGHT_VISION_CHECK + 10)
  public void nocturneDoesNothingByDay(final TestContext context) {
    final ServerPlayerEntity human = wearNocturne(context);
    context.runAtTick(
        NIGHT_VISION_CHECK,
        () -> {
          context.assertFalse(
              human.hasStatusEffect(StatusEffects.NIGHT_VISION), "nocturne rests by day");
          TestPlayers.finish(context, human);
        });
  }

  private static ServerPlayerEntity wearNocturne(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final ItemStack hat = new ItemStack(ClothingItems.of(ClothingPiece.TRICORNE));
    hat.addEnchantment(enchantment(context, "nocturne"), 1);
    human.equipStack(EquipmentSlot.HEAD, hat);
    return human;
  }

  private static void craftedFromItsRecipe(final TestContext context, final ClothingPiece piece) {
    final ServerWorld world = context.getWorld();
    final RecipeEntry<?> entry =
        world.getRecipeManager().get(Identifier.of(Nightborn.MOD_ID, piece.id())).orElseThrow();
    final ShapedRecipe recipe = (ShapedRecipe) entry.value();
    final List<ItemStack> grid = new ArrayList<>();
    for (final Ingredient ingredient : recipe.getIngredients()) {
      grid.add(ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getMatchingStacks()[0].copy());
    }
    final CraftingRecipeInput input =
        CraftingRecipeInput.create(recipe.getWidth(), recipe.getHeight(), grid);
    final ItemStack made =
        world
            .getRecipeManager()
            .getFirstMatch(RecipeType.CRAFTING, input, world)
            .orElseThrow()
            .value()
            .craft(input, world.getRegistryManager());
    context.assertTrue(
        made.isOf(ClothingItems.of(piece)), piece.id() + " is what its grid crafts, got " + made);
    context.complete();
  }

  private static void dyedOnlyIfDyeable(final TestContext context, final ClothingPiece piece) {
    final ServerWorld world = context.getWorld();
    final CraftingRecipeInput input =
        CraftingRecipeInput.create(
            2, 1, List.of(new ItemStack(ClothingItems.of(piece)), new ItemStack(Items.RED_DYE)));
    final ItemStack dyed =
        world
            .getRecipeManager()
            .getFirstMatch(RecipeType.CRAFTING, input, world)
            .map(match -> match.value().craft(input, world.getRegistryManager()))
            .orElse(ItemStack.EMPTY);
    context.assertEquals(
        piece.dyeable(), dyed.isOf(ClothingItems.of(piece)), piece.id() + " takes dye");
    if (piece.dyeable()) {
      context.assertEquals(
          RED, dyed.get(DataComponentTypes.DYED_COLOR).rgb(), "red dye makes it red");
    }
    context.complete();
  }

  private static void mendedWithItsFabric(final TestContext context, final ClothingPiece piece) {
    final ClothingItem item = ClothingItems.of(piece);
    final ItemStack worn = new ItemStack(item);
    final boolean cloth = piece.fabric() == ClothingFabric.CLOTH;
    context.assertEquals(
        cloth,
        item.canRepair(worn, new ItemStack(Items.BLACK_WOOL)),
        piece.id() + " mends with wool");
    context.assertEquals(
        !cloth,
        item.canRepair(worn, new ItemStack(Items.LEATHER)),
        piece.id() + " mends with leather");
    context.complete();
  }

  private static void takesArmourEnchantments(
      final TestContext context, final ClothingPiece piece) {
    final ItemStack stack = new ItemStack(ClothingItems.of(piece));
    for (final RegistryKey<Enchantment> key :
        List.of(Enchantments.PROTECTION, Enchantments.UNBREAKING, Enchantments.MENDING)) {
      context.assertTrue(
          entry(context, key).value().isAcceptableItem(stack),
          piece.id() + " takes " + key.getValue());
    }
    context.complete();
  }

  private static void takesItsSlotPowersOnly(final TestContext context, final ClothingPiece piece) {
    final ItemStack stack = new ItemStack(ClothingItems.of(piece));
    for (final Power power : POWERS) {
      final Enchantment enchantment = enchantment(context, power.enchantment()).value();
      final boolean fits = power.slot() == piece.type().getEquipmentSlot();
      context.assertEquals(
          fits, enchantment.isAcceptableItem(stack), piece.id() + " takes " + power.enchantment());
      context.assertEquals(
          fits,
          enchantment.isPrimaryItem(stack),
          piece.id() + " is offered " + power.enchantment());
    }
    final Enchantment respiration = entry(context, Enchantments.RESPIRATION).value();
    context.assertEquals(
        piece.type().getEquipmentSlot() == EquipmentSlot.HEAD,
        respiration.isAcceptableItem(stack),
        "vanilla slot enchantments follow the slot");
    context.complete();
  }

  private static void wornInItsSlot(final TestContext context, final ClothingPiece piece) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final ItemStack held = new ItemStack(ClothingItems.of(piece));
    human.setStackInHand(Hand.MAIN_HAND, held);
    ClothingItems.of(piece).use(context.getWorld(), human, Hand.MAIN_HAND);
    final EquipmentSlot slot = piece.type().getEquipmentSlot();
    context.assertTrue(
        human.getEquippedStack(slot).isOf(ClothingItems.of(piece)),
        piece.id() + " is put on in the " + slot.getName() + " slot");
    context.assertTrue(human.getMainHandStack().isEmpty(), "the hand is left empty");
    context.runAtTick(
        WORN,
        () -> {
          context.assertEquals(
              (double) ClothingItems.of(piece).getProtection(),
              human.getAttributeValue(EntityAttributes.GENERIC_ARMOR),
              piece.id() + " protects like leather");
          TestPlayers.finish(context, human);
        });
  }

  private static void offeredByTheEnchantingTable(final TestContext context, final Power power) {
    final RegistryEntry<Enchantment> entry = enchantment(context, power.enchantment());
    context.assertTrue(
        entry.isIn(EnchantmentTags.IN_ENCHANTING_TABLE), power.enchantment() + " is in the table");
    context.assertFalse(
        entry.isIn(EnchantmentTags.TREASURE), power.enchantment() + " is not a treasure");
    context.assertTrue(
        entry.value().isAcceptableItem(new ItemStack(ClothingItems.of(power.piece()))),
        "an anvil puts " + power.enchantment() + " on " + power.piece().id());
    context.complete();
  }

  private static void wearingRaisesTheAttribute(final TestContext context, final Raise raise) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final double before = human.getAttributeValue(raise.attribute());
    final ItemStack stack = new ItemStack(ClothingItems.of(raise.piece()));
    stack.addEnchantment(enchantment(context, raise.enchantment()), raise.level());
    human.equipStack(raise.piece().type().getEquipmentSlot(), stack);
    context.runAtTick(
        WORN,
        () -> {
          context.assertTrue(
              Math.abs(before + raise.by() - human.getAttributeValue(raise.attribute())) < EPSILON,
              raise.enchantment()
                  + " "
                  + raise.level()
                  + " raises "
                  + raise.attribute().getIdAsString());
          human.equipStack(raise.piece().type().getEquipmentSlot(), ItemStack.EMPTY);
        });
    context.runAtTick(
        WORN + 2,
        () -> {
          context.assertEquals(
              before, human.getAttributeValue(raise.attribute()), "taking it off undoes it");
          TestPlayers.finish(context, human);
        });
  }

  private static TestFunction test(
      final String name,
      final ClothingPiece piece,
      final BiConsumer<TestContext, ClothingPiece> body) {
    return new TestFunction(
        BATCH,
        "clothinggametest." + name + "." + piece.id(),
        ARENA,
        WORN + 10,
        0L,
        true,
        context -> body.accept(context, piece));
  }

  private static RegistryEntry<Enchantment> enchantment(
      final TestContext context, final String name) {
    return entry(
        context, RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(Nightborn.MOD_ID, name)));
  }

  private static RegistryEntry<Enchantment> entry(
      final TestContext context, final RegistryKey<Enchantment> key) {
    return context
        .getWorld()
        .getRegistryManager()
        .get(RegistryKeys.ENCHANTMENT)
        .getEntry(key)
        .orElseThrow();
  }
}
