package com.grahambartley.nightborn.ward;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.mind.Glamour;
import com.grahambartley.nightborn.mob.Gestures;
import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;

public final class Flinch {

  private Flinch() {}

  public static boolean starts(final boolean wasInReach, final boolean inReach) {
    return !wasInReach && inReach;
  }

  public static boolean inReach(final int... strengths) {
    for (final int strength : strengths) {
      if (strength > 0) {
        return true;
      }
    }
    return false;
  }

  public static void show(
      final LivingEntity vampire, final boolean garlic, final boolean crucifix) {
    if (Glamour.on(vampire)) {
      return;
    }
    recoil(garlic, crucifix, small(GiftAttributes.value(vampire, GiftAttributes.SMALL_FLINCH)))
        .ifPresent(pose -> Gestures.show(vampire, pose));
  }

  public static Optional<VampirePose> recoil(
      final boolean garlic, final boolean crucifix, final boolean small) {
    if (!garlic && !crucifix) {
      return Optional.empty();
    }
    if (small) {
      return Optional.of(VampirePose.SMALL_FLINCH);
    }
    return Optional.of(crucifix ? VampirePose.RECOIL_CRUCIFIX : VampirePose.RECOIL_GARLIC);
  }

  public static boolean small(final double smallFlinch) {
    return smallFlinch > 0.0;
  }
}
