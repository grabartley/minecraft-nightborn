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
import com.grahambartley.nightborn.structure.NearestStructures.Found;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.Items;
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
    final GuildSettings settings = ServerConfigService.current().guild();
    final long wait = RiteAccess.waitSeconds(now(world), RiteCooldownAttachment.readyAt(hunter));
    final RiteAccess.Refusal refusal =
        RiteAccess.refusal(
            new RiteAccess.Standing(
                GuildMembership.isHunter(hunter),
                OathAttachment.get(hunter).rank(),
                emeralds(hunter),
                wait),
            new RiteAccess.Terms(settings.riteMinRank(), settings.ritePrice()));
    if (refusal != RiteAccess.Refusal.NONE) {
      hunter.sendMessage(refused(refusal, master, settings.ritePrice(), wait), false);
      return;
    }
    final CompletableFuture<Optional<BlockPos>> search = new CompletableFuture<>();
    GuildRumour.nearestHouse(
        world, master.getBlockPos(), hunter, nearest -> search.complete(nearest.map(Found::pos)));
    begin(hunter, master, rite.get(), search);
  }

  static Text refused(
      final RiteAccess.Refusal refusal,
      final GuildMasterEntity master,
      final int price,
      final long wait) {
    final Text teller = master.getDisplayName();
    return switch (refusal) {
      case WAIT -> Text.translatable("nightborn.guild.rite.wait", teller, RiteAccess.clock(wait));
      case PRICE -> Text.translatable("nightborn.guild.rite.price", teller, price);
      case RANK, NONE -> Text.translatable("nightborn.guild.rite.rank", teller);
    };
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
    hunter.sendMessage(
        Text.translatable("nightborn.guild.rite.begun", master.getDisplayName()), true);
    SENDINGS.add(sending);
  }

  public static boolean underway(final ServerPlayerEntity hunter) {
    return SENDINGS.stream().anyMatch(sending -> sending.hunter == hunter && sending.casting());
  }

  public static int emeralds(final ServerPlayerEntity hunter) {
    return hunter.getInventory().count(Items.EMERALD);
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
      return fail(sending, Text.translatable("nightborn.guild.rite.broken", teller(sending)));
    }
    master.getNavigation().stop();
    master.getLookControl().lookAt(hunter, LOOK_TURN, LOOK_TURN);
    find(sending);
    final boolean goesOn =
        switch (RiteSteps.at(sending.age, sending.leftAt, rite.timing(), sending.way)) {
          case FAIL_NONE ->
              fail(sending, Text.translatable("nightborn.guild.rite.none", teller(sending)));
          case FAIL_UNSAFE -> unsafe(sending);
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
      final Optional<BlockPos> landing = RiteGround.landing(sending.world, sending.entrance, reach);
      sending.landing = landing.orElse(BlockPos.ORIGIN);
      sending.way = landing.isPresent() ? Way.READY : Way.UNSAFE;
    }
  }

  private static boolean glow(final Sending sending) {
    final GuildRite.Sounds sounds = sending.rite.sounds();
    if (sending.leftAt == RiteSteps.NOT_YET) {
      RiteLight.sounds(sending.world, sending.from, sounds.cast(), sending.age);
    } else {
      RiteLight.sounds(sending.world, sending.from, sounds.leave(), sending.age - sending.leftAt);
    }
    if (sending.age % MOTE_TICKS == 0) {
      RiteLight.motes(sending.world, sending.from, sending.rite.light());
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
      return unsafe(sending);
    }
    if (!pay(hunter, settings.ritePrice())) {
      return fail(
          sending, refused(RiteAccess.Refusal.PRICE, sending.master, settings.ritePrice(), 0L));
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
                + settings.riteShieldSeconds() * RiteAccess.TICKS_PER_SECOND
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

  private static boolean pay(final ServerPlayerEntity hunter, final int price) {
    if (emeralds(hunter) < price) {
      return false;
    }
    hunter
        .getInventory()
        .remove(
            stack -> stack.isOf(Items.EMERALD),
            price,
            hunter.playerScreenHandler.getCraftingInput());
    return true;
  }

  private static boolean unsafe(final Sending sending) {
    return fail(sending, Text.translatable("nightborn.guild.rite.unsafe", teller(sending)));
  }

  private static boolean fail(final Sending sending, final Text why) {
    sending.hunter.sendMessage(why, false);
    sending.master.showPower("");
    if (sending.leftAt != RiteSteps.NOT_YET) {
      PlayerCues.stopPower(sending.hunter);
    }
    return false;
  }

  private static Text teller(final Sending sending) {
    return sending.master.getDisplayName();
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
