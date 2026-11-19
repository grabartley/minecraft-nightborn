package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.trap.TrapAllies;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MarkedBlows {

  private MarkedBlows() {}

  public static float scale(
      final LivingEntity target, final DamageSource source, final float amount) {
    final MinecraftServer server = target.getServer();
    if (server == null
        || !(source.getAttacker() instanceof LivingEntity attacker)
        || !Vampires.isVampire(target)) {
      return amount;
    }
    final long now = target.getWorld().getTime();
    final Set<UUID> markers = new HashSet<>();
    Quarries.all()
        .forEach(
            (hunter, mark) -> {
              if (mark.lasts(now) && mark.on(target.getUuid())) {
                markers.add(hunter);
              }
            });
    for (final UUID hunter : Revelations.hunters()) {
      Revelations.of(hunter)
          .filter(revealing -> revealing.lasts(now) && revealing.reveals(target.getUuid()))
          .ifPresent(revealing -> markers.add(hunter));
    }
    double share = 0.0;
    for (final UUID id : markers) {
      final ServerPlayerEntity hunter = server.getPlayerManager().getPlayer(id);
      if (hunter == null) {
        continue;
      }
      final QuarryMark mark = Quarries.all().get(id);
      final boolean quarry = mark != null && mark.lasts(now) && mark.on(target.getUuid());
      share +=
          MarkShare.of(
              attacker == hunter,
              attacker != hunter && TrapAllies.allied(hunter, attacker),
              quarry ? mark.share() : 0.0,
              SeekGifts.alliedShare(HeldSeekGifts.of(hunter)));
    }
    return MarkShare.raise(amount, share);
  }
}
