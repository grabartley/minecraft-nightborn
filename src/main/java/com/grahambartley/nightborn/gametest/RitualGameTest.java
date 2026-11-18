package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.decor.ChaliceBlock;
import com.grahambartley.nightborn.decor.ChaliceBlood;
import com.grahambartley.nightborn.decor.ChaliceContents;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.ritual.AltarPhase;
import com.grahambartley.nightborn.ritual.RitualAltarBlock;
import com.grahambartley.nightborn.ritual.RitualAltarBlockEntity;
import com.grahambartley.nightborn.ritual.RitualBlocks;
import com.grahambartley.nightborn.ritual.RitualItems;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
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

  private static void setAndFill(
      final TestContext context, final ServerPlayerEntity player, final int bottles) {
    use(context, player, new ItemStack(DecorBlocks.CHALICE));
    for (int bottle = 0; bottle < bottles; bottle++) {
      use(context, player, BloodBottleItem.of(BloodQuality.ANIMAL));
    }
  }

  private static ChaliceContents chalice(final RitualAltarBlockEntity altar) {
    return altar.chalice().orElse(ChaliceContents.EMPTY);
  }

  private static AltarPhase phase(final TestContext context) {
    return context.getBlockState(ALTAR).get(RitualAltarBlock.PHASE);
  }

  private static List<EldestEntity> summoned(final TestContext context) {
    final List<EldestEntity> found =
        context
            .getWorld()
            .getEntitiesByClass(EldestEntity.class, context.getTestBox().expand(1.0), e -> true);
    found.forEach(eldest -> eldest.setAiDisabled(true));
    return found;
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 400)
  public void aHumanWithAFullChaliceSummonsTheEldestAtNight(final TestContext context) {
    final RitualAltarBlockEntity altar = altar(context);
    final BlockPos candle = new BlockPos(2, 2, 2);
    context.setBlockState(candle, Blocks.CANDLE.getDefaultState().with(CandleBlock.LIT, true));
    final ServerPlayerEntity human = TestPlayers.human(context, BESIDE);
    final int buildUp = ServerConfigService.current().ritual().buildUpTicks();
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          setAndFill(context, human, ChaliceContents.CAPACITY);
          context.assertTrue(chalice(altar).full(), "seven bottles fill the chalice");
          context.assertTrue(phase(context) == AltarPhase.READY, "a full chalice readies it");
          use(context, human, ItemStack.EMPTY);
          context.assertTrue(
              phase(context) == AltarPhase.RITUAL_ACTIVE, "the ritual runs: " + phase(context));
          context.assertTrue(
              context.getBlockState(ALTAR).getLuminance() > 0, "the altar glows while it runs");
          human.setSneaking(true);
          use(context, human, ItemStack.EMPTY);
          human.setSneaking(false);
          context.assertTrue(altar.chalice().isPresent(), "a running ritual keeps its chalice");
        });
    context.runAtTick(
        TestPlayers.READY + buildUp + 5L,
        () -> {
          final List<EldestEntity> eldest = summoned(context);
          context.assertTrue(eldest.size() == 1, "one Eldest comes, not " + eldest.size());
          context.assertTrue(
              eldest.getFirst().getBlockPos().getManhattanDistance(context.getAbsolutePos(ALTAR))
                  <= 3,
              "it stands beside the altar");
          context.assertTrue(
              altar.chalice().equals(Optional.of(ChaliceContents.EMPTY)),
              "the ritual drank the chalice dry and left it: " + altar.chalice());
          context.assertTrue(phase(context) == AltarPhase.IDLE, "the altar is ready again");
          context.assertTrue(
              !context.getBlockState(candle).get(CandleBlock.LIT),
              "the candle guttered during the build-up");
          eldest.forEach(EldestEntity::discard);
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 400)
  public void aVampireWhoCallsTheEldestBecomesItsFoe(final TestContext context) {
    final RitualAltarBlockEntity altar = altar(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, BESIDE);
    final int buildUp = ServerConfigService.current().ritual().buildUpTicks();
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          setAndFill(context, vampire, ChaliceContents.CAPACITY);
          use(context, vampire, ItemStack.EMPTY);
          context.assertTrue(altar.active(), "a vampire can start the ritual");
        });
    context.runAtTick(
        TestPlayers.READY + buildUp + 5L,
        () -> {
          final List<EldestEntity> eldest = summoned(context);
          context.assertTrue(eldest.size() == 1, "the Eldest still comes");
          context.assertTrue(eldest.getFirst().summonedBy(vampire), "it knows who called it");
          context.assertTrue(
              eldest.getFirst().attacksOnSight(vampire), "it attacks its caller on sight");
          eldest.forEach(EldestEntity::discard);
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 200)
  public void aChaliceShortOfFullStartsNothingAndComesBackWithItsBlood(final TestContext context) {
    final RitualAltarBlockEntity altar = altar(context);
    final ServerPlayerEntity human = TestPlayers.human(context, BESIDE);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          use(context, human, new ItemStack(RitualItems.CASTLE_SIGNET));
          context.assertTrue(altar.chalice().isEmpty(), "a relic is not set on the altar");
          setAndFill(context, human, ChaliceContents.CAPACITY - 1);
          use(context, human, BloodBottleItem.of(BloodQuality.VILLAGER));
          context.assertTrue(
              chalice(altar).bottles() == ChaliceContents.CAPACITY - 1,
              "a bottle of another quality is refused");
          use(context, human, ItemStack.EMPTY);
          context.assertTrue(!altar.active(), "six bottles start nothing");
          human.setSneaking(true);
          use(context, human, ItemStack.EMPTY);
          human.setSneaking(false);
          context.assertTrue(altar.chalice().isEmpty(), "sneaking takes the chalice back");
          context.assertTrue(
              ChaliceBlock.contents(human.getMainHandStack())
                  .equals(new ChaliceContents(ChaliceBlood.ANIMAL, ChaliceContents.CAPACITY - 1)),
              "the chalice comes back with its blood");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = NOON_BATCH, tickLimit = 200)
  public void byDayAFullChaliceStartsNothingAndAVampireDrinksFromIt(final TestContext context) {
    final RitualAltarBlockEntity altar = altar(context);
    final ServerPlayerEntity human = TestPlayers.human(context, BESIDE);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(6, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          setAndFill(context, human, ChaliceContents.CAPACITY);
          use(context, human, ItemStack.EMPTY);
          context.assertTrue(!altar.active(), "daylight starts nothing");
          use(context, vampire, ItemStack.EMPTY);
          context.assertTrue(!altar.active(), "a vampire by day starts nothing either");
          context.assertTrue(
              chalice(altar).bottles() == ChaliceContents.CAPACITY - 1,
              "the vampire drinks one bottle's worth");
          TestPlayers.finish(context, human);
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 200)
  public void breakingTheAltarMidRitualStopsItAndDropsAnEmptyChalice(final TestContext context) {
    final RitualAltarBlockEntity altar = altar(context);
    final ServerPlayerEntity human = TestPlayers.human(context, BESIDE);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          setAndFill(context, human, ChaliceContents.CAPACITY);
          use(context, human, ItemStack.EMPTY);
          context.assertTrue(altar.active(), "the ritual runs");
          context.setBlockState(ALTAR, Blocks.AIR);
        });
    context.runAtTick(
        TestPlayers.READY + 2L,
        () -> {
          final List<ItemEntity> chalices =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      ItemEntity.class,
                      context.getTestBox(),
                      item -> item.getStack().isOf(DecorBlocks.CHALICE.asItem()));
          context.assertTrue(chalices.size() == 1, "the chalice drops, found " + chalices.size());
          context.assertTrue(
              ChaliceBlock.contents(chalices.getFirst().getStack()).empty(), "its blood spills");
          context.assertTrue(summoned(context).isEmpty(), "nothing comes");
          chalices.forEach(ItemEntity::discard);
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void anOldAltarHandsBackTheRelicsLeftOnIt(final TestContext context) {
    final RitualAltarBlockEntity altar = altar(context);
    final NbtCompound nbt = altar.createNbt(context.getWorld().getRegistryManager());
    final NbtList relics = new NbtList();
    relics.add(
        new ItemStack(RitualItems.MANOR_LOCKET).encode(context.getWorld().getRegistryManager()));
    nbt.put("Relics", relics);
    altar.read(nbt, context.getWorld().getRegistryManager());
    context.runAtTick(
        3,
        () -> {
          final List<ItemEntity> dropped =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      ItemEntity.class,
                      context.getTestBox().expand(1.0),
                      item -> item.getStack().isOf(RitualItems.MANOR_LOCKET));
          context.assertTrue(dropped.size() == 1, "the locket comes back, found " + dropped.size());
          context.assertTrue(altar.oldRelics().isEmpty(), "the altar holds no relic");
          dropped.forEach(ItemEntity::discard);
          context.complete();
        });
  }
}
