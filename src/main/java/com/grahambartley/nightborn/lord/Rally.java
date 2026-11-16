package com.grahambartley.nightborn.lord;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

public final class Rally {

  private Rally() {}

  public static List<MobEntity> call(final LivingEntity caster, final RallyPower power) {
    final LivingEntity foe = foe(caster);
    final Optional<BlockPos> home = home(caster);
    if (foe == null || home.isEmpty()) {
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
                        home.equals(home(mob)),
                        Downed.helpless(mob),
                        mob.distanceTo(caster)));
    for (final MobEntity vampire : court) {
      vampire.setAttacker(foe);
      vampire.setTarget(foe);
    }
    return court;
  }

  static Optional<BlockPos> home(final LivingEntity entity) {
    return entity instanceof VampireEntity vampire ? vampire.homeLife().home() : Optional.empty();
  }

  @Nullable
  static LivingEntity foe(final LivingEntity caster) {
    if (caster instanceof MobEntity mob) {
      return mob.getTarget();
    }
    return caster.getAttacking();
  }
}
