package com.grahambartley.nightborn.eldest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.RitualSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Modifiers;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class EldestSet {

  static final Identifier VIGOUR = Identifier.of(Nightborn.MOD_ID, "eldest/set_vigour");
  static final int CHECK_TICKS = 20;
  static final int ARMOR_SLOTS = 4;

  private EldestSet() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(EldestSet::afterDamage);
    ServerTickEvents.END_SERVER_TICK.register(EldestSet::tick);
  }

  public static boolean complete(final int armorPieces, final boolean bladeHeld) {
    return armorPieces >= ARMOR_SLOTS && bladeHeld;
  }

  public static float stolen(
      final float dealt, final boolean blade, final boolean complete, final RitualSettings s) {
    if (!blade || dealt <= 0.0F) {
      return 0.0F;
    }
    return dealt * (complete ? s.setLifeSteal() : s.bladeLifeSteal()) / 100.0F;
  }

  static int armorPieces(final LivingEntity wearer) {
    int pieces = 0;
    for (final EquipmentSlot slot : EquipmentSlot.values()) {
      if (slot.isArmorSlot() && wearer.getEquippedStack(slot).isIn(EldestSpoils.SET_ARMOR)) {
        pieces++;
      }
    }
    return pieces;
  }

  static boolean wearsAll(final LivingEntity wearer) {
    return complete(armorPieces(wearer), wearer.getMainHandStack().isIn(EldestSpoils.SET_BLADES));
  }

  private static void afterDamage(
      final LivingEntity victim,
      final DamageSource source,
      final float base,
      final float taken,
      final boolean blocked) {
    if (!(source.getAttacker() instanceof LivingEntity attacker)
        || !source.isDirect()
        || attacker == victim) {
      return;
    }
    final float healed =
        stolen(
            taken,
            attacker.getMainHandStack().isIn(EldestSpoils.SET_BLADES),
            wearsAll(attacker),
            ServerConfigService.current().ritual());
    if (healed > 0.0F) {
      attacker.heal(healed);
    }
  }

  private static void tick(final MinecraftServer server) {
    if (server.getTicks() % CHECK_TICKS != 0) {
      return;
    }
    final int vigour = ServerConfigService.current().ritual().setHealth();
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      Modifiers.set(
          player,
          EntityAttributes.GENERIC_MAX_HEALTH,
          VIGOUR,
          wearsAll(player) ? vigour : 0.0,
          EntityAttributeModifier.Operation.ADD_VALUE);
      Modifiers.clampHealth(player);
    }
  }
}
