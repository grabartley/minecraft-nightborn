package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.StructureSettings;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.gramophone.GramophoneBlock;
import com.grahambartley.nightborn.gramophone.GramophoneBlockEntity;
import com.grahambartley.nightborn.gramophone.MusicDiscs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.household.GramophoneTask;
import com.grahambartley.nightborn.server.ServerConfigService;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.jukebox.JukeboxSongs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

public class GramophoneGameTest implements FabricGameTest {

  private static final String ROOM = "nightborn:decor_room";
  private static final String ARENA = VampireMobGameTest.ARENA;
  private static final String AMBIENCE_BATCH = "gramophone-ambience";
  private static final BlockPos FLOOR = new BlockPos(2, 1, 2);
  private static final BlockPos ON_FLOOR = FLOOR.up();
  private static final int SHORTEST_WAIT_SECONDS = 20;
  private static final int LONGEST_WAIT_TICKS = SHORTEST_WAIT_SECONDS * 30 + 40;
  private static final int LISTEN_TICKS = 40;

  @BeforeBatch(batchId = AMBIENCE_BATCH)
  public void playOften(final ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(StructureSettings.GRAMOPHONE_SECONDS, SHORTEST_WAIT_SECONDS));
  }

  @AfterBatch(batchId = AMBIENCE_BATCH)
  public void restoreInterval(final ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(
                StructureSettings.GRAMOPHONE_SECONDS,
                StructureSettings.GRAMOPHONE_SECONDS.defaultValue()));
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aGothicDiscPutInAGramophonePlaysAndSignalsLikeAJukebox(final TestContext context) {
    place(context);
    final PlayerEntity player = holding(context, new ItemStack(disc()));

    context.useBlock(ON_FLOOR, player);

    final BlockState state = context.getBlockState(ON_FLOOR);
    final BlockPos at = context.getAbsolutePos(ON_FLOOR);
    context.assertTrue(state.get(GramophoneBlock.HAS_RECORD), "the gramophone holds the disc");
    context.assertTrue(state.get(GramophoneBlock.PLAYING), "the gramophone plays the disc");
    context.assertTrue(player.getMainHandStack().isEmpty(), "the disc leaves the player's hand");
    context.assertEquals(
        15,
        state.getWeakRedstonePower(context.getWorld(), at, Direction.UP),
        "a playing gramophone powers redstone like a jukebox");
    context.assertEquals(
        1,
        state.getComparatorOutput(context.getWorld(), at),
        "a comparator reads the disc's own signal");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aVanillaDiscPlaysInAGramophone(final TestContext context) {
    place(context);

    context.useBlock(ON_FLOOR, holding(context, new ItemStack(Items.MUSIC_DISC_CAT)));

    final BlockState state = context.getBlockState(ON_FLOOR);
    context.assertTrue(state.get(GramophoneBlock.PLAYING), "the gramophone plays a vanilla disc");
    context.assertEquals(
        context
            .getWorld()
            .getRegistryManager()
            .get(RegistryKeys.JUKEBOX_SONG)
            .get(JukeboxSongs.CAT)
            .comparatorOutput(),
        state.getComparatorOutput(context.getWorld(), context.getAbsolutePos(ON_FLOOR)),
        "a comparator reads the same signal a jukebox gives for the disc");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void usingALoadedGramophoneGivesTheDiscBackAndStopsIt(final TestContext context) {
    place(context);
    context.useBlock(ON_FLOOR, holding(context, new ItemStack(disc())));

    context.useBlock(ON_FLOOR, holding(context, ItemStack.EMPTY));

    final BlockState state = context.getBlockState(ON_FLOOR);
    context.assertFalse(state.get(GramophoneBlock.HAS_RECORD), "the gramophone is empty");
    context.assertFalse(state.get(GramophoneBlock.PLAYING), "the music stops");
    context.assertEquals(
        0,
        state.getWeakRedstonePower(
            context.getWorld(), context.getAbsolutePos(ON_FLOOR), Direction.UP),
        "an empty gramophone powers nothing");
    context.expectItemAt(disc(), ON_FLOOR, 2.0);
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aGothicDiscPlaysInAVanillaJukebox(final TestContext context) {
    context.setBlockState(ON_FLOOR, Blocks.JUKEBOX);
    final PlayerEntity player = holding(context, new ItemStack(disc()));

    context.useBlock(ON_FLOOR, player);

    final JukeboxBlockEntity jukebox = context.getBlockEntity(ON_FLOOR);
    context.assertTrue(player.getMainHandStack().isEmpty(), "the jukebox takes the disc");
    context.assertTrue(jukebox.getManager().isPlaying(), "the jukebox plays the gothic disc");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 60)
  public void aHopperLoadsAGramophoneWhichStartsTheDisc(final TestContext context) {
    place(context);
    context.setBlockState(
        ON_FLOOR.up(), Blocks.HOPPER.getDefaultState().with(HopperBlock.FACING, Direction.DOWN));
    final HopperBlockEntity hopper = context.getBlockEntity(ON_FLOOR.up());
    hopper.setStack(0, new ItemStack(disc()));

    context.runAtEveryTick(
        () -> {
          final BlockState state = context.getBlockState(ON_FLOOR);
          if (state.get(GramophoneBlock.PLAYING)) {
            context.assertTrue(hopper.isEmpty(), "the disc leaves the hopper");
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireStartsALoadedGramophoneBesideIt(final TestContext context) {
    final BlockPos spot = new BlockPos(3, 2, 3);
    final GramophoneBlockEntity gramophone = loaded(context, spot, false);
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(3, 2, 4));
    final GramophoneTask task =
        new GramophoneTask(vampire, context.getWorld(), context.getAbsolutePos(spot), LISTEN_TICKS);

    context.assertFalse(gramophone.playing(), "a loaded gramophone waits in silence");
    context.assertTrue(task.tick(), "the vampire stays to listen");

    context.assertTrue(gramophone.playing(), "the vampire starts the disc");
    context.assertTrue(
        context.getBlockState(spot).get(GramophoneBlock.PLAYING), "the gramophone shows it plays");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = AMBIENCE_BATCH, tickLimit = LONGEST_WAIT_TICKS)
  public void aStructureGramophoneStartsItsDiscWhileAPlayerIsNear(final TestContext context) {
    final BlockPos spot = new BlockPos(3, 2, 3);
    final GramophoneBlockEntity gramophone = loaded(context, spot, true);
    final ServerPlayerEntity player = TestPlayers.human(context, new BlockPos(3, 2, 5));

    context.assertFalse(gramophone.playing(), "a structure gramophone starts out silent");
    context.runAtEveryTick(
        () -> {
          if (gramophone.playing()) {
            context.assertEquals(
                disc(), gramophone.getStack().getItem(), "it plays the disc it was built with");
            TestPlayers.remove(context, player);
            context.complete();
          }
        });
  }

  private static Item disc() {
    return MusicDiscs.all().getFirst();
  }

  private static void place(final TestContext context) {
    context.setBlockState(FLOOR, Blocks.STONE);
    context.setBlockState(ON_FLOOR, DecorBlocks.GRAMOPHONE.getDefaultState());
  }

  private static GramophoneBlockEntity loaded(
      final TestContext context, final BlockPos spot, final boolean ambient) {
    context.setBlockState(
        spot, DecorBlocks.GRAMOPHONE.getDefaultState().with(GramophoneBlock.HAS_RECORD, true));
    final GramophoneBlockEntity gramophone = context.getBlockEntity(spot);
    final NbtCompound nbt = new NbtCompound();
    nbt.put(
        GramophoneBlockEntity.RECORD_KEY,
        new ItemStack(disc()).encode(context.getWorld().getRegistryManager()));
    nbt.putBoolean(GramophoneBlockEntity.AMBIENT_KEY, ambient);
    gramophone.read(nbt, context.getWorld().getRegistryManager());
    return gramophone;
  }

  private static PlayerEntity holding(final TestContext context, final ItemStack stack) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    player.setStackInHand(Hand.MAIN_HAND, stack);
    return player;
  }
}
