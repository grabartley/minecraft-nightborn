package com.grahambartley.nightborn.faith;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.coating.Coatings;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public final class Kindling {

  public static final ComponentType<Flame> FLAME =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(Nightborn.MOD_ID, "blood_fire"),
          ComponentType.<Flame>builder()
              .codec(Flame.CODEC)
              .packetCodec(Flame.PACKET_CODEC)
              .build());

  static final int GLOW_TICKS = 2;
  static final int SWEEP_TICKS = 20;

  private Kindling() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(Kindling::afterDamage);
    ServerTickEvents.END_SERVER_TICK.register(Kindling::sweep);
  }

  public static boolean affords(final LivingEntity caster, final KindlePower power) {
    return KindleRules.affords(caster.getHealth(), power.cost());
  }

  public static void light(final LivingEntity caster, final KindlePower power) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    caster.setHealth(caster.getHealth() - power.cost());
    world.playSound(
        null,
        caster.getBlockPos(),
        SoundEvents.ENTITY_PLAYER_HURT,
        SoundCategory.PLAYERS,
        1.0F,
        0.8F);
    caster.getMainHandStack().set(FLAME, power.flame(world.getTime()));
    world.spawnParticles(
        BladeFlames.FLAME,
        caster.getX(),
        caster.getBodyY(0.6),
        caster.getZ(),
        20,
        0.3,
        0.4,
        0.3,
        0.02);
  }

  public static Optional<Flame> burning(final ItemStack stack, final long now) {
    return Optional.ofNullable(stack.get(FLAME)).filter(flame -> flame.burning(now));
  }

  public static float strike(final ItemStack weapon, final Entity target) {
    if (target.isFireImmune()
        || target instanceof LivingEntity living
            && living.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
      return 0.0F;
    }
    return burning(weapon, target.getWorld().getTime()).map(Flame::damage).orElse(0.0F);
  }

  public static float heal(final LivingEntity entity, final float amount) {
    return KindleRules.heal(amount, FaithMarks.SEARED.on(entity), entity.isOnFire());
  }

  public static void tick(final LivingEntity holder) {
    if (!(holder.getWorld() instanceof ServerWorld world) || world.getTime() % GLOW_TICKS != 0) {
      return;
    }
    final ItemStack held = holder.getMainHandStack();
    if (!held.contains(FLAME)) {
      return;
    }
    if (burning(held, world.getTime()).isEmpty()) {
      held.remove(FLAME);
      return;
    }
    BladeFlames.emit(world, holder);
  }

  static void afterDamage(
      final LivingEntity target,
      final DamageSource source,
      final float baseDamage,
      final float damage,
      final boolean blocked) {
    final ItemStack weapon = source.getWeaponStack();
    if (blocked || weapon == null || !(source.getAttacker() instanceof LivingEntity)) {
      return;
    }
    burning(weapon, target.getWorld().getTime())
        .filter(
            flame -> KindleRules.sets(target.isFireImmune(), source.isIn(Coatings.WEAPON_STRIKES)))
        .ifPresent(
            flame -> {
              target.setFireTicks(Math.max(target.getFireTicks(), flame.burnTicks()));
              if (KindleRules.sears(Vampires.isVampire(target))) {
                FaithMarks.SEARED.set(target, flame.burnTicks());
              }
            });
  }

  private static void sweep(final MinecraftServer server) {
    if (server.getTicks() % SWEEP_TICKS != 0) {
      return;
    }
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      final long now = player.getServerWorld().getTime();
      final PlayerInventory inventory = player.getInventory();
      for (int slot = 0; slot < inventory.size(); slot++) {
        final ItemStack stack = inventory.getStack(slot);
        if (stack.contains(FLAME) && burning(stack, now).isEmpty()) {
          stack.remove(FLAME);
        }
      }
    }
  }
}
