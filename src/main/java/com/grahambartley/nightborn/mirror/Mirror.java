package com.grahambartley.nightborn.mirror;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.mind.Glamour;
import com.grahambartley.nightborn.network.MirrorPayload;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class Mirror {

  static final String RAISED_AT_YOU = "nightborn.mirror.raised_at_you";

  private Mirror() {}

  public static boolean reflects(final boolean vampire) {
    return !vampire;
  }

  public static boolean showsSelf(final boolean sneaking) {
    return sneaking;
  }

  public static void raise(final ServerPlayerEntity user, final LivingEntity subject) {
    ServerPlayNetworking.send(
        user,
        new MirrorPayload(
            subject.getId(), reflects(Vampires.isVampire(subject) && !Glamour.on(subject))));
    user.getServerWorld()
        .playSound(
            null,
            user.getBlockPos(),
            SoundEvents.ITEM_SPYGLASS_USE,
            SoundCategory.PLAYERS,
            1.0F,
            1.0F);
    if (subject == user && Vampires.isVampire(user)) {
      Deeds.record(user, Deed.of(DeedKinds.MIRROR_SELF));
    }
    if (subject != user && subject instanceof ServerPlayerEntity watched) {
      watched.sendMessage(Text.translatable(RAISED_AT_YOU, user.getDisplayName()), true);
      if (!reflects(Vampires.isVampire(watched))) {
        Deeds.record(
            user,
            Deed.of(DeedKinds.SEEN_THROUGH).with(DeedKinds.VAMPIRE, watched.getUuidAsString()));
      }
    }
  }
}
