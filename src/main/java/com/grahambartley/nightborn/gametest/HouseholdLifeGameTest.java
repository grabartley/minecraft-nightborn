package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.decor.ChairBlock;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.decor.DrapeBlock;
import com.grahambartley.nightborn.decor.LightableFurnitureBlock;
import com.grahambartley.nightborn.decor.SeatEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.household.Chore;
import com.grahambartley.nightborn.mob.household.ChoreTask;
import com.grahambartley.nightborn.mob.household.FurnitureKind;
import com.grahambartley.nightborn.mob.household.HomeFurniture;
import com.grahambartley.nightborn.mob.household.HouseholdBonds;
import com.grahambartley.nightborn.mob.household.ReadTask;
import com.grahambartley.nightborn.mob.household.SitTask;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
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
    final ReadTask read = new ReadTask(vampire, context.getAbsolutePos(shelf), SIT_TICKS);
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
  public void aVampireSavedMidReadPutsTheBookBackWhenItLoads(final TestContext context) {
    final BlockPos shelf = new BlockPos(3, 2, 3);
    context.setBlockState(shelf, Blocks.BOOKSHELF);
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(3, 2, 4));
    new ReadTask(vampire, context.getAbsolutePos(shelf), SIT_TICKS).tick();
    final NbtCompound nbt = new NbtCompound();
    vampire.writeNbt(nbt);
    final VampireEntity loaded = NightbornMobs.VAMPIRE.create(context.getWorld());
    loaded.readNbt(nbt);
    context.assertTrue(
        loaded.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty(),
        "a reloaded vampire does not keep the book it was reading");
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
}
