package com.grahambartley.nightborn.gore;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.gear.VampireBane;
import com.grahambartley.nightborn.network.GorePayload;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class GoreEvents {

  private GoreEvents() {}

  public static void register() {
    GoreParticles.register();
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, base, taken, blocked) -> {
          if (!blocked && taken > 0.0F) {
            send(
                entity,
                source,
                GoreRules.onHit(bleeds(entity), Vampires.isVampire(entity), blow(source)));
          }
        });
    ServerLivingEntityEvents.AFTER_DEATH.register(
        (entity, source) ->
            send(
                entity,
                source,
                GoreRules.onDeath(
                    bleeds(entity),
                    Vampires.isVampire(entity),
                    Staking.staked(source),
                    blow(source))));
  }

  private static boolean bleeds(final LivingEntity entity) {
    return Vampires.isVampire(entity) || BloodQuality.of(entity.getType()).isPresent();
  }

  private static GoreRules.Blow blow(final DamageSource source) {
    final ItemStack weapon = source.getWeaponStack();
    return new GoreRules.Blow(
        source.isOf(NightbornDamage.BITE),
        Staking.staked(source),
        (source.isDirect() && weapon != null && weapon.isIn(VampireBane.SILVER_WEAPONS))
            || VampireBane.ammo(source).isIn(VampireBane.SILVER_AMMO));
  }

  private static void send(
      final LivingEntity entity, final DamageSource source, final Optional<Gore> gore) {
    if (gore.isEmpty() || entity.getWorld().isClient()) {
      return;
    }
    final Entity from = source.getSource() != null ? source.getSource() : source.getAttacker();
    final float yaw =
        from == null
            ? Float.NaN
            : GoreRules.sprayYaw(entity.getX() - from.getX(), entity.getZ() - from.getZ());
    final GorePayload payload = new GorePayload(entity.getId(), gore.get(), yaw);
    if (entity instanceof ServerPlayerEntity player) {
      ServerPlayNetworking.send(player, payload);
    }
    PlayerLookup.tracking(entity).forEach(watcher -> ServerPlayNetworking.send(watcher, payload));
  }
}
