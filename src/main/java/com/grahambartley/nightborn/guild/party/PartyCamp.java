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

  private PartyCamp() {}

  public static CampSite.Ground ground(final BlockState state) {
    if (state.isAir()) {
      return CampSite.Ground.AIR;
    }
    if (state.isIn(PartyData.CLEARABLE)) {
      return CampSite.Ground.CLEARABLE;
    }
    return state.isIn(PartyData.GROUND) && state.getFluidState().isEmpty()
        ? CampSite.Ground.NATURAL
        : CampSite.Ground.BUILT;
  }

  public static Optional<BlockPos> site(
      final ServerWorld world, final BlockPos near, final CampSite.Bounds bounds) {
    for (int dx = -SEARCH; dx <= SEARCH; dx += STRIDE) {
      for (int dz = -SEARCH; dz <= SEARCH; dz += STRIDE) {
        final int x = near.getX() + dx;
        final int z = near.getZ() + dz;
        if (!loaded(world, x, z, bounds)) {
          continue;
        }
        final BlockPos centre =
            new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z), z);
        if (CampSite.fits(
            (ox, oy, oz) -> ground(world.getBlockState(centre.add(ox, oy, oz))), bounds)) {
          return Optional.of(centre);
        }
      }
    }
    return Optional.empty();
  }

  private static boolean loaded(
      final ServerWorld world, final int x, final int z, final CampSite.Bounds bounds) {
    for (final int ox : new int[] {bounds.minX() - 1, bounds.maxX() + 1}) {
      for (final int oz : new int[] {bounds.minZ() - 1, bounds.maxZ() + 1}) {
        if (!world.isChunkLoaded(
            ChunkSectionPos.getSectionCoord(x + ox), ChunkSectionPos.getSectionCoord(z + oz))) {
          return false;
        }
      }
    }
    return true;
  }

  public static HunterParty.Camp pitch(
      final ServerWorld world, final BlockPos centre, final CampLayout layout) {
    final CampSite.Bounds bounds = layout.bounds();
    for (int dx = bounds.minX(); dx <= bounds.maxX(); dx++) {
      for (int dz = bounds.minZ(); dz <= bounds.maxZ(); dz++) {
        for (int dy = 0; dy <= bounds.height(); dy++) {
          final BlockPos at = centre.add(dx, dy, dz);
          if (ground(world.getBlockState(at)) == CampSite.Ground.CLEARABLE) {
            world.setBlockState(at, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
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
    world.setBlockState(fire, Blocks.CAMPFIRE.getDefaultState(), Block.NOTIFY_ALL);
    return new HunterParty.Camp(fire, placed, beds);
  }

  public static void strike(final ServerWorld world, final HunterParty.Camp camp) {
    final CampClaims claims = CampClaims.of(world);
    for (final BlockPos at : camp.blocks()) {
      final BlockState state = world.getBlockState(at);
      if (claims.holds(at, state)) {
        claims.release(at, state);
        world.setBlockState(at, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
      }
    }
    final BlockState fire = world.getBlockState(camp.fire());
    if (fire.getBlock() instanceof CampfireBlock) {
      world.setBlockState(camp.fire(), fire.with(CampfireBlock.LIT, false), Block.NOTIFY_ALL);
    }
  }
}
