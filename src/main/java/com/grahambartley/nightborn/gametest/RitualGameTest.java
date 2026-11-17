package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.ritual.AltarPhase;
import com.grahambartley.nightborn.ritual.RitualAltarBlock;
import com.grahambartley.nightborn.ritual.RitualAltarBlockEntity;
import com.grahambartley.nightborn.ritual.RitualBlocks;
import com.grahambartley.nightborn.ritual.RitualItems;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class RitualGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String NIGHT_BATCH = "ritualNight";
  static final String NOON_BATCH = "ritualNoon";
  static final BlockPos ALTAR = new BlockPos(4, 2, 4);
  static final BlockPos BESIDE = new BlockPos(4, 2, 6);

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

  private static RitualAltarBlockEntity altar(final TestContext context) {
    context.setBlockState(ALTAR, RitualBlocks.ALTAR.getDefaultState());
    return context.getBlockEntity(ALTAR);
  }

  private static void use(
      final TestContext context, final ServerPlayerEntity player, final ItemStack held) {
    player.setStackInHand(Hand.MAIN_HAND, held);
    context.useBlock(ALTAR, player);
  }

  private static void placeAll(final TestContext context, final ServerPlayerEntity player) {
    for (final Item relic :
        List.of(
            RitualItems.CASTLE_SIGNET,
            RitualItems.CRYPT_RELIQUARY,
            RitualItems.MANOR_LOCKET,
            RitualItems.LAIR_FANG)) {
      use(context, player, new ItemStack(relic));
    }
  }

  private static AltarPhase phase(final TestContext context) {
    return context.getBlockState(ALTAR).get(RitualAltarBlock.PHASE);
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 400)
  public void aHumanWithEveryRelicSummonsTheEldestAtNight(final TestContext context) {
    final RitualAltarBlockEntity altar = altar(context);
    final BlockPos candle = new BlockPos(2, 2, 2);
    context.setBlockState(candle, Blocks.CANDLE.getDefaultState().with(CandleBlock.LIT, true));
    final ServerPlayerEntity human = TestPlayers.human(context, BESIDE);
    final int buildUp = ServerConfigService.current().ritual().buildUpTicks();
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          placeAll(context, human);
          context.assertTrue(human.getMainHandStack().isEmpty(), "the last relic left the hand");
          context.assertTrue(altar.relics().size() == 4, "four relics lie on the altar");
          use(context, human, ItemStack.EMPTY);
          context.assertTrue(
              phase(context) == AltarPhase.RITUAL_ACTIVE, "the ritual runs: " + phase(context));
          context.assertTrue(
              context.getBlockState(ALTAR).getLuminance() > 0, "the altar glows while it runs");
          use(context, human, ItemStack.EMPTY);
          context.assertTrue(altar.relics().size() == 4, "a running ritual gives no relic back");
        });
    context.runAtTick(
        TestPlayers.READY + buildUp + 5L,
        () -> {
          final List<EldestEntity> summoned =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      EldestEntity.class, context.getTestBox().expand(1.0), e -> true);
          summoned.forEach(eldest -> eldest.setAiDisabled(true));
          context.assertTrue(summoned.size() == 1, "one Eldest comes, not " + summoned.size());
          final EldestEntity eldest = summoned.getFirst();
          context.assertTrue(
              eldest.getBlockPos().getManhattanDistance(context.getAbsolutePos(ALTAR)) <= 3,
              "it stands beside the altar");
          context.assertTrue(
              eldest.stageId().equals(EldestEntity.STAGE), "it is staged as " + eldest.stageId());
          context.assertTrue(altar.relics().isEmpty(), "the ritual used up the relics");
          context.assertTrue(phase(context) == AltarPhase.IDLE, "the altar is ready again");
          context.assertTrue(
              !context.getBlockState(candle).get(CandleBlock.LIT),
              "the candle guttered during the build-up");
          eldest.discard();
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 200)
  public void aVampireCannotStartTheRitual(final TestContext context) {
    final RitualAltarBlockEntity altar = altar(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, BESIDE);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          placeAll(context, vampire);
          use(context, vampire, ItemStack.EMPTY);
          context.assertTrue(!altar.active(), "a vampire's hand starts nothing");
          context.assertTrue(altar.relics().size() == 4, "the relics stay on the altar");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 200)
  public void anIncompleteSetStartsNothingAndComesBackOff(final TestContext context) {
    final RitualAltarBlockEntity altar = altar(context);
    final ServerPlayerEntity human = TestPlayers.human(context, BESIDE);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          use(context, human, new ItemStack(RitualItems.CASTLE_SIGNET));
          use(context, human, new ItemStack(RitualItems.LAIR_FANG));
          use(context, human, new ItemStack(RitualItems.LAIR_FANG));
          context.assertTrue(altar.relics().size() == 2, "a second fang is not taken");
          context.assertTrue(
              phase(context) == AltarPhase.RELICS_PLACED, "the altar shows its relics");
          use(context, human, ItemStack.EMPTY);
          context.assertTrue(!altar.active(), "two relics start nothing");
          context.assertTrue(
              human.getMainHandStack().isOf(RitualItems.LAIR_FANG), "the last relic comes back");
          context.assertTrue(altar.relics().size() == 1, "one relic is left");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = NOON_BATCH, tickLimit = 200)
  public void theRitualDoesNotStartByDay(final TestContext context) {
    final RitualAltarBlockEntity altar = altar(context);
    final ServerPlayerEntity human = TestPlayers.human(context, BESIDE);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          placeAll(context, human);
          use(context, human, ItemStack.EMPTY);
          context.assertTrue(!altar.active(), "daylight starts nothing");
          context.assertTrue(altar.relics().size() == 4, "the relics stay on the altar");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void breakingTheAltarDropsItsRelics(final TestContext context) {
    final RitualAltarBlockEntity altar = altar(context);
    altar.place(new ItemStack(RitualItems.MANOR_LOCKET));
    context.setBlockState(ALTAR, Blocks.AIR);
    context.runAtTick(
        2,
        () -> {
          final List<ItemEntity> dropped =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      ItemEntity.class,
                      context.getTestBox(),
                      item -> item.getStack().isOf(RitualItems.MANOR_LOCKET));
          context.assertTrue(dropped.size() == 1, "the locket drops, found " + dropped.size());
          dropped.forEach(ItemEntity::discard);
          context.complete();
        });
  }
}
