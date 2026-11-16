package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.decor.ChairBlock;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.decor.DrapeBlock;
import com.grahambartley.nightborn.decor.LightableFurnitureBlock;
import com.grahambartley.nightborn.decor.SeatEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.goal.HouseholdLifeGoal;
import com.grahambartley.nightborn.mob.household.Chore;
import com.grahambartley.nightborn.mob.household.ChoreTask;
import com.grahambartley.nightborn.mob.household.Conversation;
import com.grahambartley.nightborn.mob.household.ConverseTask;
import com.grahambartley.nightborn.mob.household.FurnitureKind;
import com.grahambartley.nightborn.mob.household.FurnitureUse;
import com.grahambartley.nightborn.mob.household.HomeFurniture;
import com.grahambartley.nightborn.mob.household.HouseholdBonds;
import com.grahambartley.nightborn.mob.household.ReadTask;
import com.grahambartley.nightborn.mob.household.SitTask;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class HouseholdLifeGameTest implements FabricGameTest {

  static final String ARENA = VampireMobGameTest.ARENA;
  static final int SIT_TICKS = 40;
  static final int TENDED_BY = 30;
  static final int RANGE = 24;
  static final int ELSEWHERE = 200;
  static final String DUSK_BATCH = "household-dusk";
  static final int DUSK = 13100;

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireSitsInAFreeChairBesideItAndRisesWhenDone(final TestContext context) {
    final BlockPos chair = new BlockPos(3, 2, 3);
    context.setBlockState(
        chair, DecorBlocks.ORNATE_CHAIR.getDefaultState().with(ChairBlock.FACING, Direction.SOUTH));
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(3, 2, 4));
    final SitTask sit =
        new SitTask(vampire, context.getWorld(), context.getAbsolutePos(chair), SIT_TICKS);
    sit.tick();
    context.assertTrue(
        vampire.getVehicle() instanceof SeatEntity, "the vampire sits in the chair beside it");
    sit.stop();
    context.assertFalse(vampire.hasVehicle(), "the vampire stands up when its pastime ends");
    context.assertTrue(
        vampire.getBlockPos().equals(context.getAbsolutePos(chair.south())),
        "the vampire steps off the front of the chair, not onto its seat");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aSeatedVampireIsSavedStandingOnItsSeat(final TestContext context) {
    final BlockPos chair = new BlockPos(3, 2, 3);
    context.setBlockState(
        chair, DecorBlocks.ORNATE_CHAIR.getDefaultState().with(ChairBlock.FACING, Direction.SOUTH));
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(3, 2, 4));
    new SitTask(vampire, context.getWorld(), context.getAbsolutePos(chair), SIT_TICKS).tick();
    vampire.setBodyYaw(90.0f);
    vampire.getVehicle().updatePassengerPosition(vampire);
    context.assertTrue(
        Math.abs(vampire.getBodyYaw() - vampire.getVehicle().getYaw()) < 1.0E-4f,
        "a seated vampire faces the way its chair faces");
    final double seatY = vampire.getVehicle().getY();
    final NbtCompound nbt = new NbtCompound();
    context.assertTrue(
        vampire.saveNbt(nbt), "a seated vampire is saved even though its seat is not");
    final VampireEntity loaded = NightbornMobs.VAMPIRE.create(context.getWorld());
    loaded.readNbt(nbt);
    context.assertFalse(loaded.hasVehicle(), "the vampire loads on its feet");
    context.assertTrue(
        Math.abs(loaded.getY() - seatY) < 1.0E-6, "the vampire stands on the seat it sat in");
    loaded.discard();
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TENDED_BY + 10)
  public void aVampireLightsACandelabraWithFlintAndSteelThenPutsItAway(final TestContext context) {
    final BlockPos candelabra = new BlockPos(3, 2, 3);
    context.setBlockState(candelabra, DecorBlocks.CANDELABRA.getDefaultState());
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(3, 2, 5));
    final ChoreTask light =
        new ChoreTask(vampire, context.getWorld(), context.getAbsolutePos(candelabra), Chore.LIGHT);
    context.runAtEveryTick(
        () -> {
          if (!light.tick()) {
            light.stop();
            context.checkBlockState(
                candelabra,
                state -> state.get(LightableFurnitureBlock.LIT),
                () -> "the candelabra is lit");
            context.assertTrue(
                vampire.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty(),
                "the vampire puts the flint and steel away");
            context.complete();
          } else if (context.getTick() > 2) {
            context.assertTrue(
                vampire.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.FLINT_AND_STEEL),
                "the vampire holds flint and steel while it lights the candles");
          }
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TENDED_BY + 10)
  public void aVampireDrawsAnOpenDrapeBeforeDawn(final TestContext context) {
    final BlockPos drape = new BlockPos(3, 2, 2);
    context.setBlockState(
        drape,
        DecorBlocks.BURGUNDY_VELVET_DRAPE
            .getDefaultState()
            .with(DrapeBlock.FACING, Direction.SOUTH)
            .with(DrapeBlock.OPEN, true));
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(3, 2, 4));
    final ChoreTask close =
        new ChoreTask(
            vampire, context.getWorld(), context.getAbsolutePos(drape), Chore.CLOSE_DRAPES);
    context.runAtEveryTick(
        () -> {
          if (!close.tick()) {
            close.stop();
            context.checkBlockState(
                drape, state -> !state.get(DrapeBlock.OPEN), () -> "the drape is drawn");
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireReadsABookFromTheShelfBesideIt(final TestContext context) {
    final BlockPos shelf = new BlockPos(3, 2, 3);
    context.setBlockState(shelf, Blocks.BOOKSHELF);
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(3, 2, 4));
    final ReadTask read =
        new ReadTask(vampire, context.getWorld(), context.getAbsolutePos(shelf), SIT_TICKS);
    read.tick();
    context.assertTrue(vampire.reading(), "the vampire opens a book");
    context.assertTrue(
        vampire.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.BOOK),
        "the vampire holds the book it reads");
    read.stop();
    context.assertFalse(vampire.reading(), "the vampire closes the book");
    context.assertTrue(
        vampire.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty(), "the book goes back");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireSavedMidReadGetsBackWhatItHeldWhenItLoads(final TestContext context) {
    final BlockPos shelf = new BlockPos(3, 2, 3);
    context.setBlockState(shelf, Blocks.BOOKSHELF);
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(3, 2, 4));
    vampire.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.ROSE_BUSH));
    new ReadTask(vampire, context.getWorld(), context.getAbsolutePos(shelf), SIT_TICKS).tick();
    final NbtCompound nbt = new NbtCompound();
    vampire.writeNbt(nbt);
    final VampireEntity loaded = NightbornMobs.VAMPIRE.create(context.getWorld());
    loaded.readNbt(nbt);
    context.assertTrue(
        loaded.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.ROSE_BUSH),
        "a reloaded vampire puts the book back and holds what it held before");
    loaded.discard();
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aHomeKnowsItsChairsBooksLightsAndDrapes(final TestContext context) {
    final BlockPos chair = new BlockPos(1, 2, 1);
    final BlockPos shelf = new BlockPos(2, 2, 1);
    final BlockPos candle = new BlockPos(3, 2, 1);
    final BlockPos drape = new BlockPos(4, 2, 1);
    context.setBlockState(chair, DecorBlocks.ORNATE_CHAIR);
    context.setBlockState(shelf, Blocks.BOOKSHELF);
    context.setBlockState(candle, Blocks.CANDLE.getDefaultState().with(CandleBlock.LIT, false));
    context.setBlockState(drape, DecorBlocks.EMERALD_VELVET_DRAPE);
    final Map<FurnitureKind, List<BlockPos>> found =
        HomeFurniture.scan(context.getWorld(), context.getAbsolutePos(new BlockPos(3, 2, 3)), 6);
    context.assertTrue(
        found.getOrDefault(FurnitureKind.SEAT, List.of()).contains(context.getAbsolutePos(chair)),
        "the chair is a seat");
    context.assertTrue(
        found.getOrDefault(FurnitureKind.BOOKS, List.of()).contains(context.getAbsolutePos(shelf)),
        "the bookshelf holds books to read");
    context.assertTrue(
        found.getOrDefault(FurnitureKind.LIGHT, List.of()).contains(context.getAbsolutePos(candle)),
        "the candle is a light to tend");
    context.assertTrue(
        FurnitureUse.lightable(context.getBlockState(candle)),
        "an unlit vanilla candle can be lit");
    context.setBlockState(candle, Blocks.CANDLE_CAKE);
    context.assertTrue(
        FurnitureKind.of(context.getBlockState(candle)).equals(Optional.of(FurnitureKind.LIGHT))
            && FurnitureUse.lightable(context.getBlockState(candle)),
        "an unlit candle cake is a light a vampire can tend");
    context.assertTrue(
        found.getOrDefault(FurnitureKind.DRAPE, List.of()).contains(context.getAbsolutePos(drape)),
        "the drape can be drawn");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void housematesMournADeadVampireButStrangersDoNot(final TestContext context) {
    final BlockPos home = context.getAbsolutePos(new BlockPos(3, 2, 3));
    final VampireEntity dead = VampireMobGameTest.still(context, new BlockPos(2, 2, 2));
    final VampireEntity housemate = VampireMobGameTest.still(context, new BlockPos(4, 2, 2));
    final VampireEntity stranger = VampireMobGameTest.still(context, new BlockPos(4, 2, 4));
    dead.settleAt(home);
    housemate.settleAt(home);
    stranger.settleAt(home.add(ELSEWHERE, 0, 0));
    HouseholdBonds.mourn(context.getWorld(), dead, RANGE);
    final long now = context.getWorld().getTime();
    context.assertTrue(housemate.householdLife().grieving(now), "the housemate mourns");
    context.assertFalse(stranger.householdLife().grieving(now), "a stranger does not");
    context.complete();
  }

  @BeforeBatch(batchId = DUSK_BATCH)
  public void pinDusk(final ServerWorld world) {
    WorldClock.freeze(world, DUSK);
  }

  @AfterBatch(batchId = DUSK_BATCH)
  public void unpinDusk(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = ARENA, batchId = DUSK_BATCH, tickLimit = TENDED_BY + 20)
  public void atDuskAHouseholdVampireLightsTheCandlesOfItsHome(final TestContext context) {
    final BlockPos candle = new BlockPos(3, 2, 3);
    context.setBlockState(candle, Blocks.CANDLE.getDefaultState().with(CandleBlock.LIT, false));
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(3, 2, 4));
    vampire.settleAt(context.getAbsolutePos(new BlockPos(3, 2, 4)));
    final HouseholdLifeGoal goal = new HouseholdLifeGoal(vampire);
    context.assertTrue(goal.canStart(), "a calm vampire at home takes up its household life");
    goal.start();
    context.runAtEveryTick(
        () -> {
          goal.tick();
          if (context.getBlockState(candle).get(CandleBlock.LIT)) {
            goal.stop();
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, tickLimit = ConverseTask.LINE_TICKS * 3)
  public void housematesTakeTurnsAndQuarrelInAConversation(final TestContext context) {
    final VampireEntity opener = VampireMobGameTest.still(context, new BlockPos(3, 2, 3));
    final VampireEntity other = VampireMobGameTest.still(context, new BlockPos(4, 2, 3));
    other.householdLife().engage(opener.getId(), Long.MAX_VALUE);
    final ConverseTask talk =
        new ConverseTask(
            opener,
            other,
            List.of(
                new Conversation.Line(true, Conversation.Tone.TALK),
                new Conversation.Line(false, Conversation.Tone.ARGUE)));
    context.runAtEveryTick(
        () -> {
          if (!talk.tick()) {
            talk.stop();
            context.assertEquals(opener.action(), "talk", "the opener speaks first");
            context.assertEquals(other.action(), "argue", "the reply turns into an argument");
            context.assertTrue(
                other.householdLife().engagement(context.getWorld().getTime()).isEmpty(),
                "the housemate is free once the conversation ends");
            context.complete();
          }
        });
  }
}
