package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.servant.job.Bearing;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class HunterReferral {

  static final double REACH = 32.0;
  static final long REST = 40;
  public static final TagKey<EntityType<?>> NEVER_REFERS =
      TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, "never_refers"));

  private HunterReferral() {}

  static boolean answers(
      final boolean alive, final boolean refers, final boolean vampire, final boolean hunted) {
    return alive && refers && !vampire && !hunted;
  }

  static boolean rested(final long now, final long last) {
    return now - last >= REST;
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
                        found ->
                            found.isAlive() && found.squaredDistanceTo(hunter) <= REACH * REACH)
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
