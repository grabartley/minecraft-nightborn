package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.sight.Sights;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class TrueSights {

  private static final Map<UUID, Seeing> ACTIVE = new HashMap<>();

  private TrueSights() {}

  static void register() {
    Sights.standing(TrueSights::show);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> ACTIVE.clear());
  }

  public static void see(final LivingEntity caster, final SightPower power) {
    if (!(caster instanceof ServerPlayerEntity hunter)) {
      return;
    }
    ACTIVE.put(hunter.getUuid(), Seeing.of(hunter.getServerWorld().getTime(), power));
    hunter.sendMessage(
        Text.translatable("nightborn.seeker.true_sight").formatted(Formatting.GOLD), true);
  }

  public static boolean seeing(final ServerPlayerEntity hunter) {
    final Seeing seeing = ACTIVE.get(hunter.getUuid());
    return seeing != null && seeing.lasts(hunter.getServerWorld().getTime());
  }

  private static void show(final ServerPlayerEntity hunter, final Map<Integer, Integer> shown) {
    final Seeing seeing = ACTIVE.get(hunter.getUuid());
    if (seeing == null) {
      return;
    }
    if (!seeing.lasts(hunter.getServerWorld().getTime())) {
      ACTIVE.remove(hunter.getUuid());
      return;
    }
    for (final LivingEntity vampire : Revelations.vampires(hunter, seeing.power().range())) {
      shown.put(vampire.getId(), seeing.power().colour());
      if (seeing.firstSight(vampire.getUuid())) {
        seen(hunter, vampire);
      }
    }
  }

  private static void seen(final ServerPlayerEntity hunter, final LivingEntity vampire) {
    if (vampire instanceof ServerPlayerEntity player) {
      player.sendMessage(
          Text.translatable("nightborn.seeker.seen_you").formatted(Formatting.DARK_RED), true);
    }
    if (Veils.hidden(vampire)) {
      Deeds.record(
          hunter,
          Deed.of(DeedKinds.SEEN_THROUGH).with(DeedKinds.VAMPIRE, vampire.getUuidAsString()));
    }
  }
}
