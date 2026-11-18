package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class HouseSuccession {

  static final String INHERITED = "nightborn.house.succession.inherited";
  static final String PASSED = "nightborn.house.succession.passed";
  static final String FELL = "nightborn.house.succession.fell";
  static final String DISBANDED = "nightborn.house.succession.disbanded";
  static final String HANDED = "nightborn.house.succession.handed";

  private HouseSuccession() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(HouseSuccession::afterDeath);
  }

  private static void afterDeath(final LivingEntity entity, final DamageSource source) {
    if (entity instanceof ServerPlayerEntity player && endsLord(source)) {
      lordLost(player.getServer(), player.getUuid());
    }
  }

  static boolean endsLord(final DamageSource source) {
    return Staking.staked(source) || source.isOf(NightbornDamage.SUN);
  }

  public static void noLongerVampire(final MinecraftServer server, final UUID vampire) {
    lordLost(server, vampire);
    final Houses houses = Houses.get(server);
    houses
        .of(vampire)
        .filter(house -> house.hasMember(vampire))
        .ifPresent(house -> houses.put(house.without(vampire)));
  }

  public static void lordLost(final MinecraftServer server, final UUID lord) {
    Houses.get(server)
        .ledBy(lord)
        .map(house -> HouseRanks.refreshed(server, house))
        .ifPresent(
            house ->
                Succession.heir(house)
                    .ifPresentOrElse(
                        heir -> pass(server, house, house.ledBy(heir, false), INHERITED),
                        () -> end(server, house, FELL)));
  }

  public static void pass(
      final MinecraftServer server, final House before, final House passed, final String heirKey) {
    final House after = HouseRanks.refreshed(server, passed);
    final int capacity = ServerConfigService.current().houses().members(after.rank(after.lord()));
    final House kept = Succession.trimmed(after, capacity);
    Houses.get(server).replace(before.lord(), kept);
    final Text newLord = HouseCommand.name(server, kept.lord());
    final ServerPlayerEntity heir = server.getPlayerManager().getPlayer(kept.lord());
    if (heir != null) {
      heir.sendMessage(Text.translatable(heirKey), false);
      heir.playSoundToPlayer(
          SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.PLAYERS, 1.0F, 0.6F);
    }
    tell(server, kept, kept.lord(), Text.translatable(PASSED, newLord));
    if (!kept.hasMember(before.lord())) {
      tell(server, before.lord(), Text.translatable(PASSED, newLord));
    }
  }

  public static void end(final MinecraftServer server, final House house, final String key) {
    Houses.get(server).remove(house.lord());
    house.everyone().forEach(member -> tell(server, member, Text.translatable(key)));
  }

  public static boolean handOver(final ServerPlayerEntity lord, final ServerPlayerEntity member) {
    final MinecraftServer server = lord.getServer();
    return Houses.get(server)
        .ledBy(lord.getUuid())
        .map(house -> HouseRanks.refreshed(server, house))
        .filter(house -> mayHandOver(house, member.getUuid()))
        .map(
            house -> {
              pass(server, house, house.ledBy(member.getUuid(), true), HANDED);
              return true;
            })
        .orElse(false);
  }

  public static boolean mayHandOver(final House house, final UUID member) {
    return house.players().contains(member)
        && HouseRules.mayFound(house.rank(member))
        && house.challenges().pending().isEmpty();
  }

  public static boolean disband(
      final MinecraftServer server, final UUID lord, final boolean operator) {
    return Houses.get(server)
        .ledBy(lord)
        .filter(
            house -> ChallengeRules.mayDisband(house.challenges().pending().isPresent(), operator))
        .map(
            house -> {
              end(server, house, DISBANDED);
              return true;
            })
        .orElse(false);
  }

  private static void tell(
      final MinecraftServer server, final House house, final UUID except, final Text message) {
    house
        .everyone()
        .filter(member -> !member.equals(except))
        .forEach(member -> tell(server, member, message));
  }

  private static void tell(final MinecraftServer server, final UUID member, final Text message) {
    final ServerPlayerEntity player = server.getPlayerManager().getPlayer(member);
    if (player != null) {
      player.sendMessage(message, false);
    }
  }
}
