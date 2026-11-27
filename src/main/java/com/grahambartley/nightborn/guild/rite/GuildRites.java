package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.config.GuildSettings;
import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildRumour;
import com.grahambartley.nightborn.guild.rite.RiteSteps.Way;
import com.grahambartley.nightborn.network.PlayerCues;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class GuildRites {

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
    private final CompletableFuture<Optional<RiteWay>> search;
    private int age;
    private int leftAt = RiteSteps.NOT_YET;
    private int arrivedAt = RiteSteps.NOT_YET;
    private Way way = Way.SEARCHING;
    private Optional<RiteWay> route = Optional.empty();
    private BlockPos landing = BlockPos.ORIGIN;
    private Optional<ZoneMarkEntity> light = Optional.empty();
    private boolean hunterStruck;
    private boolean masterStruck;

    private Sending(
        final ServerPlayerEntity hunter,
        final GuildMasterEntity master,
        final ServerWorld world,
        final GuildRite rite,
        final CompletableFuture<Optional<RiteWay>> search) {
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

    private Text teller() {
      return master.getDisplayName();
    }
  }

  private GuildRites() {}

  static void register() {
    ServerTickEvents.END_SERVER_TICK.register(server -> tick());
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearAll());
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (struck, source, dealt, taken, blocked) ->
            SENDINGS.forEach(
                sending -> {
                  sending.hunterStruck |= sending.hunter == struck;
                  sending.masterStruck |= sending.master == struck;
                }));
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
    final Text teller = master.getDisplayName();
    if (busy(hunter, master)) {
      hunter.sendMessage(RiteSpeech.Line.BUSY.by(teller), false);
      return;
    }
    final GuildSettings settings = ServerConfigService.current().guild();
    final long wait = RiteAccess.waitSeconds(now(world), RiteCooldownAttachment.readyAt(hunter));
    final Optional<Text> refused =
        RiteSpeech.refused(
            RiteAccess.refusal(
                new RiteAccess.Standing(
                    GuildMembership.isHunter(hunter),
                    OathAttachment.get(hunter).rank(),
                    RiteTithe.held(hunter),
                    wait),
                new RiteAccess.Terms(settings.riteMinRank(), settings.ritePrice())),
            teller,
            settings.ritePrice(),
            wait);
    if (refused.isPresent()) {
      hunter.sendMessage(refused.get(), false);
      return;
    }
    final GuildRite.Reach reach = rite.get().reach();
    final CompletableFuture<Optional<RiteWay>> search = new CompletableFuture<>();
    GuildRumour.nearestHouse(
        world,
        master.getBlockPos(),
        hunter,
        nearest ->
            search.complete(
                nearest.map(
                    house ->
                        new RiteWay(
                            house.pos(), loaded -> RiteGround.door(loaded, house, reach)))));
    begin(hunter, master, rite.get(), search);
  }

  public static void begin(
      final ServerPlayerEntity hunter,
      final GuildMasterEntity master,
      final GuildRite rite,
      final CompletableFuture<Optional<RiteWay>> search) {
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
    hunter.sendMessage(RiteSpeech.Line.BEGUN.by(sending.teller()), true);
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
            sending.hunterStruck,
            hunter.getPos().squaredDistanceTo(sending.from)),
        new RiteBreaks.Master(
            master.isAlive() && !master.isRemoved(),
            sending.masterStruck,
            master.getTarget() != null),
        rite.reach().breakDistance())) {
      return fail(sending, RiteSpeech.Line.BROKEN.by(sending.teller()));
    }
    master.getNavigation().stop();
    master.getLookControl().lookAt(hunter, LOOK_TURN, LOOK_TURN);
    find(sending);
    final boolean goesOn =
        switch (RiteSteps.at(sending.age, sending.leftAt, rite.timing(), sending.way)) {
          case FAIL_NONE -> fail(sending, RiteSpeech.Line.NONE.by(sending.teller()));
          case FAIL_UNSAFE -> fail(sending, RiteSpeech.Line.UNSAFE.by(sending.teller()));
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
      sending.route =
          sending.search.isCompletedExceptionally()
              ? Optional.empty()
              : sending.search.getNow(Optional.empty());
      sending.way = sending.route.isPresent() ? Way.LOADING : Way.NONE;
    }
    if (sending.route.isEmpty() || (sending.way != Way.LOADING && sending.way != Way.READY)) {
      return;
    }
    final RiteWay route = sending.route.get();
    RiteTicket.keep(sending.world, route.entrance());
    if (sending.way == Way.LOADING
        && RiteTicket.loaded(sending.world, route.entrance(), reach.searchRadius())) {
      final Optional<BlockPos> landing =
          route
              .door()
              .apply(sending.world)
              .flatMap(door -> RiteGround.landing(sending.world, door, reach));
      sending.landing = landing.orElse(BlockPos.ORIGIN);
      sending.way = landing.isPresent() ? Way.READY : Way.UNSAFE;
    }
  }

  private static boolean glow(final Sending sending) {
    final GuildRite rite = sending.rite;
    if (RiteSteps.repeats(sending.age, sending.leftAt, rite.timing())) {
      sending.master.showPower(rite.clips().cast());
    }
    if (sending.leftAt == RiteSteps.NOT_YET) {
      RiteLight.sounds(
          sending.world,
          sending.from,
          rite.sounds().cast(),
          RiteSteps.castTick(sending.age, rite.timing()));
    } else {
      RiteLight.sounds(
          sending.world, sending.from, rite.sounds().leave(), sending.age - sending.leftAt);
    }
    if (sending.age % MOTE_TICKS == 0) {
      RiteLight.motes(sending.world, sending.from, rite.light());
    }
    return true;
  }

  private static boolean leave(final Sending sending) {
    sending.leftAt = sending.age;
    PlayerCues.power(sending.hunter, sending.rite.clips().leave());
    return glow(sending);
  }

  private static boolean depart(final Sending sending) {
    final ServerPlayerEntity hunter = sending.hunter;
    final GuildRite rite = sending.rite;
    final GuildSettings settings = ServerConfigService.current().guild();
    if (!RiteGround.safe(sending.world, sending.landing, rite.reach())) {
      return fail(sending, RiteSpeech.Line.UNSAFE.by(sending.teller()));
    }
    if (!RiteTithe.pay(hunter, settings.ritePrice())) {
      return fail(
          sending,
          RiteSpeech.refused(RiteAccess.Refusal.PRICE, sending.teller(), settings.ritePrice(), 0L)
              .orElseThrow());
    }
    final Vec3d to = Vec3d.ofBottomCenter(sending.landing);
    RiteLight.motes(sending.world, sending.from, rite.light());
    dim(sending);
    hunter.teleport(sending.world, to.x, to.y, to.z, hunter.getYaw(), hunter.getPitch());
    hunter.fallDistance = 0.0F;
    Sanctuary.shelter(hunter, settings.riteShieldSeconds());
    RiteCooldownAttachment.wait(
        hunter, RiteAccess.readyAt(now(sending.world), settings.riteCooldownSeconds()));
    sending.light =
        RiteLight.shine(
            sending.world,
            to,
            rite.light(),
            rite.timing().settleTicks()
                + Sanctuary.ticks(settings.riteShieldSeconds())
                + LIGHT_SPARE_TICKS);
    RiteLight.motes(sending.world, to, rite.light());
    sending.arrivedAt = sending.age + 1;
    return true;
  }

  private static boolean shelter(final Sending sending) {
    final ServerPlayerEntity hunter = sending.hunter;
    final int sinceArrival = sending.age - sending.arrivedAt;
    RiteLight.sounds(sending.world, hunter.getPos(), sending.rite.sounds().arrive(), sinceArrival);
    if (RiteSteps.settles(sinceArrival, sending.rite.timing())) {
      PlayerCues.power(hunter, sending.rite.clips().arrive());
    }
    sending.light.ifPresent(
        light -> light.setPosition(hunter.getX(), hunter.getY() + LIGHT_LIFT, hunter.getZ()));
    sending.age++;
    return !RiteSteps.over(sinceArrival, sending.rite.timing(), Sanctuary.sheltered(hunter));
  }

  private static boolean fail(final Sending sending, final Text why) {
    sending.hunter.sendMessage(why, false);
    sending.master.showPower("");
    if (sending.leftAt != RiteSteps.NOT_YET) {
      PlayerCues.stopPower(sending.hunter);
    }
    return false;
  }

  private static void dim(final Sending sending) {
    sending.light.ifPresent(ZoneMarkEntity::discard);
    sending.light = Optional.empty();
  }

  private static long now(final ServerWorld world) {
    return world.getServer().getOverworld().getTime();
  }
}
