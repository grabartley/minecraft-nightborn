package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.mob.BossVampireEntity;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Lordship {

  private Lordship() {}

  public static int capacity(final ServerPlayerEntity lord) {
    return ServerConfigService.current().houses().members(AgeAttachment.get(lord).rank());
  }

  public static HouseRules.Invite judge(final ServerPlayerEntity lord, final LivingEntity guest) {
    final Houses houses = Houses.get(lord.getServer());
    final Optional<House> house = houses.ledBy(lord.getUuid());
    return HouseRules.invite(
        house.isPresent(),
        StageRanks.of(lord),
        house.map(House::size).orElse(0),
        capacity(lord),
        new HouseRules.Guest(
            Vampires.isVampire(guest),
            StageRanks.of(guest),
            house.filter(led -> led.belongs(guest.getUuid())).isPresent(),
            guest instanceof BossVampireEntity
                || guest instanceof VampireEntity vampire && vampire.householdTie().head(),
            guest instanceof PlayerEntity && houses.ledBy(guest.getUuid()).isPresent(),
            guest.getWorld() == lord.getWorld()
                && house
                    .flatMap(led -> HousePlace.of(lord.getServer(), led))
                    .filter(place -> place.dimension().equals(guest.getWorld().getRegistryKey()))
                    .isPresent()));
  }
}
