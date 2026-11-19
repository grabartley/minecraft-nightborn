package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.mind.Glamour;
import com.grahambartley.nightborn.occult.MistForm;
import com.grahambartley.nightborn.vanish.VanishAttachment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;

public final class Veils {

  private Veils() {}

  public static boolean veils(final boolean shadowVeil, final boolean mist, final boolean glamour) {
    return shadowVeil || mist || glamour;
  }

  public static boolean hides(final boolean veiled, final boolean vanished) {
    return veiled || vanished;
  }

  public static boolean veiled(final LivingEntity entity) {
    return veils(
        entity.hasStatusEffect(StatusEffects.INVISIBILITY),
        MistForm.on(entity),
        Glamour.on(entity));
  }

  public static boolean hidden(final LivingEntity entity) {
    return hides(veiled(entity), entity.getAttached(VanishAttachment.TYPE) != null);
  }

  public static void lift(final LivingEntity vampire) {
    vampire.removeStatusEffect(StatusEffects.INVISIBILITY);
    MistForm.fail(vampire);
    Glamour.end(vampire);
  }
}
