package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.guild.GuildRumour;
import com.grahambartley.nightborn.servant.job.Bearing;
import com.grahambartley.nightborn.servant.job.ScoutChoices;
import com.grahambartley.nightborn.structure.NearestStructures;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class VillagerMutters {

  static final double EARSHOT = 8.0;
  static final int LINES = 4;

  private VillagerMutters() {}

  static NightLog listen(
      final ServerWorld world,
      final ServerPlayerEntity player,
      final NightLog log,
      final int range) {
    final long today = Math.floorDiv(world.getTimeOfDay(), NightTime.DAY_LENGTH);
    if (Vampires.isVampire(player) || log.mutteredOn(today)) {
      return log;
    }
    final Optional<VillagerEntity> near =
        world
            .getEntitiesByClass(
                VillagerEntity.class,
                player.getBoundingBox().expand(EARSHOT),
                villager -> !villager.isBaby() && !villager.isSleeping())
            .stream()
            .min(Comparator.comparingDouble(villager -> villager.squaredDistanceTo(player)));
    if (near.isEmpty()) {
      return log;
    }
    final VillagerEntity villager = near.get();
    final BlockPos from = villager.getBlockPos();
    final int line = world.getRandom().nextInt(LINES);
    NearestStructures.eachOf(world, GuildRumour.VAMPIRE_HOUSES, from, range)
        .thenAcceptAsync(
            found ->
                ScoutChoices.nearest(from, found, range, 1).stream()
                    .findFirst()
                    .filter(house -> villager.isAlive() && !player.isDisconnected())
                    .ifPresent(
                        house -> {
                          villager.playSound(
                              SoundEvents.ENTITY_VILLAGER_AMBIENT, 1.0F, villager.getSoundPitch());
                          player.sendMessage(
                              mutter(villager.getDisplayName(), from, house.pos(), line), false);
                        }),
            world.getServer());
    return log.mutteredTo(today);
  }

  static Text mutter(
      final Text villager, final BlockPos from, final BlockPos house, final int line) {
    return Text.translatable(
        "nightborn.villager.mutter." + Math.floorMod(line, LINES),
        villager,
        Text.translatable(
            Bearing.of(house.getX() - from.getX(), house.getZ() - from.getZ()).translationKey()));
  }
}
