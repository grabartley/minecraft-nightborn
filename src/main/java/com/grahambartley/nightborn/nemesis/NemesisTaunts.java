package com.grahambartley.nightborn.nemesis;

import com.grahambartley.nightborn.nemesis.NemesisLines.Moment;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class NemesisTaunts {

  static final String SAYS = "chat.type.text";

  private NemesisTaunts() {}

  public static void say(
      final MobEntity nemesis, final ServerPlayerEntity quarry, final Moment moment) {
    NemesisKinds.of(nemesis)
        .flatMap(found -> found.kind().lines().pick(moment, nemesis.getRandom()::nextInt))
        .ifPresent(
            line ->
                quarry.sendMessage(
                    Text.translatable(SAYS, nemesis.getDisplayName(), Text.translatable(line)),
                    false));
  }
}
