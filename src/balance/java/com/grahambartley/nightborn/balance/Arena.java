package com.grahambartley.nightborn.balance;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.VampireEntity;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

final class Arena {

  static final int SPACING = 64;
  static final int HALF = 10;
  static final int HEIGHT = 5;
  static final int FLOOR_Y = -61;
  static final int TEAM_GAP = 7;

  private final ServerWorld world;
  private final Matchup matchup;
  private final BlockPos origin;
  private final int roundsWanted;
  private final List<RoundResult> results = new ArrayList<>();
  private final Map<Side, List<MobEntity>> fighters = new EnumMap<>(Side.class);
  private Tally tally = new Tally();
  private int ticks;

  Arena(final ServerWorld world, final Matchup matchup, final int index, final int roundsWanted) {
    this.world = world;
    this.matchup = matchup;
    this.origin = new BlockPos(index * SPACING, FLOOR_Y, 0);
    this.roundsWanted = roundsWanted;
  }

  Matchup matchup() {
    return matchup;
  }

  List<RoundResult> results() {
    return results;
  }

  boolean done() {
    return results.size() >= roundsWanted;
  }

  void open() {
    for (int x = origin.getX() - HALF - 1; x <= origin.getX() + HALF + 1; x += 16) {
      for (int z = -HALF - 1; z <= HALF + 1; z += 16) {
        world.setChunkForced(x >> 4, z >> 4, true);
      }
    }
    final ChunkPos far = new ChunkPos(new BlockPos(origin.getX() + HALF + 1, 0, HALF + 1));
    world.setChunkForced(far.x, far.z, true);
    startRound();
  }

  Side sideOf(final Entity entity) {
    for (final Side side : Side.values()) {
      if (fighters.getOrDefault(side, List.of()).contains(entity)) {
        return side;
      }
    }
    return null;
  }

  Tally tally() {
    return tally;
  }

  void tick() {
    if (done()) {
      return;
    }
    ticks++;
    final RoundRule.Verdict verdict =
        RoundRule.judge(standing(Side.VAMPIRES), standing(Side.HUNTERS), ticks);
    if (verdict.over()) {
      results.add(
          new RoundResult(
              matchup.key(),
              verdict.winner(),
              ticks,
              Map.of(Side.VAMPIRES, tally.of(Side.VAMPIRES), Side.HUNTERS, tally.of(Side.HUNTERS)),
              Map.of(Side.VAMPIRES, tags(Side.VAMPIRES), Side.HUNTERS, tags(Side.HUNTERS))));
      clear();
      if (!done()) {
        startRound();
      }
    }
  }

  private void startRound() {
    build();
    tally = new Tally();
    ticks = 0;
    fighters.put(Side.VAMPIRES, team(matchup.vampires(), TEAM_GAP, 180.0F));
    fighters.put(Side.HUNTERS, team(matchup.hunters(), -TEAM_GAP, 0.0F));
  }

  private List<MobEntity> team(final Roster roster, final int z, final float yaw) {
    final List<MobEntity> team = new ArrayList<>();
    for (int i = 0; i < matchup.size(); i++) {
      final double x = origin.getX() + 0.5 + (i - (matchup.size() - 1) / 2.0) * 2.0;
      final MobEntity mob = Fighters.spawn(world, roster, new Vec3d(x, FLOOR_Y + 1, z + 0.5), yaw);
      if (mob instanceof VampireEntity vampire) {
        vampire.settleAt(origin.up());
      }
      team.add(mob);
    }
    return team;
  }

  private int standing(final Side side) {
    int standing = 0;
    for (final MobEntity mob : fighters.getOrDefault(side, List.of())) {
      if (mob.isAlive() && !mob.isRemoved() && !Downed.helpless(mob) && inside(mob)) {
        standing++;
      }
    }
    return standing;
  }

  private boolean inside(final Entity entity) {
    return bounds().contains(entity.getPos());
  }

  private Set<String> tags(final Side side) {
    final Set<String> tags = new LinkedHashSet<>();
    fighters.getOrDefault(side, List.of()).forEach(mob -> tags.addAll(Fighters.tags(mob)));
    return tags;
  }

  private Box bounds() {
    return new Box(
        origin.getX() - HALF - 2,
        FLOOR_Y - 2,
        -HALF - 2,
        origin.getX() + HALF + 3,
        FLOOR_Y + HEIGHT + 3,
        HALF + 3);
  }

  private void clear() {
    for (final Entity entity :
        world.getEntitiesByClass(
            Entity.class, bounds().expand(8.0), e -> !(e instanceof PlayerEntity))) {
      entity.discard();
    }
    fighters.clear();
  }

  private void build() {
    final BlockState wall = Blocks.STONE_BRICKS.getDefaultState();
    final BlockState floor = Blocks.STONE.getDefaultState();
    final BlockState air = Blocks.AIR.getDefaultState();
    for (int x = -HALF - 1; x <= HALF + 1; x++) {
      for (int z = -HALF - 1; z <= HALF + 1; z++) {
        final boolean edge = Math.abs(x) > HALF || Math.abs(z) > HALF;
        for (int y = 0; y <= HEIGHT + 1; y++) {
          final BlockPos pos = origin.add(x, y, z);
          final BlockState state =
              y == 0 ? (edge ? wall : floor) : y == HEIGHT + 1 || edge ? wall : air;
          if (!world.getBlockState(pos).equals(state)) {
            world.setBlockState(pos, state);
          }
        }
      }
    }
  }
}
