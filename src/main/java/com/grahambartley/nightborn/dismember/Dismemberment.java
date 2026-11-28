package com.grahambartley.nightborn.dismember;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.DismembermentSettings;
import com.grahambartley.nightborn.fight.Blow;
import com.grahambartley.nightborn.fight.BlowReading;
import com.grahambartley.nightborn.fight.Cutting;
import com.grahambartley.nightborn.gore.Gore;
import com.grahambartley.nightborn.gore.GoreEvents;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public final class Dismemberment {

  public static final AttachmentType<BodyState> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "body"),
          builder ->
              builder
                  .persistent(BodyState.CODEC)
                  .syncWith(BodyState.PACKET_CODEC, AttachmentSyncPredicate.all()));

  static final SoundEvent CUT_SOUND = SoundEvents.ENTITY_SLIME_SQUISH;
  static final float CUT_PITCH = 0.5F;

  private Dismemberment() {}

  public static void register() {
    BodyZoneData.register();
    BodyRuleData.register();
    WoundSearing.register();
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, base, taken, blocked) -> {
          if (!blocked && taken > 0.0F) {
            onHit(entity, source, taken);
          }
        });
    ServerPlayerEvents.COPY_FROM.register(
        (oldPlayer, newPlayer, alive) -> {
          if (!alive) {
            keep(newPlayer, state(oldPlayer).afterDeath());
          }
        });
  }

  public static BodyState state(final LivingEntity entity) {
    return entity.getAttachedOrElse(TYPE, BodyState.WHOLE);
  }

  public static boolean missing(final LivingEntity entity, final BodyPart part) {
    return state(entity).missing(part);
  }

  private static void onHit(
      final LivingEntity victim, final DamageSource source, final float taken) {
    if (victim.getWorld().isClient()) {
      return;
    }
    final DismembermentSettings settings = ServerConfigService.current().dismemberment();
    final DynamicRegistryManager registries = victim.getWorld().getRegistryManager();
    final Entity attacker = source.getAttacker();
    final Blow blow =
        BlowReading.read(
            source,
            attacker == null
                ? Cutting.WEAPON
                : BodyRuleData.rule(registries, attacker.getType()).cutting());
    final BodyState body = state(victim);
    final long now = victim.getWorld().getTime();
    final SeverRules.Victim facts =
        new SeverRules.Victim(
            BodyRuleData.rule(registries, victim.getType()),
            victim instanceof PlayerEntity,
            victim.getMaxHealth(),
            victim.getHealth(),
            Holds.staggered(victim),
            body.cutRecently(now, SeverRules.CUT_COOLDOWN_TICKS));
    if (!SeverRules.severs(
        settings,
        facts,
        blow,
        attacker instanceof PlayerEntity,
        PowerReach.pvp(victim),
        taken,
        victim.getRandom().nextDouble())) {
      return;
    }
    SeverRules.part(
            BlowAim.aimed(victim, source, BodyZoneData.zones(registries), victim.getRandom()), body)
        .ifPresent(part -> sever(victim, part, GoreEvents.sprayYaw(victim, source)));
  }

  public static void sever(final LivingEntity victim, final BodyPart part) {
    sever(victim, part, Float.NaN);
  }

  private static void sever(final LivingEntity victim, final BodyPart part, final float yaw) {
    victim.setAttached(
        TYPE,
        state(victim)
            .cut(
                part,
                victim.getWorld().getTime(),
                BleedRules.bleedTicks(
                    ServerConfigService.current().dismemberment(), Vampires.isVampire(victim))));
    GoreEvents.spray(victim, Gore.SEVER, yaw);
    victim
        .getWorld()
        .playSound(
            null, victim.getBlockPos(), CUT_SOUND, victim.getSoundCategory(), 1.0F, CUT_PITCH);
  }

  public static void restore(final LivingEntity entity) {
    entity.removeAttached(TYPE);
  }

  private static void keep(final LivingEntity entity, final BodyState body) {
    if (body.whole()) {
      entity.removeAttached(TYPE);
    } else {
      entity.setAttached(TYPE, body);
    }
  }
}
