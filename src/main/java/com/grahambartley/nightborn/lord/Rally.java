package com.grahambartley.nightborn.lord;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

public final class Rally {

  private Rally() {}

  public static List<MobEntity> call(final LivingEntity caster, final RallyPower power) {
    final LivingEntity foe = foe(caster);
    if (foe == null) {
      return List.of();
    }
    final List<MobEntity> court =
        caster
            .getWorld()
            .getEntitiesByClass(
                MobEntity.class,
                Box.of(caster.getPos(), power.range() * 2, power.range() * 2, power.range() * 2),
                mob ->
                    power.answers(
                        mob == caster,
                        Vampires.isVampire(mob),
                        Downed.helpless(mob),
                        mob.distanceTo(caster)));
    for (final MobEntity vampire : court) {
      vampire.setAttacker(foe);
      vampire.setTarget(foe);
    }
    return court;
  }

  @Nullable
  static LivingEntity foe(final LivingEntity caster) {
    if (caster instanceof MobEntity mob) {
      return mob.getTarget();
    }
    return caster.getAttacking();
  }
}
