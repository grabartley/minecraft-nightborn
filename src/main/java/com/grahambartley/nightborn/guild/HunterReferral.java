package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.servant.job.Bearing;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class HunterReferral {

  static final double REACH = 32.0;

  private HunterReferral() {}

  static boolean answers(final boolean alive, final boolean refers, final boolean vampire) {
    return alive && refers && !vampire;
  }

  static void refer(final HunterEntity hunter, final ServerPlayerEntity player) {
    if (!(hunter.getWorld() instanceof ServerWorld world)) {
      return;
    }
    hunter.getLookControl().lookAt(player);
    hunter.playAmbientSound();
    player.sendMessage(
        line(
            hunter.getDisplayName(),
            master(world, hunter).map(found -> new Pointed(found, hunter.getPos()))),
        false);
  }

  public static Optional<GuildMasterEntity> master(
      final ServerWorld world, final HunterEntity hunter) {
    return hunter
        .partyLeader()
        .map(world::getEntity)
        .filter(GuildMasterEntity.class::isInstance)
        .map(GuildMasterEntity.class::cast)
        .filter(GuildMasterEntity::isAlive)
        .or(
            () ->
                world
                    .getEntitiesByClass(
                        GuildMasterEntity.class,
                        new Box(hunter.getBlockPos()).expand(REACH),
                        GuildMasterEntity::isAlive)
                    .stream()
                    .min(Comparator.comparingDouble(found -> found.squaredDistanceTo(hunter))));
  }

  static Text line(final Text hunter, final Optional<Pointed> master) {
    return master
        .<Text>map(
            pointed ->
                Text.translatable(
                    "nightborn.hunter.referral",
                    hunter,
                    pointed.name(),
                    Text.translatable(Bearing.of(pointed.dx(), pointed.dz()).translationKey())))
        .orElseGet(() -> Text.translatable("nightborn.hunter.referral.none", hunter));
  }

  record Pointed(Text name, double dx, double dz) {

    Pointed(final GuildMasterEntity master, final Vec3d from) {
      this(master.getDisplayName(), master.getX() - from.x, master.getZ() - from.z);
    }
  }
}
