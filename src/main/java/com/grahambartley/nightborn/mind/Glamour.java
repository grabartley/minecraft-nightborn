package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.name.StageNames;
import com.grahambartley.nightborn.smoke.SmokePoof;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class Glamour {

  public static final AttachmentType<Long> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "glamour"),
          builder ->
              builder
                  .persistent(Codec.LONG)
                  .syncWith(PacketCodecs.VAR_LONG.cast(), AttachmentSyncPredicate.all()));

  private Glamour() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, base, taken, blocked) -> {
          if (on(entity) && taken > 0.0F) {
            end(entity);
          }
        });
  }

  public static boolean lasts(final long until, final long now) {
    return now < until;
  }

  public static boolean on(final Entity entity) {
    final Long until = entity.getAttached(TYPE);
    return until != null && lasts(until, entity.getWorld().getTime());
  }

  public static void start(final LivingEntity vampire, final GlamourPower power) {
    vampire.setAttached(TYPE, vampire.getWorld().getTime() + power.durationTicks());
    changed(vampire);
    if (vampire instanceof ServerPlayerEntity player) {
      player.sendMessage(Text.translatable("nightborn.glamour.start"), true);
    }
  }

  public static void tick(final LivingEntity vampire) {
    final Long until = vampire.getAttached(TYPE);
    if (until == null) {
      return;
    }
    if (!lasts(until, vampire.getWorld().getTime()) || FeedingSessions.isFeeding(vampire)) {
      end(vampire);
    }
  }

  public static void end(final LivingEntity vampire) {
    if (vampire.getAttached(TYPE) == null) {
      return;
    }
    vampire.removeAttached(TYPE);
    changed(vampire);
    SmokePoof.burst(vampire, SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE);
    if (vampire instanceof ServerPlayerEntity player) {
      player.sendMessage(Text.translatable("nightborn.glamour.end"), true);
    }
  }

  private static void changed(final LivingEntity vampire) {
    if (vampire instanceof ServerPlayerEntity player) {
      StageNames.changed(player);
    }
  }
}
