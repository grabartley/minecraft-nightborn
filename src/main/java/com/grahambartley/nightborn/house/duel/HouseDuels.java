package com.grahambartley.nightborn.house.duel;

import com.grahambartley.nightborn.config.DownedSettings;
import com.grahambartley.nightborn.config.HouseSettings;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedRules;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.house.Challenge;
import com.grahambartley.nightborn.house.ChallengeRules;
import com.grahambartley.nightborn.house.House;
import com.grahambartley.nightborn.house.HouseSuccession;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.network.BossArenaPayload;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.VampireAttributes;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class HouseDuels {

  static final int REVIEW_TICKS = 20;
  static final int SEEN_FROM = 24;
  static final String BEGINS = "nightborn.house.duel.begins";
  static final String WON = "nightborn.house.duel.won";
  static final String LOST = "nightborn.house.duel.lost";
  static final String HELD = "nightborn.house.duel.held";
  static final String REPELLED = "nightborn.house.duel.repelled";
  static final String UNANSWERED = "nightborn.house.duel.unanswered";

  private static final Map<UUID, Fight> FIGHTS = new HashMap<>();

  private record Fight(HouseDuel duel, int shown, DuelSeal seal, Set<UUID> told) {}

  private HouseDuels() {}

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(HouseDuels::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> FIGHTS.clear());
    DuelGuard.register();
  }

  public static Optional<HouseDuel> of(final UUID fighter) {
    return FIGHTS.values().stream()
        .map(Fight::duel)
        .filter(duel -> duel.involves(fighter))
        .findFirst();
  }

  static void tick(final MinecraftServer server) {
    for (final Fight fight : Map.copyOf(FIGHTS).values()) {
      hold(server, fight);
    }
    if (server.getTicks() % REVIEW_TICKS == 0) {
      review(server);
    }
  }

  private static void review(final MinecraftServer server) {
    final HouseSettings settings = ServerConfigService.current().houses();
    final long night = night(server);
    for (final House house : Houses.get(server).all()) {
      if (FIGHTS.containsKey(house.lord())) {
        continue;
      }
      house
          .challenges()
          .pending()
          .ifPresent(challenge -> answer(server, house, challenge, night, settings));
    }
  }

  private static void answer(
      final MinecraftServer server,
      final House house,
      final Challenge challenge,
      final long night,
      final HouseSettings settings) {
    final ServerPlayerEntity lord = server.getPlayerManager().getPlayer(house.lord());
    final ServerPlayerEntity challenger =
        server.getPlayerManager().getPlayer(challenge.challenger());
    final Optional<Double> apart =
        ready(lord) && ready(challenger) && lord.getWorld() == challenger.getWorld()
            ? Optional.of((double) lord.distanceTo(challenger))
            : Optional.empty();
    final ChallengeRules.Review review =
        ChallengeRules.review(
            challenge,
            night,
            ready(challenger),
            settings.answerNights(),
            apart,
            settings.duelReach());
    if (!review.challenge().equals(challenge)) {
      Houses.get(server).put(house.withChallenges(house.challenges().declared(review.challenge())));
    }
    switch (review.outcome()) {
      case UNANSWERED ->
          HouseSuccession.pass(
              server, house, house.ledBy(challenge.challenger(), true), UNANSWERED);
      case MEET -> begin(lord, challenger, settings);
      case WAITING -> {}
    }
  }

  private static boolean ready(final ServerPlayerEntity player) {
    return player != null
        && player.isAlive()
        && !player.isSpectator()
        && Vampires.isVampire(player)
        && !Downed.helpless(player);
  }

  private static void begin(
      final ServerPlayerEntity lord,
      final ServerPlayerEntity challenger,
      final HouseSettings settings) {
    final HouseDuel duel =
        new HouseDuel(
            lord.getUuid(),
            challenger.getUuid(),
            lord.getWorld().getRegistryKey(),
            DuelRules.circle(lord.getPos(), challenger.getPos(), settings.duelRadius()));
    final Fight fight = new Fight(duel, lord.getId(), new DuelSeal(), new HashSet<>());
    FIGHTS.put(lord.getUuid(), fight);
    show(lord.getServerWorld(), fight);
    for (final ServerPlayerEntity fighter : new ServerPlayerEntity[] {lord, challenger}) {
      FeedingSessions.end(fighter);
      fighter.sendMessage(Text.translatable(BEGINS), false);
    }
    lord.getServerWorld()
        .playSound(
            null,
            duel.arena().centre(),
            SoundEvents.BLOCK_BELL_RESONATE,
            SoundCategory.PLAYERS,
            1.5F,
            0.5F);
  }

  private static void hold(final MinecraftServer server, final Fight fight) {
    final HouseDuel duel = fight.duel();
    final ServerWorld world = server.getWorld(duel.world());
    final ServerPlayerEntity lord = server.getPlayerManager().getPlayer(duel.lord());
    final ServerPlayerEntity challenger = server.getPlayerManager().getPlayer(duel.challenger());
    if (world == null
        || Houses.get(server)
            .ledBy(duel.lord())
            .flatMap(house -> house.challenges().pending())
            .isEmpty()) {
      finish(server, fight);
      return;
    }
    final boolean lordStanding = standing(lord, world);
    final Optional<Boolean> lordAbsent =
        ChallengeRules.lordLosesByAbsence(lordStanding, standing(challenger, world));
    if (lordAbsent.isPresent() || !lordStanding) {
      lordAbsent.ifPresent(
          lordLeft -> settle(server, duel, lordLeft ? duel.lord() : duel.challenger()));
      finish(server, fight);
      return;
    }
    fight.seal().hold(world, duel);
    if (server.getTicks() % REVIEW_TICKS == 0) {
      show(world, fight);
    }
  }

  private static boolean standing(final ServerPlayerEntity fighter, final ServerWorld world) {
    return fighter != null
        && fighter.isAlive()
        && fighter.getServerWorld() == world
        && Vampires.isVampire(fighter);
  }

  private static void show(final ServerWorld world, final Fight fight) {
    final BossArenaPayload shown =
        new BossArenaPayload(
            fight.shown(),
            Optional.of(fight.duel().arena()),
            true,
            Optional.empty(),
            Optional.empty());
    final double reach = fight.duel().arena().radius() + SEEN_FROM;
    for (final ServerPlayerEntity viewer :
        world.getPlayers(
            player ->
                player.squaredDistanceTo(
                        fight.duel().arena().midX(), player.getY(), fight.duel().arena().midZ())
                    <= reach * reach)) {
      if (fight.told().add(viewer.getUuid())) {
        ServerPlayNetworking.send(viewer, shown);
      }
    }
  }

  public static boolean downed(final LivingEntity loser) {
    final Optional<HouseDuel> duel = of(loser.getUuid());
    if (duel.isEmpty() || !(loser.getWorld() instanceof ServerWorld world)) {
      return false;
    }
    final MinecraftServer server = world.getServer();
    final DownedSettings downed = ServerConfigService.current().downed();
    Downed.clear(loser);
    loser.setHealth(
        DownedRules.recoveredHealth(
            loser.getMaxHealth(),
            downed,
            VampireAttributes.value(loser, VampireAttributes.RISE_HEALTH)));
    world.playSound(
        null,
        loser.getBlockPos(),
        SoundEvents.ENTITY_PLAYER_BIG_FALL,
        loser.getSoundCategory(),
        1.0F,
        0.7F);
    settle(server, duel.get(), loser.getUuid());
    FIGHTS.values().stream()
        .filter(fight -> fight.duel().equals(duel.get()))
        .findFirst()
        .ifPresent(fight -> finish(server, fight));
    return true;
  }

  private static void settle(final MinecraftServer server, final HouseDuel duel, final UUID loser) {
    final Houses houses = Houses.get(server);
    final Optional<House> house = houses.ledBy(duel.lord());
    if (house.isEmpty()) {
      return;
    }
    final UUID winner = duel.foe(loser).orElse(duel.lord());
    if (loser.equals(duel.lord())) {
      HouseSuccession.pass(server, house.get(), house.get().ledBy(duel.challenger(), true), WON);
      tell(server, loser, Text.translatable(LOST));
      return;
    }
    final long until =
        ChallengeRules.barredUntil(
            night(server), ServerConfigService.current().houses().repeatNights());
    houses.put(house.get().withChallenges(house.get().challenges().lost(duel.challenger(), until)));
    tell(server, winner, Text.translatable(HELD));
    tell(server, loser, Text.translatable(REPELLED));
  }

  private static void finish(final MinecraftServer server, final Fight fight) {
    FIGHTS.remove(fight.duel().lord());
    for (final UUID viewer : fight.told()) {
      final ServerPlayerEntity player = server.getPlayerManager().getPlayer(viewer);
      if (player != null) {
        ServerPlayNetworking.send(player, BossArenaPayload.left(fight.shown()));
      }
    }
  }

  private static void tell(final MinecraftServer server, final UUID player, final Text message) {
    final ServerPlayerEntity online = server.getPlayerManager().getPlayer(player);
    if (online != null) {
      online.sendMessage(message, false);
      online.playSoundToPlayer(SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.PLAYERS, 1.0F, 0.6F);
    }
  }

  public static long night(final MinecraftServer server) {
    return NightTime.night(server.getOverworld().getTimeOfDay());
  }
}
