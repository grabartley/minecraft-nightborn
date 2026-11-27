package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildRumour;
import com.grahambartley.nightborn.guild.rite.RiteSteps.Way;
import com.grahambartley.nightborn.network.PlayerCues;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.structure.NearestStructures.Found;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class GuildRites {

  public static final TagKey<Block> HAZARDS =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(Nightborn.MOD_ID, "rite_hazards"));

  static final float LOOK_TURN = 30.0F;
  static final double LIGHT_LIFT = 0.02;
  static final int MOTE_TICKS = 4;
  static final int LIGHT_SPARE_TICKS = 20;

  private static final List<Sending> SENDINGS = new ArrayList<>();

  private static final class Sending {
    private final ServerPlayerEntity hunter;
    private final GuildMasterEntity master;
    private final ServerWorld world;
    private final Vec3d from;
    private final GuildRite rite;
    private final CompletableFuture<Optional<BlockPos>> search;
    private int age;
    private int leftAt = RiteSteps.NOT_YET;
    private int arrivedAt = RiteSteps.NOT_YET;
    private Way way = Way.SEARCHING;
    private BlockPos entrance = BlockPos.ORIGIN;
    private BlockPos landing = BlockPos.ORIGIN;
    private Optional<ZoneMarkEntity> light = Optional.empty();

    private Sending(
        final ServerPlayerEntity hunter,
        final GuildMasterEntity master,
        final ServerWorld world,
        final GuildRite rite,
        final CompletableFuture<Optional<BlockPos>> search) {
      this.hunter = hunter;
      this.master = master;
      this.world = world;
      this.from = hunter.getPos();
      this.rite = rite;
      this.search = search;
    }

    private boolean casting() {
      return arrivedAt == RiteSteps.NOT_YET;
    }
  }

  private GuildRites() {}

  static void register() {
    ServerTickEvents.END_SERVER_TICK.register(server -> tick());
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearAll());
  }

  public static void clearAll() {
    SENDINGS.forEach(GuildRites::dim);
    SENDINGS.clear();
  }

  public static void ask(final ServerPlayerEntity hunter, final GuildMasterEntity master) {
    if (!(master.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final Optional<GuildRite> rite = GuildRite.sending(world.getRegistryManager());
    if (rite.isEmpty()) {
      return;
    }
    if (busy(hunter, master)) {
      tell(hunter, master, "busy");
      return;
    }
    final long wait = RiteAccess.waitSeconds(now(world), RiteCooldownAttachment.readyAt(hunter));
    if (wait > 0) {
      hunter.sendMessage(
          Text.translatable("nightborn.guild.rite.wait", master.getDisplayName(), wait), false);
      return;
    }
    final CompletableFuture<Optional<BlockPos>> search = new CompletableFuture<>();
    GuildRumour.nearestHouse(
        world, master.getBlockPos(), hunter, nearest -> search.complete(nearest.map(Found::pos)));
    begin(hunter, master, rite.get(), search);
  }

  public static void begin(
      final ServerPlayerEntity hunter,
      final GuildMasterEntity master,
      final GuildRite rite,
      final CompletableFuture<Optional<BlockPos>> search) {
    if (!(master.getWorld() instanceof ServerWorld world) || busy(hunter, master)) {
      return;
    }
    final Sending sending = new Sending(hunter, master, world, rite, search);
    final GuildRite.Timing timing = rite.timing();
    sending.light =
        RiteLight.shine(
            world,
            sending.from,
            rite.light(),
            timing.castTicks() + timing.waitTicks() + timing.leaveTicks() + LIGHT_SPARE_TICKS);
    master.showPower(rite.clips().cast());
    RiteLight.sound(world, master.getPos(), rite.sounds().cast());
    hunter.sendMessage(
        Text.translatable("nightborn.guild.rite.begun", master.getDisplayName()), true);
    SENDINGS.add(sending);
  }

  public static boolean underway(final ServerPlayerEntity hunter) {
    return SENDINGS.stream().anyMatch(sending -> sending.hunter == hunter && sending.casting());
  }

  private static boolean busy(final ServerPlayerEntity hunter, final GuildMasterEntity master) {
    return SENDINGS.stream()
        .anyMatch(
            sending -> sending.hunter == hunter || (sending.master == master && sending.casting()));
  }

  private static void tick() {
    SENDINGS.removeIf(
        sending -> {
          final boolean goesOn =
              !sending.hunter.isRemoved()
                  && !sending.hunter.isDisconnected()
                  && (sending.casting() ? cast(sending) : shelter(sending));
          if (!goesOn) {
            dim(sending);
          }
          return !goesOn;
        });
  }

  private static boolean cast(final Sending sending) {
    final ServerPlayerEntity hunter = sending.hunter;
    final GuildMasterEntity master = sending.master;
    final GuildRite rite = sending.rite;
    if (RiteBreaks.broken(
        new RiteBreaks.Hunter(
            hunter.isAlive() && hunter.getWorld() == sending.world,
            hunter.hurtTime > 0,
            hunter.getPos().squaredDistanceTo(sending.from)),
        new RiteBreaks.Master(
            master.isAlive() && !master.isRemoved(),
            master.hurtTime > 0,
            master.getTarget() != null),
        rite.reach().breakDistance())) {
      return fail(sending, "broken");
    }
    master.getNavigation().stop();
    master.getLookControl().lookAt(hunter, LOOK_TURN, LOOK_TURN);
    find(sending);
    final boolean goesOn =
        switch (RiteSteps.at(sending.age, sending.leftAt, rite.timing(), sending.way)) {
          case FAIL_NONE -> fail(sending, "none");
          case FAIL_UNSAFE -> fail(sending, "unsafe");
          case BEGIN_LEAVE -> leave(sending);
          case DEPART -> depart(sending);
          case CAST, LEAVE -> glow(sending);
        };
    sending.age++;
    return goesOn;
  }

  private static void find(final Sending sending) {
    final GuildRite.Reach reach = sending.rite.reach();
    if (sending.way == Way.SEARCHING && sending.search.isDone()) {
      final Optional<BlockPos> entrance =
          sending.search.isCompletedExceptionally()
              ? Optional.empty()
              : sending.search.getNow(Optional.empty());
      sending.entrance = entrance.orElse(BlockPos.ORIGIN);
      sending.way = entrance.isPresent() ? Way.LOADING : Way.NONE;
    }
    if (sending.way == Way.LOADING || sending.way == Way.READY) {
      RiteTicket.keep(sending.world, sending.entrance);
    }
    if (sending.way == Way.LOADING
        && RiteTicket.loaded(sending.world, sending.entrance, reach.searchRadius())) {
      final Optional<BlockPos> landing =
          RiteLanding.choose(
              sending.entrance,
              reach.searchRadius(),
              reach.searchRise(),
              spot -> safe(sending.world, spot, reach));
      sending.landing = landing.orElse(BlockPos.ORIGIN);
      sending.way = landing.isPresent() ? Way.READY : Way.UNSAFE;
    }
  }

  private static boolean safe(
      final ServerWorld world, final BlockPos spot, final GuildRite.Reach reach) {
    return RiteLanding.stands(
            world.getBlockState(spot.down()),
            world.getBlockState(spot),
            world.getBlockState(spot.up()),
            state -> state.isIn(HAZARDS))
        && world
            .getEntitiesByClass(
                LivingEntity.class,
                new Box(spot).expand(reach.householdReach()),
                near -> !(near instanceof PlayerEntity) && Vampires.isVampire(near))
            .isEmpty();
  }

  private static boolean glow(final Sending sending) {
    if (sending.age % MOTE_TICKS == 0) {
      RiteLight.motes(sending.world, sending.from, sending.rite.light());
    }
    return true;
  }

  private static boolean leave(final Sending sending) {
    sending.leftAt = sending.age;
    PlayerCues.power(sending.hunter, sending.rite.clips().leave());
    RiteLight.sound(sending.world, sending.from, sending.rite.sounds().leave());
    return true;
  }

  private static boolean depart(final Sending sending) {
    final ServerPlayerEntity hunter = sending.hunter;
    final GuildRite rite = sending.rite;
    if (!safe(sending.world, sending.landing, rite.reach())) {
      return fail(sending, "unsafe");
    }
    final Vec3d to = Vec3d.ofBottomCenter(sending.landing);
    final int shieldSeconds = ServerConfigService.current().guild().riteShieldSeconds();
    RiteLight.motes(sending.world, sending.from, rite.light());
    dim(sending);
    hunter.teleport(sending.world, to.x, to.y, to.z, hunter.getYaw(), hunter.getPitch());
    hunter.fallDistance = 0.0F;
    Sanctuary.shelter(hunter, shieldSeconds);
    RiteCooldownAttachment.wait(
        hunter,
        RiteAccess.readyAt(
            now(sending.world), ServerConfigService.current().guild().riteCooldownSeconds()));
    sending.light =
        RiteLight.shine(
            sending.world,
            to,
            rite.light(),
            rite.timing().settleTicks()
                + shieldSeconds * RiteAccess.TICKS_PER_SECOND
                + LIGHT_SPARE_TICKS);
    RiteLight.sound(sending.world, to, rite.sounds().arrive());
    RiteLight.motes(sending.world, to, rite.light());
    sending.arrivedAt = sending.age;
    return true;
  }

  private static boolean shelter(final Sending sending) {
    final ServerPlayerEntity hunter = sending.hunter;
    final int sinceArrival = sending.age - sending.arrivedAt;
    if (RiteSteps.settles(sinceArrival, sending.rite.timing())) {
      PlayerCues.power(hunter, sending.rite.clips().arrive());
    }
    sending.light.ifPresent(
        light -> light.setPosition(hunter.getX(), hunter.getY() + LIGHT_LIFT, hunter.getZ()));
    sending.age++;
    return !RiteSteps.over(sinceArrival, sending.rite.timing(), Sanctuary.sheltered(hunter));
  }

  private static boolean fail(final Sending sending, final String why) {
    tell(sending.hunter, sending.master, why);
    sending.master.showPower("");
    return false;
  }

  private static void tell(
      final ServerPlayerEntity hunter, final GuildMasterEntity master, final String key) {
    hunter.sendMessage(
        Text.translatable("nightborn.guild.rite." + key, master.getDisplayName()), false);
  }

  private static void dim(final Sending sending) {
    sending.light.ifPresent(ZoneMarkEntity::discard);
    sending.light = Optional.empty();
  }

  private static long now(final ServerWorld world) {
    return world.getServer().getOverworld().getTime();
  }
}
