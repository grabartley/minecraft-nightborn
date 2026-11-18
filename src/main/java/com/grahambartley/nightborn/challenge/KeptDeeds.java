package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.accessory.AccessoryItem;
import com.grahambartley.nightborn.accessory.AccessoryOwnership;
import com.grahambartley.nightborn.familiar.FamiliarRegistry;
import com.grahambartley.nightborn.occult.GhostlyFamiliars;
import com.grahambartley.nightborn.thrall.ThrallRegistry;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.ward.WardTags;
import java.util.List;
import java.util.Optional;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class KeptDeeds {

  private KeptDeeds() {}

  static void check(final ServerPlayerEntity player) {
    final MinecraftServer server = player.getServer();
    if (server == null) {
      return;
    }
    held(AccessoryOwnership.items(player).stream()
            .filter(AccessoryItem.class::isInstance)
            .map(Registries.ITEM::getId)
            .toList())
        .ifPresent(deed -> Deeds.record(player, deed));
    worn(AccessoryOwnership.worn(player).size()).ifPresent(deed -> Deeds.record(player, deed));
    staff(
            ThrallRegistry.of(server).count(player.getUuid()),
            GhostlyFamiliars.owned(player.getServerWorld(), player.getUuid()).size(),
            FamiliarRegistry.of(server).bondOf(player.getUuid()).isPresent())
        .ifPresent(deed -> Deeds.record(player, deed));
    crucifixes(
            Vampires.isVampire(player),
            player.getMainHandStack().isIn(WardTags.CRUCIFIXES),
            player.getOffHandStack().isIn(WardTags.CRUCIFIXES))
        .ifPresent(deed -> Deeds.record(player, deed));
  }

  static Optional<Deed> held(final List<Identifier> accessories) {
    if (accessories.isEmpty()) {
      return Optional.empty();
    }
    Deed deed = Deed.of(DeedKinds.ACCESSORIES_HELD).times(accessories.size());
    for (final Identifier accessory : accessories) {
      deed = deed.with(accessory.toString(), true);
    }
    return Optional.of(deed);
  }

  static Optional<Deed> worn(final int slots) {
    return slots > 0
        ? Optional.of(Deed.of(DeedKinds.ACCESSORIES_WORN).with(DeedKinds.SLOTS, slots))
        : Optional.empty();
  }

  static Optional<Deed> staff(final int thralls, final int ghosts, final boolean humanFamiliar) {
    final int kinds = (thralls > 0 ? 1 : 0) + (ghosts > 0 ? 1 : 0) + (humanFamiliar ? 1 : 0);
    return kinds > 0
        ? Optional.of(Deed.of(DeedKinds.STAFF_KEPT).with(DeedKinds.KINDS, kinds))
        : Optional.empty();
  }

  static Optional<Deed> crucifixes(
      final boolean vampire, final boolean mainHand, final boolean offHand) {
    return !vampire && mainHand && offHand
        ? Optional.of(Deed.of(DeedKinds.CRUCIFIXES_HELD))
        : Optional.empty();
  }
}
