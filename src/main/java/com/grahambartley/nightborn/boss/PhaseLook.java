package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record PhaseLook(
    String idle,
    String walk,
    String form,
    String formEnter,
    String fly,
    String flyForm,
    String enterClip,
    Optional<Identifier> held,
    Optional<Identifier> skin) {

  public static final PhaseLook PLAIN =
      new PhaseLook("", "", "", "", "", "", "", Optional.empty(), Optional.empty());

  public static final Codec<PhaseLook> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING.optionalFieldOf("idle", "").forGetter(PhaseLook::idle),
                      Codec.STRING.optionalFieldOf("walk", "").forGetter(PhaseLook::walk),
                      Codec.STRING.optionalFieldOf("form", "").forGetter(PhaseLook::form),
                      Codec.STRING
                          .optionalFieldOf("form_enter", "")
                          .forGetter(PhaseLook::formEnter),
                      Codec.STRING.optionalFieldOf("fly", "").forGetter(PhaseLook::fly),
                      Codec.STRING.optionalFieldOf("fly_form", "").forGetter(PhaseLook::flyForm),
                      Codec.STRING
                          .optionalFieldOf("enter_clip", "")
                          .forGetter(PhaseLook::enterClip),
                      Identifier.CODEC.optionalFieldOf("held").forGetter(PhaseLook::held),
                      Identifier.CODEC.optionalFieldOf("skin").forGetter(PhaseLook::skin))
                  .apply(instance, PhaseLook::new));

  public String enters(final String fallback) {
    return enterClip.isEmpty() ? fallback : enterClip;
  }

  public ShownForm shown(
      final boolean flying, final boolean downed, final boolean changing, final String downedForm) {
    if (downed) {
      return new ShownForm("", "", downedForm.isEmpty() ? form : downedForm, false);
    }
    if (changing && !formEnter.isEmpty()) {
      return new ShownForm(idle, walk, formEnter, false);
    }
    if (flying && !fly.isEmpty()) {
      return new ShownForm(fly, fly, flyForm.isEmpty() ? form : flyForm, false);
    }
    return new ShownForm(idle, walk, form, false);
  }
}
