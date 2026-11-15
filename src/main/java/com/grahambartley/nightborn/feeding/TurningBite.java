package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.network.ToggleTurningBitePayload;
import com.grahambartley.nightborn.vampire.Vampires;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class TurningBite {

  public static final AttachmentType<Boolean> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "turning_bite"),
          builder ->
              builder
                  .persistent(Codec.BOOL)
                  .copyOnDeath()
                  .syncWith(PacketCodecs.BOOL.cast(), AttachmentSyncPredicate.targetOnly()));

  static final String ON = "nightborn.turning_bite.on";
  static final String OFF = "nightborn.turning_bite.off";

  private TurningBite() {}

  public static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        ToggleTurningBitePayload.ID, (payload, context) -> toggle(context.player()));
  }

  public static boolean isOn(final AttachmentTarget target) {
    return target.getAttachedOrElse(TYPE, false);
  }

  public static void set(final AttachmentTarget target, final boolean on) {
    if (on) {
      target.setAttached(TYPE, true);
    } else {
      target.removeAttached(TYPE);
    }
  }

  public static boolean toggle(final ServerPlayerEntity player) {
    if (!Vampires.isVampire(player)) {
      return false;
    }
    final boolean on = !isOn(player);
    set(player, on);
    player.sendMessage(Text.translatable(messageKey(on)), true);
    return true;
  }

  static String messageKey(final boolean on) {
    return on ? ON : OFF;
  }
}
