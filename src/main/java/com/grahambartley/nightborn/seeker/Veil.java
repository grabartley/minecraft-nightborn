package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.mind.Glamour;
import com.grahambartley.nightborn.occult.MistForm;
import com.grahambartley.nightborn.vanish.VanishAttachment;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.Locale;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.StringIdentifiable;

public enum Veil implements StringIdentifiable {
  SHADOW_VEIL,
  MIST,
  GLAMOUR,
  VANISH;

  public static final Codec<Veil> CODEC = StringIdentifiable.createCodec(Veil::values);

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }

  public boolean on(final LivingEntity entity) {
    return switch (this) {
      case SHADOW_VEIL -> entity.hasStatusEffect(StatusEffects.INVISIBILITY);
      case MIST -> MistForm.on(entity);
      case GLAMOUR -> Glamour.on(entity);
      case VANISH -> entity.getAttached(VanishAttachment.TYPE) != null;
    };
  }

  public void lift(final LivingEntity entity) {
    switch (this) {
      case SHADOW_VEIL -> entity.removeStatusEffect(StatusEffects.INVISIBILITY);
      case MIST -> MistForm.fail(entity);
      case GLAMOUR -> Glamour.end(entity);
      case VANISH -> {}
    }
  }

  public static boolean any(final Collection<Veil> veils, final LivingEntity entity) {
    return veils.stream().anyMatch(veil -> veil.on(entity));
  }

  public static void liftAll(final Collection<Veil> veils, final LivingEntity entity) {
    veils.stream().filter(veil -> veil.on(entity)).forEach(veil -> veil.lift(entity));
  }
}
