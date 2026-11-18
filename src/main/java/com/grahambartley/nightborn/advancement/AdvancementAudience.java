package com.grahambartley.nightborn.advancement;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class AdvancementAudience {

  private AdvancementAudience() {}

  public static void announce(
      final PlayerManager players,
      final ServerPlayerEntity earner,
      final Identifier advancement,
      final Text message) {
    final boolean earnerIsVampire = Vampires.isVampire(earner);
    players.getServer().sendMessage(message);
    for (final ServerPlayerEntity listener : players.getPlayerList()) {
      if (hears(advancement, earnerIsVampire, listener == earner, Vampires.isVampire(listener))) {
        listener.sendMessage(message, false);
      }
    }
  }

  public static boolean hears(
      final Identifier advancement,
      final boolean earnerIsVampire,
      final boolean listenerIsEarner,
      final boolean listenerIsVampire) {
    return listenerIsEarner
        || listenerIsVampire
        || !earnerIsVampire
        || !advancement.getNamespace().equals(Nightborn.MOD_ID);
  }
}
