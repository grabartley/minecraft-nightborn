package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.trap.TrapAllies;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    final Set<UUID> hunters = new HashSet<>(Quarries.markers(target.getUuid(), now));
    hunters.addAll(Revelations.markers(target.getUuid(), now));
    final List<Marker> markers = new ArrayList<>();
    for (final UUID id : hunters) {
      final ServerPlayerEntity hunter = server.getPlayerManager().getPlayer(id);
      if (hunter != null) {
        markers.add(
            new Marker(
                attacker == hunter,
                attacker != hunter && TrapAllies.allied(hunter, attacker),
                Quarries.share(id, target.getUuid(), now),
                SeekGifts.alliedShare(HeldSeekGifts.of(hunter))));
      }
    }
    return MarkShare.raise(amount, MarkShare.total(markers));
  }
}
