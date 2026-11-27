package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.guild.HunterReferral;
import com.grahambartley.nightborn.guild.HunterTargetGoal;
import com.grahambartley.nightborn.guild.party.CampBats;
import com.grahambartley.nightborn.guild.party.CampLayout;
import com.grahambartley.nightborn.guild.party.CampSite;
import com.grahambartley.nightborn.guild.party.CampStay;
import com.grahambartley.nightborn.guild.party.HunterParty;
import com.grahambartley.nightborn.guild.party.HunterTentBlock;
import com.grahambartley.nightborn.guild.party.HunterTentBlockEntity;
import com.grahambartley.nightborn.guild.party.LeaderlessCamps;
import com.grahambartley.nightborn.guild.party.PartyCamp;
import com.grahambartley.nightborn.guild.party.PartySchedule;
import com.grahambartley.nightborn.guild.party.PitchedCamps;
import com.grahambartley.nightborn.guild.party.TentBlocks;
import com.grahambartley.nightborn.guild.party.TentPart;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireBatEntity;
import com.grahambartley.nightborn.mob.VampireEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class HunterPartyGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "hunter_party";
  static final int SWEEP_SLACK = 20;
  static final BlockPos CENTRE = new BlockPos(4, 2, 4);
  static final BlockPos BED_FOOT = new BlockPos(6, 2, 4);
  static final BlockPos TENT_SIDE = new BlockPos(5, 2, 4);
  static final BlockPos TENT_ORIGIN = new BlockPos(6, 3, 5);
  static final CampLayout LAYOUT =
      new CampLayout(
          BlockPos.ORIGIN,
          List.of(new CampLayout.Tent(new BlockPos(2, 0, 0), BlockRotation.NONE, tentPieces())));

  private static List<CampLayout.Piece> tentPieces() {
    final List<CampLayout.Piece> pieces = new ArrayList<>();
    for (final TentPart part : TentPart.values()) {
      pieces.add(
          new CampLayout.Piece(
              new BlockPos(0, 1, 1).add(part.offset(Direction.NORTH)),
              TentBlocks.HUNTER_TENT.getDefaultState().with(HunterTentBlock.PART, part)));
    }
    pieces.add(
        new CampLayout.Piece(
            BlockPos.ORIGIN,
            Blocks.BROWN_BED.getDefaultState().with(BedBlock.FACING, Direction.SOUTH)));
    pieces.add(
        new CampLayout.Piece(
            new BlockPos(0, 0, 1),
            Blocks.BROWN_BED
                .getDefaultState()
                .with(BedBlock.FACING, Direction.SOUTH)
                .with(BedBlock.PART, BedPart.HEAD)));
    return pieces;
  }

  private static void meadow(final TestContext context) {
    for (int x = 0; x < 9; x++) {
      for (int z = 0; z < 9; z++) {
        context.setBlockState(new BlockPos(x, 1, z), Blocks.GRASS_BLOCK);
        context.setBlockState(new BlockPos(x, 4, z), Blocks.AIR);
      }
    }
  }

  private static boolean fits(final TestContext context) {
    final BlockPos centre = context.getAbsolutePos(CENTRE);
    return CampSite.fits(
        (dx, dy, dz) -> PartyCamp.ground(context.getWorld().getBlockState(centre.add(dx, dy, dz))),
        LAYOUT.bounds());
  }

  private static HunterParty.Camp pitch(final TestContext context) {
    return PartyCamp.pitch(context.getWorld(), context.getAbsolutePos(CENTRE), LAYOUT);
  }

  @GameTest(templateName = ARENA, batchId = BATCH)
  public void aPartyPitchesALitFireTentsAndBedsOnClearMeadow(final TestContext context) {
    meadow(context);
    context.setBlockState(TENT_SIDE, Blocks.SHORT_GRASS);
    context.assertTrue(fits(context), "clear meadow takes a camp");
    final HunterParty.Camp camp = pitch(context);
    context.checkBlockState(CENTRE, state -> state.get(CampfireBlock.LIT), () -> "a lit fire");
    context.expectBlock(TentBlocks.HUNTER_TENT, TENT_SIDE);
    context.checkBlockEntity(
        TENT_ORIGIN, entity -> entity instanceof HunterTentBlockEntity, () -> "the tent is drawn");
    context.expectBlock(Blocks.BROWN_BED, BED_FOOT);
    context.assertEquals(camp.beds().size(), 1, "one bed to sleep in");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH)
  public void aPartyNeverPitchesBesideAPlayersBuild(final TestContext context) {
    meadow(context);
    context.setBlockState(new BlockPos(8, 2, 4), Blocks.COBBLESTONE);
    context.assertFalse(fits(context), "a built block beside the site rules it out");
    context.expectBlock(Blocks.COBBLESTONE, new BlockPos(8, 2, 4));
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH)
  public void breakingCampLeavesOnlyAnUnlitFire(final TestContext context) {
    meadow(context);
    PartyCamp.strike(context.getWorld(), pitch(context));
    context.checkBlockState(CENTRE, state -> !state.get(CampfireBlock.LIT), () -> "an unlit fire");
    context.expectBlock(Blocks.AIR, BED_FOOT);
    context.checkBlockState(
        TENT_ORIGIN, state -> state.get(HunterTentBlock.STRUCK), () -> "the tent comes down");
    context.runAtTick(
        HunterTentBlock.STRIKE_TICKS + 2,
        () -> {
          context.expectBlock(Blocks.AIR, TENT_ORIGIN);
          context.expectBlock(Blocks.AIR, TENT_SIDE);
          context.complete();
        });
  }

  @GameTest(
      templateName = ARENA,
      batchId = BATCH,
      tickLimit = LeaderlessCamps.CHECK_TICKS + SWEEP_SLACK)
  public void aCampWithNoLivingLeaderComesDownWhenItsNightsRunOut(final TestContext context) {
    meadow(context);
    final ServerWorld world = context.getWorld();
    final CampStay ranOut =
        new CampStay(1, PartySchedule.day(world.getTimeOfDay()) - 1, world.getTime());
    PitchedCamps.of(world).pitched(UUID.randomUUID(), pitch(context), ranOut);
    context.runAtTick(
        LeaderlessCamps.CHECK_TICKS + 1,
        () -> {
          context.checkBlockState(
              CENTRE, state -> !state.get(CampfireBlock.LIT), () -> "an unlit fire");
          context.expectBlock(Blocks.AIR, BED_FOOT);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH)
  public void tentsAndBedsDropNothing(final TestContext context) {
    meadow(context);
    pitch(context);
    context.getWorld().breakBlock(context.getAbsolutePos(TENT_SIDE), true);
    context.getWorld().breakBlock(context.getAbsolutePos(BED_FOOT), true);
    context.assertTrue(
        context
            .getWorld()
            .getEntitiesByClass(ItemEntity.class, context.getTestBox(), item -> true)
            .isEmpty(),
        "nothing dropped");
    context.expectBlock(Blocks.AIR, TENT_ORIGIN);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH)
  public void aPartyAndItsCampSurviveASaveAndLoad(final TestContext context) {
    meadow(context);
    final GuildMasterEntity master = context.spawnEntity(GuildMobs.GUILD_MASTER, CENTRE.north(2));
    master.setAiDisabled(true);
    final HunterParty party =
        HunterParty.gathered(List.of(UUID.randomUUID()), context.getAbsolutePos(CENTRE), 0L)
            .pitch(pitch(context), 2, 0L, context.getWorld().getTime());
    master.lead(party);
    final NbtCompound saved = new NbtCompound();
    master.writeNbt(saved);
    final GuildMasterEntity loaded = GuildMobs.GUILD_MASTER.create(context.getWorld());
    loaded.readNbt(saved);
    context.assertEquals(loaded.party(), party, "the party, its camp and its nights");
    loaded.discard();
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH)
  public void batsRaidTheCampAndHuntersFightThem(final TestContext context) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, CENTRE);
    hunter.setAiDisabled(true);
    final VampireBatEntity wild = context.spawnEntity(NightbornMobs.VAMPIRE_BAT, CENTRE.up(2));
    wild.setAiDisabled(true);
    CampBats.raid(context.getWorld(), context.getAbsolutePos(CENTRE), 3, Random.create(5));
    final List<VampireBatEntity> raiders =
        context
            .getWorld()
            .getEntitiesByClass(
                VampireBatEntity.class,
                new Box(context.getAbsolutePos(CENTRE)).expand(CampBats.RAID_RANGE),
                VampireBatEntity::raidsCamp);
    raiders.forEach(bat -> bat.setAiDisabled(true));
    context.assertEquals(raiders.size(), 3, "three bats fall on the camp");
    context.assertTrue(hunter.canTarget(raiders.get(0)), "a hunter fights a raiding bat");
    context.assertFalse(hunter.canTarget(wild), "a hunter leaves other bats alone");
    raiders.forEach(VampireBatEntity::discard);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH)
  public void aPartyHunterPointsToItsOwnGuildMaster(final TestContext context) {
    final GuildMasterEntity stranger =
        context.spawnEntity(GuildMobs.GUILD_MASTER, new BlockPos(2, 2, 4));
    final GuildMasterEntity leader =
        context.spawnEntity(GuildMobs.GUILD_MASTER, new BlockPos(7, 2, 4));
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(1, 2, 4));
    stranger.setAiDisabled(true);
    leader.setAiDisabled(true);
    hunter.setAiDisabled(true);
    hunter.join(leader.getUuid());
    context.assertTrue(
        HunterReferral.master(context.getWorld(), hunter).orElse(null) == leader,
        "a party hunter should point to its own Guild Master, not the nearest one");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH)
  public void aHunterFightsBackAgainstAMonsterThatHitsIt(final TestContext context) {
    final HunterEntity struck = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(2, 2, 4));
    final HunterEntity bystander = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(6, 2, 4));
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 2, 4));
    struck.setAiDisabled(true);
    bystander.setAiDisabled(true);
    zombie.setAiDisabled(true);
    struck.damage(context.getWorld().getDamageSources().mobAttack(zombie), 1.0F);
    context.assertTrue(struck.canTarget(zombie), "a hunter fights back against a zombie");
    context.assertFalse(
        bystander.canTarget(zombie), "a hunter nobody struck does not start the fight itself");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH)
  public void aHunterTakesAVampireBesideItOnItsFirstLook(final TestContext context) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(2, 2, 4));
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(5, 2, 4));
    hunter.setAiDisabled(true);
    vampire.setAiDisabled(true);
    final HunterTargetGoal sight = new HunterTargetGoal(hunter);
    context.assertTrue(sight.canStart(), "a hunter sees a vampire three blocks away at once");
    sight.start();
    context.assertTrue(hunter.getTarget() == vampire, "and goes for it");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH)
  public void aHunterBehindATentPathsRoundItToItsOpenEnd(final TestContext context) {
    meadow(context);
    pitch(context);
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(6, 2, 8));
    hunter.setAiDisabled(true);
    context.runAtTick(
        5,
        () -> {
          hunter.setOnGround(true);
          final Path path =
              hunter.getNavigation().findPathTo(context.getAbsolutePos(new BlockPos(6, 2, 3)), 0);
          context.assertTrue(path != null, "a hunter finds a way round the tent");
          for (int i = 0; i < path.getLength(); i++) {
            final BlockPos node = path.getNodePos(i);
            final BlockState state = context.getWorld().getBlockState(node);
            context.assertFalse(
                state.isOf(TentBlocks.HUNTER_TENT)
                    && !state.getCollisionShape(context.getWorld(), node).isEmpty(),
                "the path goes through tent canvas at " + context.getRelativePos(node));
          }
          context.assertTrue(
              path.getEnd().getBlockPos().equals(context.getAbsolutePos(new BlockPos(6, 2, 3))),
              "the path ends at the tent's open end, not short of it");
          context.complete();
        });
  }
}
