package com.grahambartley.nightborn.guild.party;

import com.grahambartley.nightborn.age.AgeNight;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.guild.raiding.RaidOrders;
import com.grahambartley.nightborn.guild.raiding.RaidRules;
import com.grahambartley.nightborn.guild.raiding.RaidingParties;
import com.grahambartley.nightborn.guild.raiding.RaidingParty;
import com.grahambartley.nightborn.house.House;
import com.grahambartley.nightborn.house.HousePlace;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.hunt.HuntAttachment;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.GlobalPos;

public final class PassingRaid {

  public static final double SIGHT = 32.0;
  static final String SEEN = "nightborn.raid.passing";

  private PassingRaid() {}

  public static boolean looks(final boolean pitched, final boolean night) {
    return !pitched && !night;
  }

  public static boolean targets(
      final boolean sameWorld,
      final double distance,
      final boolean lordOnline,
      final boolean vampire,
      final boolean quiet,
      final boolean underway) {
    return sameWorld && distance <= SIGHT && lordOnline && vampire && !quiet && !underway;
  }

  static boolean raids(
      final ServerWorld world,
      final GuildMasterEntity master,
      final HunterParty party,
      final List<HunterEntity> members) {
    final MinecraftServer server = world.getServer();
    final RaidingParties raids = RaidingParties.get(server);
    final Optional<RaidingParty> underway = raids.ledBy(master.getUuid());
    if (underway.isPresent()) {
      underway
          .flatMap(RaidingParty::place)
          .ifPresent(place -> master.setPositionTarget(place.pos(), PartyLeader.AREA_REACH));
      return true;
    }
    final long now = server.getOverworld().getTimeOfDay();
    if (members.isEmpty() || !looks(party.pitched(), NightTime.isNight(false, now))) {
      return false;
    }
    for (final House house : Houses.get(server).all()) {
      final Optional<GlobalPos> place = HousePlace.of(server, house);
      if (place.isEmpty()) {
        continue;
      }
      final ServerPlayerEntity lord = server.getPlayerManager().getPlayer(house.lord());
      if (targets(
          place.get().dimension() == world.getRegistryKey(),
          Math.sqrt(master.getBlockPos().getSquaredDistance(place.get().pos())),
          lord != null,
          lord != null && Vampires.isVampire(lord),
          lord != null && HuntAttachment.get(lord).quiet(AgeNight.index(now)),
          raids.against(house.lord()).isPresent())) {
        raid(raids, master, members, house.lord(), place.get(), now);
        lord.sendMessage(Text.translatable(SEEN), false);
        return true;
      }
    }
    return false;
  }

  private static void raid(
      final RaidingParties raids,
      final GuildMasterEntity master,
      final List<HunterEntity> members,
      final UUID lord,
      final GlobalPos place,
      final long now) {
    final RaidOrders orders =
        new RaidOrders(lord, RaidRules.nextDusk(now), Optional.of(place.pos()));
    members.forEach(member -> member.enlist(orders, member.kit()));
    raids.start(
        new RaidingParty(
            lord,
            members.stream().map(HunterEntity::getUuid).toList(),
            members.size(),
            orders.until(),
            Optional.of(place),
            Optional.of(master.getUuid())));
    master.setPositionTarget(place.pos(), PartyLeader.AREA_REACH);
  }

  public static void moveOn(final MinecraftServer server, final UUID leader) {
    for (final ServerWorld world : server.getWorlds()) {
      if (world.getEntity(leader) instanceof GuildMasterEntity master && master.party() != null) {
        final HunterParty party = master.party();
        master.lead(party.struck(PartyLeader.onward(master.getBlockPos(), master.getRandom())));
        return;
      }
    }
  }
}
