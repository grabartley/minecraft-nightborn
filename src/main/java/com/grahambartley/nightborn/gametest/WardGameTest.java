package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.FeedingSettings;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.config.WardSettings;
import com.grahambartley.nightborn.decor.WallDecorBlock;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.gear.GarlicStringBlock;
import com.grahambartley.nightborn.gear.GearBlocks;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.ward.CrucifixShapes;
import com.grahambartley.nightborn.ward.WardTags;
import com.grahambartley.nightborn.ward.Wards;
import com.grahambartley.nightborn.weakened.Weakened;
import com.grahambartley.nightborn.weakened.WeakenedSources;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class WardGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BITES = "ward-bites";
  static final int SCANNED = 25;
  static final String SHORT_RANGE = "ward-short-range";
  static final int SHORT = 3;

  private static NightbornConfig beforeBites = NightbornConfig.DEFAULTS;

  @BeforeBatch(batchId = BITES)
  public void pvpOnAndNoStruggling(final ServerWorld world) {
    BatchPvp.set(world.getServer(), true);
    beforeBites = ServerConfigService.current();
    ServerConfigService.update(
        world.getServer(), beforeBites.with(FeedingSettings.RESIST_CHANCE, 0));
  }

  @AfterBatch(batchId = BITES)
  public void restore(final ServerWorld world) {
    BatchPvp.restore(world.getServer());
    ServerConfigService.update(world.getServer(), beforeBites);
  }

  private static NightbornConfig beforeShortRange = NightbornConfig.DEFAULTS;

  @BeforeBatch(batchId = SHORT_RANGE)
  public void shortRanges(final ServerWorld world) {
    beforeShortRange = ServerConfigService.current();
    ServerConfigService.update(
        world.getServer(),
        beforeShortRange
            .with(WardSettings.STRING_RANGE, SHORT)
            .with(WardSettings.CRUCIFIX_RANGE, SHORT)
            .with(WardSettings.SHAPE_RANGE, SHORT));
  }

  @AfterBatch(batchId = SHORT_RANGE)
  public void restoreRanges(final ServerWorld world) {
    ServerConfigService.update(world.getServer(), beforeShortRange);
  }

  @GameTest(templateName = ARENA, batchId = SHORT_RANGE, tickLimit = SCANNED + 5)
  public void aStringOutOfRangeDoesNothing(final TestContext context) {
    context.setBlockState(
        new BlockPos(4, 3, 1),
        GearBlocks.GARLIC_STRING.getDefaultState().with(GarlicStringBlock.HANGING, true));
    final VampireEntity near = still(context, new BlockPos(4, 2, 3));
    final VampireEntity far = still(context, new BlockPos(4, 2, 7));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(near, Wards.STRINGS), 15, "in range");
          context.assertEquals(strength(far, Wards.STRINGS), 0, "out of range");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = SHORT_RANGE, tickLimit = SCANNED + 5)
  public void aCrucifixOutOfRangeDoesNothing(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(1, 2, 4));
    hunter.setStackInHand(Hand.MAIN_HAND, new ItemStack(GearItems.GOLD_CRUCIFIX));
    final VampireEntity near = still(context, new BlockPos(3, 2, 4));
    final VampireEntity far = still(context, new BlockPos(6, 2, 4));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(near, Wards.CRUCIFIX), 60, "in range");
          context.assertEquals(strength(far, Wards.CRUCIFIX), 0, "out of range");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = SHORT_RANGE, tickLimit = SCANNED + 5)
  public void aCrucifixShapeOutOfRangeDoesNothing(final TestContext context) {
    build(context, new BlockPos(1, 2, 4), Blocks.OAK_PLANKS, Direction.SOUTH);
    final VampireEntity near = still(context, new BlockPos(3, 2, 2));
    final VampireEntity far = still(context, new BlockPos(7, 2, 4));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(near, Wards.SHAPE), 40, "in range");
          context.assertEquals(strength(far, Wards.SHAPE), 0, "out of range");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 25)
  public void breakingStringsLowersTheWeakening(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    for (int i = 0; i < 3; i++) {
      context.setBlockState(
          new BlockPos(1 + 2 * i, 3, 1),
          GearBlocks.GARLIC_STRING.getDefaultState().with(GarlicStringBlock.HANGING, true));
    }
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.STRINGS), 45, "three strings");
          context.setBlockState(new BlockPos(3, 3, 1), Blocks.AIR);
          context.setBlockState(new BlockPos(5, 3, 1), Blocks.AIR);
        });
    context.runAtTick(
        SCANNED + 20,
        () -> {
          context.assertEquals(strength(vampire, Wards.STRINGS), 15, "one string left");
          context.complete();
        });
  }

  @CustomTestProvider
  public Collection<TestFunction> moreStringsWeakenMoreUpToTheCap() {
    final List<TestFunction> tests = new ArrayList<>();
    final int[][] cases = {{1, 15}, {2, 30}, {3, 45}, {4, 45}};
    for (final int[] c : cases) {
      tests.add(
          test(
              "wardgametest.strings." + c[0],
              SCANNED + 5,
              context -> stringsWeaken(context, c[0], c[1])));
    }
    return tests;
  }

  private static void stringsWeaken(final TestContext context, final int strings, final int want) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    for (int i = 0; i < strings; i++) {
      context.setBlockState(
          new BlockPos(1 + 2 * i, 3, 1),
          GearBlocks.GARLIC_STRING.getDefaultState().with(GarlicStringBlock.HANGING, true));
    }
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.STRINGS), want, strings + " strings");
          context.assertEquals(strength(vampire, Wards.TOUCH), 0, "out of reach is not a touch");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 25)
  public void threeStringsAtOnceEarnStringsAttached(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    for (int i = 0; i < 2; i++) {
      context.setBlockState(
          new BlockPos(1 + 2 * i, 3, 1),
          GearBlocks.GARLIC_STRING.getDefaultState().with(GarlicStringBlock.HANGING, true));
    }
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertFalse(
              TestAdvancements.done(context, vampire, "strings_attached"), "two strings");
          context.assertTrue(
              TestAdvancements.progress(context, vampire, "strings_attached").isAnyObtained(),
              "two strings show as progress");
          context.setBlockState(
              new BlockPos(5, 3, 1),
              GearBlocks.GARLIC_STRING.getDefaultState().with(GarlicStringBlock.HANGING, true));
        });
    context.runAtTick(
        SCANNED + 20,
        () -> {
          context.assertTrue(
              TestAdvancements.done(context, vampire, "strings_attached"), "three strings");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void aStringOnAWallStaysUpAndWeakens(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    context.setBlockState(new BlockPos(1, 2, 2), Blocks.STONE);
    context.setBlockState(
        new BlockPos(1, 2, 3),
        GearBlocks.GARLIC_STRING.getDefaultState().with(GarlicStringBlock.FACING, Direction.SOUTH));
    context.runAtTick(
        SCANNED,
        () -> {
          context.expectBlock(GearBlocks.GARLIC_STRING, new BlockPos(1, 2, 3));
          context.assertEquals(strength(vampire, Wards.STRINGS), 15, "one string in range");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void aHungCrucifixWeakensLikeACrucifixShape(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    context.setBlockState(new BlockPos(1, 2, 2), Blocks.STONE);
    context.setBlockState(
        new BlockPos(1, 2, 3),
        GearBlocks.GOLD_WALL_CRUCIFIX
            .getDefaultState()
            .with(WallDecorBlock.FACING, Direction.SOUTH));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.SHAPE), 40, "a hung crucifix in sight");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void aWallHidesAHungCrucifix(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 5));
    context.setBlockState(new BlockPos(1, 2, 2), Blocks.STONE);
    context.setBlockState(
        new BlockPos(1, 2, 3),
        GearBlocks.GOLD_WALL_CRUCIFIX
            .getDefaultState()
            .with(WallDecorBlock.FACING, Direction.SOUTH));
    for (int x = 0; x <= 6; x++) {
      for (int y = 1; y <= 4; y++) {
        context.setBlockState(new BlockPos(x, y, 4), Blocks.STONE);
      }
    }
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.SHAPE), 0, "the wall blocks the sight");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aStringFallsWhenItsWallIsBroken(final TestContext context) {
    context.setBlockState(new BlockPos(1, 2, 2), Blocks.STONE);
    context.setBlockState(
        new BlockPos(1, 2, 3),
        GearBlocks.GARLIC_STRING.getDefaultState().with(GarlicStringBlock.FACING, Direction.SOUTH));
    context.setBlockState(new BlockPos(1, 2, 2), Blocks.AIR);
    context.expectBlock(Blocks.AIR, new BlockPos(1, 2, 3));
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void walkingThroughGarlicWeakensFarMoreThanAString(final TestContext context) {
    context.setBlockState(new BlockPos(4, 1, 4), Blocks.FARMLAND);
    context.setBlockState(new BlockPos(4, 2, 4), GearBlocks.GARLIC.getDefaultState());
    final VampireEntity vampire = walking(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.TOUCH), 80, "touching garlic");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void brushingAStringWeakensLikeTouchingGarlic(final TestContext context) {
    context.setBlockState(
        new BlockPos(4, 3, 4),
        GearBlocks.GARLIC_STRING.getDefaultState().with(GarlicStringBlock.HANGING, true));
    final VampireEntity vampire = walking(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.TOUCH), 80, "brushing a string");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void aVampireHoldingGarlicIsWeakened(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    vampire.setStackInHand(Hand.OFF_HAND, new ItemStack(GearItems.GARLIC));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.TOUCH), 80, "holding garlic");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void garlicNeverWeakensAHuman(final TestContext context) {
    context.setBlockState(new BlockPos(4, 1, 4), Blocks.FARMLAND);
    context.setBlockState(new BlockPos(4, 2, 4), GearBlocks.GARLIC.getDefaultState());
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    human.setStackInHand(Hand.MAIN_HAND, new ItemStack(GearItems.GARLIC));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertTrue(Weakened.sources(human).isEmpty(), "a human is never weakened");
          TestPlayers.finish(context, human);
        });
  }

  @CustomTestProvider
  public Collection<TestFunction> aHeldCrucifixWeakensAVampireThatSeesIt() {
    final List<TestFunction> tests = new ArrayList<>();
    for (final Hand hand : Hand.values()) {
      for (final ItemStack crucifix :
          List.of(
              new ItemStack(GearItems.GOLD_CRUCIFIX), new ItemStack(GearItems.SILVER_CRUCIFIX))) {
        tests.add(
            test(
                "wardgametest.crucifix." + hand.name().toLowerCase() + "." + crucifix.getItem(),
                SCANNED + 5,
                context -> crucifixWeakens(context, hand, crucifix.copy())));
      }
    }
    return tests;
  }

  private static void crucifixWeakens(
      final TestContext context, final Hand hand, final ItemStack crucifix) {
    final VampireEntity vampire = still(context, new BlockPos(2, 2, 4));
    final double free = vampire.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(6, 2, 4));
    hunter.setStackInHand(hand, crucifix);
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertTrue(crucifix.isIn(WardTags.CRUCIFIXES), "the crucifix is tagged");
          context.assertEquals(strength(vampire, Wards.CRUCIFIX), 60, "a crucifix in sight");
          context.assertTrue(
              vampire.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) < free * 0.8,
              "the vampire is plainly slowed");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void aHeldCrucifixWeakensAndSlowsAVampirePlayer(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final double free = vampire.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(6, 2, 4));
    hunter.setStackInHand(Hand.OFF_HAND, new ItemStack(GearItems.SILVER_CRUCIFIX));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.CRUCIFIX), 60, "a crucifix in sight");
          context.assertTrue(
              vampire.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) < free * 0.8,
              "the vampire player is slowed");
          TestPlayers.finish(context, vampire, hunter);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void aCrucifixInAVampiresHandWardsNoOne(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity holder = TestPlayers.vampire(context, new BlockPos(6, 2, 4));
    holder.setStackInHand(Hand.MAIN_HAND, new ItemStack(GearItems.GOLD_CRUCIFIX));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.CRUCIFIX), 0, "a vampire cannot wield it");
          TestPlayers.finish(context, holder);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void aWallHidesTheCrucifix(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(6, 2, 4));
    hunter.setStackInHand(Hand.MAIN_HAND, new ItemStack(GearItems.GOLD_CRUCIFIX));
    for (int z = 2; z <= 6; z++) {
      context.setBlockState(new BlockPos(4, 2, z), Blocks.STONE);
      context.setBlockState(new BlockPos(4, 3, z), Blocks.STONE);
    }
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.CRUCIFIX), 0, "it cannot see the crucifix");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void faithMakesTheCrucifixStronger(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(6, 2, 4));
    hunter.getAttributeInstance(HunterAttributes.FAITH).setBaseValue(2.0);
    hunter.setStackInHand(Hand.MAIN_HAND, new ItemStack(GearItems.GOLD_CRUCIFIX));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.CRUCIFIX), 90, "two points of faith");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireMobCannotBiteACrucifixHolder(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final VillagerEntity warded = context.spawnEntity(EntityType.VILLAGER, new BlockPos(3, 2, 4));
    final VillagerEntity bare = context.spawnEntity(EntityType.VILLAGER, new BlockPos(5, 2, 4));
    warded.setAiDisabled(true);
    bare.setAiDisabled(true);
    warded.setStackInHand(Hand.OFF_HAND, new ItemStack(GearItems.SILVER_CRUCIFIX));
    vampire.setTarget(warded);
    context.assertFalse(vampire.tryAttack(warded), "the bite is held back");
    context.assertEquals(warded.getHealth(), warded.getMaxHealth(), "the holder is unhurt");
    vampire.setTarget(bare);
    context.assertTrue(vampire.tryAttack(bare), "without a crucifix the bite lands");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BITES, tickLimit = TestPlayers.READY + 40)
  public void aVampireCannotBiteACrucifixHolder(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(4, 2, 4));
    hunter.setStackInHand(Hand.OFF_HAND, new ItemStack(GearItems.GOLD_CRUCIFIX));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= TestPlayers.READY) {
            hold(context, vampire, hunter);
          }
        });
    context.runAtTick(
        TestPlayers.READY + 30,
        () -> {
          context.assertFalse(FeedingSessions.isFeeding(vampire), "the bite is refused");
          context.assertEquals(hunter.getHealth(), hunter.getMaxHealth(), "the holder is unhurt");
          TestPlayers.finish(context, vampire, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BITES, tickLimit = TestPlayers.READY + 60)
  public void raisingACrucifixBreaksABite(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    context.runAtTick(TestPlayers.READY, () -> hold(context, vampire, human));
    context.runAtTick(
        TestPlayers.READY + 5,
        () -> {
          context.assertTrue(FeedingSessions.isFeeding(vampire), "the bite takes hold");
          human.setStackInHand(Hand.MAIN_HAND, new ItemStack(GearItems.GOLD_CRUCIFIX));
        });
    context.runAtTick(
        TestPlayers.READY + 8,
        () -> {
          context.assertFalse(FeedingSessions.isFeeding(vampire), "the crucifix breaks the bite");
          TestPlayers.finish(context, vampire, human);
        });
  }

  @CustomTestProvider
  public Collection<TestFunction> aCrucifixShapeIsFoundInAnyFacing() {
    final List<TestFunction> tests = new ArrayList<>();
    for (final Block material : List.of(Blocks.OAK_PLANKS, Blocks.SPRUCE_LOG, Blocks.GOLD_BLOCK)) {
      for (final Direction arms : List.of(Direction.EAST, Direction.SOUTH)) {
        tests.add(
            test(
                "wardgametest.shape." + material.asItem() + "." + arms.asString(),
                20,
                context -> shapeFound(context, material, arms)));
      }
    }
    return tests;
  }

  private static void shapeFound(
      final TestContext context, final Block material, final Direction arms) {
    final BlockPos centre = new BlockPos(4, 2, 4);
    build(context, centre, material, arms);
    final CrucifixShapes shapes = CrucifixShapes.get(context.getWorld());
    context.assertTrue(shapes.contains(context.getAbsolutePos(centre)), "the shape is found");
    context.setBlockState(centre.offset(arms), Blocks.AIR);
    context.assertFalse(
        shapes.contains(context.getAbsolutePos(centre)), "an arm taken away breaks it");
    context.setBlockState(centre.offset(arms), material);
    context.assertTrue(shapes.contains(context.getAbsolutePos(centre)), "put back, it is found");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void stoneIsNotACrucifix(final TestContext context) {
    final BlockPos centre = new BlockPos(4, 2, 4);
    build(context, centre, Blocks.STONE, Direction.EAST);
    context.assertFalse(
        CrucifixShapes.get(context.getWorld()).contains(context.getAbsolutePos(centre)),
        "stone is not in the crucifix materials tag");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void aCrucifixShapeWeakensAVampireThatSeesIt(final TestContext context) {
    build(context, new BlockPos(1, 2, 4), Blocks.OAK_PLANKS, Direction.SOUTH);
    final VampireEntity vampire = still(context, new BlockPos(6, 2, 4));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.SHAPE), 40, "a crucifix shape in sight");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = SCANNED + 5)
  public void aWallHidesTheCrucifixShape(final TestContext context) {
    build(context, new BlockPos(1, 2, 4), Blocks.OAK_PLANKS, Direction.SOUTH);
    for (int z = 1; z <= 7; z++) {
      context.setBlockState(new BlockPos(3, 2, z), Blocks.STONE);
      context.setBlockState(new BlockPos(3, 3, z), Blocks.STONE);
    }
    final VampireEntity vampire = still(context, new BlockPos(6, 2, 4));
    context.runAtTick(
        SCANNED,
        () -> {
          context.assertEquals(strength(vampire, Wards.SHAPE), 0, "the shape is out of sight");
          context.complete();
        });
  }

  private static void build(
      final TestContext context,
      final BlockPos centre,
      final Block material,
      final Direction arms) {
    context.setBlockState(centre.down(), material);
    context.setBlockState(centre, material);
    context.setBlockState(centre.up(), material);
    context.setBlockState(centre.offset(arms), material);
    context.setBlockState(centre.offset(arms.getOpposite()), material);
  }

  private static VampireEntity still(final TestContext context, final BlockPos at) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, at);
    vampire.setAiDisabled(true);
    return vampire;
  }

  private static VampireEntity walking(final TestContext context, final BlockPos at) {
    final VampireEntity vampire = still(context, at);
    context.runAtEveryTick(
        () ->
            vampire.move(
                MovementType.SELF, new Vec3d((context.getTick() % 2) * 0.02 - 0.01, 0, 0)));
    return vampire;
  }

  private static int strength(final LivingEntity vampire, final String source) {
    final WeakenedSources.Source found = Weakened.sources(vampire).sources().get(source);
    return found == null ? 0 : found.strength();
  }

  private static void hold(
      final TestContext context, final ServerPlayerEntity vampire, final LivingEntity victim) {
    vampire.setSneaking(true);
    UseEntityCallback.EVENT
        .invoker()
        .interact(vampire, context.getWorld(), Hand.MAIN_HAND, victim, null);
  }

  private static TestFunction test(
      final String name, final int tickLimit, final Consumer<TestContext> body) {
    return new TestFunction("defaultBatch", name, ARENA, tickLimit, 0L, true, body);
  }
}
