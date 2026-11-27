package com.grahambartley.nightborn.guild.party;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;

public final class PartyCamp {

  public static final int SEARCH = 12;
  public static final int STRIDE = 3;
  public static final int APART = 16;
  private static final List<BlockPos> NEAREST_FIRST = CampSite.nearestFirst(SEARCH, STRIDE);

  private PartyCamp() {}

  public static CampSite.Ground ground(final BlockState state) {
    return CampSite.classify(
        state.isAir(),
        state.isIn(PartyData.CLEARABLE),
        state.isIn(PartyData.GROUND),
        state.getFluidState().isEmpty());
  }

  public static Optional<BlockPos> site(
      final ServerWorld world,
      final BlockPos near,
      final CampSite.Bounds bounds,
      final Optional<BlockPos> lastCamp) {
    for (final BlockPos offset : NEAREST_FIRST) {
      final int x = near.getX() + offset.getX();
      final int z = near.getZ() + offset.getZ();
      if (!loaded(world, x, z, bounds) || !CampSite.apart(lastCamp, x, z, APART)) {
        continue;
      }
      final BlockPos centre =
          new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z), z);
      if (CampSite.fits(survey(world, centre), bounds)) {
        return Optional.of(centre);
      }
    }
    return Optional.empty();
  }

  public static Optional<BlockPos> around(
      final ServerWorld world, final BlockPos near, final BlockPos fire, final CampLayout layout) {
    final BlockPos centre = fire.subtract(layout.campfire());
    if (!CampSite.inReach(near, fire, SEARCH + APART)
        || !loaded(world, centre.getX(), centre.getZ(), layout.bounds())) {
      return Optional.empty();
    }
    return reusable(world.getBlockState(fire))
            && CampSite.fitsAround(survey(world, centre), layout.bounds(), layout.campfire())
        ? Optional.of(centre)
        : Optional.empty();
  }

  private static CampSite.Survey survey(final ServerWorld world, final BlockPos centre) {
    return (ox, oy, oz) -> ground(world.getBlockState(centre.add(ox, oy, oz)));
  }

  public static boolean reusable(final BlockState fire) {
    return fire.isOf(Blocks.CAMPFIRE)
        && !fire.get(CampfireBlock.LIT)
        && !fire.get(CampfireBlock.WATERLOGGED);
  }

  private static boolean loaded(
      final ServerWorld world, final int x, final int z, final CampSite.Bounds bounds) {
    return CampSite.corners(bounds).stream()
        .allMatch(
            corner ->
                world.isChunkLoaded(
                    ChunkSectionPos.getSectionCoord(x + corner[0]),
                    ChunkSectionPos.getSectionCoord(z + corner[1])));
  }

  public static HunterParty.Camp pitch(
      final ServerWorld world, final BlockPos centre, final CampLayout layout) {
    final CampSite.Bounds bounds = layout.bounds();
    for (int dx = bounds.minX(); dx <= bounds.maxX(); dx++) {
      for (int dz = bounds.minZ(); dz <= bounds.maxZ(); dz++) {
        for (int dy = 0; dy <= bounds.height() + CampSite.CROWN; dy++) {
          final BlockPos at = centre.add(dx, dy, dz);
          final CampSite.Ground ground = ground(world.getBlockState(at));
          if (ground == CampSite.Ground.CLEARABLE) {
            world.setBlockState(at, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
          } else if (dy > bounds.height()) {
            break;
          }
        }
      }
    }
    final CampClaims claims = CampClaims.of(world);
    final List<BlockPos> placed = new ArrayList<>();
    final List<BlockPos> beds = new ArrayList<>();
    for (final CampLayout.Piece piece : layout.pieces()) {
      final BlockPos at = centre.add(piece.pos());
      world.setBlockState(at, piece.state(), Block.NOTIFY_LISTENERS);
      claims.claim(at, piece.state());
      placed.add(at);
      if (piece.state().getBlock() instanceof BedBlock
          && piece.state().get(BedBlock.PART) == BedPart.HEAD) {
        beds.add(at);
      }
    }
    final BlockPos fire = centre.add(layout.campfire());
    final BlockState standing = world.getBlockState(fire);
    world.setBlockState(
        fire,
        reusable(standing)
            ? standing.with(CampfireBlock.LIT, true)
            : Blocks.CAMPFIRE.getDefaultState(),
        Block.NOTIFY_ALL);
    return new HunterParty.Camp(fire, placed, beds);
  }

  public static void strike(final ServerWorld world, final HunterParty.Camp camp) {
    final CampClaims claims = CampClaims.of(world);
    for (final BlockPos at : camp.blocks()) {
      final BlockState state = world.getBlockState(at);
      if (state.getBlock() instanceof StrikesWithCamp piece) {
        piece.strikeWithCamp(world, at, state);
        continue;
      }
      if (claims.release(at, state)) {
        world.setBlockState(at, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
      }
    }
    final BlockState fire = world.getBlockState(camp.fire());
    if (fire.getBlock() instanceof CampfireBlock) {
      world.setBlockState(camp.fire(), fire.with(CampfireBlock.LIT, false), Block.NOTIFY_ALL);
    }
  }
}
