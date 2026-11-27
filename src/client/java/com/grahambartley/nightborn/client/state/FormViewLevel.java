package com.grahambartley.nightborn.client.state;

import com.grahambartley.nightborn.formview.FormView;
import com.grahambartley.nightborn.formview.FormViewTint;
import com.grahambartley.nightborn.formview.FormViewWash;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum FormViewLevel {
  FULL,
  REDUCED,
  OFF;

  static final float SOFTEN = 0.5F;

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  public String translationKey() {
    return "nightborn.client.form_views." + id();
  }

  public static Optional<FormViewLevel> byId(final String id) {
    return Arrays.stream(values()).filter(level -> level.id().equals(id)).findFirst();
  }

  public Optional<FormView> look(final FormView view) {
    return switch (this) {
      case FULL -> Optional.of(view);
      case OFF -> Optional.empty();
      case REDUCED ->
          Optional.of(
              new FormView(
                  view.form(),
                  view.priority(),
                  view.fadeInTicks(),
                  view.fadeOutTicks(),
                  view.darkness() * SOFTEN,
                  view.echo(),
                  view.tint()
                      .map(
                          tint ->
                              new FormViewTint(
                                  tint.color(), tint.centre() * SOFTEN, tint.edge() * SOFTEN)),
                  Optional.empty(),
                  view.wash()
                      .map(
                          wash ->
                              new FormViewWash(
                                  wash.color(),
                                  wash.amount() * SOFTEN,
                                  wash.saturation() + (1.0F - wash.saturation()) * SOFTEN,
                                  wash.blur() * SOFTEN,
                                  wash.drift() * SOFTEN))));
    };
  }
}
