package com.grahambartley.nightborn.faith;

import com.grahambartley.nightborn.coating.Coatings;
import com.grahambartley.nightborn.skill.PowerSight;
import com.grahambartley.nightborn.trap.TrapAllies;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;

public final class Wielders {

  private Wielders() {}

  @Nullable
  public static LivingEntity of(final LivingEntity caster, final PowerFaith faith) {
    if (faith.blessing().isPresent()) {
      final LivingEntity ally = ally(caster, faith.blessing().get().range());
      return pick(armed(ally), armed(caster)).orElse(null);
    }
    return faith.kindle().isPresent() && armed(caster) != null ? caster : null;
  }

  static <T> Optional<T> pick(@Nullable final T first, @Nullable final T second) {
    return Optional.ofNullable(first).or(() -> Optional.ofNullable(second));
  }

  @Nullable
  private static LivingEntity ally(final LivingEntity caster, final double range) {
    if (caster instanceof MobEntity) {
      return null;
    }
    final LivingEntity looked = PowerSight.looked(caster, range);
    return looked != null
            && looked.isAlive()
            && caster.canSee(looked)
            && TrapAllies.allied(caster, looked)
        ? looked
        : null;
  }

  @Nullable
  private static LivingEntity armed(@Nullable final LivingEntity entity) {
    return entity != null && entity.getMainHandStack().isIn(Coatings.WEAPONS) ? entity : null;
  }
}
