package com.grahambartley.nightborn.house.duel;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Identifier;

public final class DuelGuard {

  static final Identifier PHASE = Identifier.of(Nightborn.MOD_ID, "house_duel");

  private DuelGuard() {}

  static void register() {
    ServerLivingEntityEvents.ALLOW_DAMAGE.addPhaseOrdering(PHASE, Event.DEFAULT_PHASE);
    ServerLivingEntityEvents.ALLOW_DAMAGE.register(PHASE, DuelGuard::allowDamage);
    ServerLivingEntityEvents.ALLOW_DEATH.addPhaseOrdering(PHASE, Event.DEFAULT_PHASE);
    ServerLivingEntityEvents.ALLOW_DEATH.register(
        PHASE, (entity, source, amount) -> !HouseDuels.downed(entity));
  }

  static boolean allowDamage(
      final LivingEntity target, final DamageSource source, final float amount) {
    final Optional<HouseDuel> targetDuel = HouseDuels.of(target.getUuid());
    final Entity attacker = source.getAttacker();
    final Optional<HouseDuel> attackerDuel =
        attacker == null ? Optional.empty() : HouseDuels.of(attacker.getUuid());
    return DuelRules.allowsHarm(
        targetDuel.isPresent(),
        attackerDuel.isPresent(),
        attacker != null
            && targetDuel
                .flatMap(duel -> duel.foe(target.getUuid()))
                .filter(attacker.getUuid()::equals)
                .isPresent(),
        attacker != null);
  }

  public static boolean forcesPvp(final UUID one, final UUID other) {
    return HouseDuels.of(one).flatMap(duel -> duel.foe(one)).filter(other::equals).isPresent();
  }
}
