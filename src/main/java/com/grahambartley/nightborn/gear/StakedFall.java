package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public final class StakedFall {

  public static final int FALL_TICKS = 60;

  static final AttachmentType<Boolean> STAKED =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "staked_fall"),
          builder -> builder.syncWith(PacketCodecs.BOOL.cast(), AttachmentSyncPredicate.all()));

  private StakedFall() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(
        (entity, source) -> {
          if (falls(Vampires.isVampire(entity), Staking.staked(source))) {
            entity.setAttached(STAKED, true);
          }
        });
  }

  public static boolean falls(final boolean vampire, final boolean staked) {
    return vampire && staked;
  }

  public static boolean staked(final Entity entity) {
    return entity.hasAttached(STAKED);
  }

  public static boolean over(final int deathTime) {
    return deathTime >= FALL_TICKS;
  }

  public static float deathRoll(final boolean staked, final float roll) {
    return staked ? 0.0F : roll;
  }

  public static boolean tick(final LivingEntity dying) {
    if (!staked(dying)) {
      return false;
    }
    dying.deathTime++;
    if (over(dying.deathTime) && !dying.getWorld().isClient() && !dying.isRemoved()) {
      dying.getWorld().sendEntityStatus(dying, EntityStatuses.ADD_DEATH_PARTICLES);
      dying.remove(Entity.RemovalReason.KILLED);
    }
    return true;
  }
}
