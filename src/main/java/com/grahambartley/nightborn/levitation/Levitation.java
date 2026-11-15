package com.grahambartley.nightborn.levitation;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.network.ToggleLevitationPayload;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.weakened.Weakened;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class Levitation {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "levitating");

  public static final AttachmentType<Boolean> TYPE =
      AttachmentRegistry.create(
          ID,
          builder ->
              builder
                  .persistent(Codec.BOOL)
                  .syncWith(PacketCodecs.BOOL.cast(), AttachmentSyncPredicate.all()));

  static final String REFUSED = "nightborn.levitation.refused";

  private Levitation() {}

  public static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        ToggleLevitationPayload.ID, (payload, context) -> request(context.player()));
  }

  public static void request(final ServerPlayerEntity player) {
    if (!Vampires.isVampire(player) || player.isSpectator() || !player.isAlive()) {
      return;
    }
    if (isLevitating(player)) {
      land(player);
    } else if (!start(player)) {
      player.sendMessage(Text.translatable(REFUSED), true);
    }
  }

  public static boolean isLevitating(final LivingEntity entity) {
    return entity.getAttachedOrElse(TYPE, false);
  }

  public static boolean start(final LivingEntity vampire) {
    if (!LevitationRules.mayLevitate(
        Vampires.isVampire(vampire), weakness(vampire), Downed.state(vampire) != null)) {
      return false;
    }
    vampire.setAttached(TYPE, true);
    return true;
  }

  public static void land(final LivingEntity entity) {
    if (entity.hasAttached(TYPE)) {
      entity.removeAttached(TYPE);
    }
  }

  public static void tick(final LivingEntity vampire, final int weakened, final boolean downed) {
    if (!isLevitating(vampire)) {
      return;
    }
    if (LevitationRules.mayLevitate(true, weakened, downed)) {
      vampire.fallDistance = 0.0F;
    } else {
      land(vampire);
    }
  }

  private static int weakness(final LivingEntity vampire) {
    return Weakened.sources(vampire).level(Downed.state(vampire) != null);
  }
}
