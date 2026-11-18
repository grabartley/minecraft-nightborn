package com.grahambartley.nightborn.guild.party;

import com.grahambartley.nightborn.config.PartySettings;
import com.grahambartley.nightborn.core.SurfaceSpots;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public final class HunterPartySpawner {

  public static final int NEAR_MIN = 24;
  public static final int NEAR_MAX = 48;
  public static final int SPREAD = 3;
  public static final double APART = 128.0;

  private HunterPartySpawner() {}

  public static void register() {
    ServerTickEvents.END_WORLD_TICK.register(HunterPartySpawner::tick);
  }

  private static void tick(final ServerWorld world) {
    if (!spawnsParties(world)) {
      return;
    }
    final PartySettings settings = ServerConfigService.current().parties();
    final long interval = (long) settings.partyMinutes() * PartyLeader.TICKS_PER_MINUTE;
    if (world.getTime() % interval != 0) {
      return;
    }
    final Random random = world.getRandom();
    if (!PartyRoll.succeeds(random::nextInt, settings.partyChance())) {
      return;
    }
    final List<ServerPlayerEntity> players =
        world.getPlayers(player -> !player.isSpectator() && player.isAlive());
    if (players.isEmpty()) {
      return;
    }
    gatherNear(world, players.get(random.nextInt(players.size())), random);
  }

  public static boolean spawnsParties(final ServerWorld world) {
    return world.getRegistryKey() == World.OVERWORLD
        && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)
        && world.getGameRules().getBoolean(GameRules.DO_PATROL_SPAWNING);
  }

  public static Optional<GuildMasterEntity> gatherNear(
      final ServerWorld world, final ServerPlayerEntity player, final Random random) {
    final PartySettings settings = ServerConfigService.current().parties();
    return spot(world, player.getBlockPos(), random)
        .filter(spot -> world.getBiome(spot).isIn(PartyData.BIOMES))
        .filter(spot -> noPartyNear(world, spot))
        .flatMap(
            spot ->
                gather(
                    world,
                    spot,
                    PartyRoll.size(
                        random::nextInt, settings.partyMinSize(), settings.partyMaxSize()),
                    random));
  }

  public static Optional<BlockPos> spot(
      final ServerWorld world, final BlockPos near, final Random random) {
    return SurfaceSpots.around(world, near, NEAR_MIN, NEAR_MAX, random);
  }

  private static boolean noPartyNear(final ServerWorld world, final BlockPos spot) {
    return world
        .getEntitiesByClass(
            GuildMasterEntity.class, new Box(spot).expand(APART), master -> master.party() != null)
        .isEmpty();
  }

  public static Optional<GuildMasterEntity> gather(
      final ServerWorld world, final BlockPos spot, final int hunters, final Random random) {
    final GuildMasterEntity master = place(world, GuildMobs.GUILD_MASTER, spot);
    if (master == null) {
      return Optional.empty();
    }
    master.setPersistent();
    final List<UUID> members = new ArrayList<>();
    for (int i = 0; i < hunters; i++) {
      final int x = spot.getX() + random.nextBetween(-SPREAD, SPREAD);
      final int z = spot.getZ() + random.nextBetween(-SPREAD, SPREAD);
      final BlockPos at =
          new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z), z);
      final HunterEntity hunter = place(world, GuildMobs.HUNTER, at);
      if (hunter != null) {
        hunter.join(master.getUuid());
        members.add(hunter.getUuid());
      }
    }
    master.lead(HunterParty.gathered(members, spot, world.getTime()));
    return Optional.of(master);
  }

  private static <T extends PathAwareEntity> T place(
      final ServerWorld world, final EntityType<T> type, final BlockPos at) {
    final T mob = type.create(world);
    if (mob == null) {
      return null;
    }
    mob.refreshPositionAndAngles(at, world.getRandom().nextFloat() * 360.0F, 0.0F);
    mob.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
    world.spawnEntityAndPassengers(mob);
    return mob;
  }
}
