package com.grahambartley.nightborn.formview;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record FormView(
    Identifier form,
    int priority,
    int fadeInTicks,
    int fadeOutTicks,
    float darkness,
    Optional<FormViewEcho> echo,
    Optional<FormViewTint> tint,
    Optional<FormViewTremble> tremble,
    Optional<FormViewWash> wash) {

  private static final Codec<Integer> FADE = Codec.intRange(1, 200);

  public static final Codec<FormView> CODEC =
      RecordCodecBuilder.<FormView>create(
              instance ->
                  instance
                      .group(
                          Identifier.CODEC.fieldOf("form").forGetter(FormView::form),
                          Codec.INT.optionalFieldOf("priority", 0).forGetter(FormView::priority),
                          FADE.fieldOf("fade_in_ticks").forGetter(FormView::fadeInTicks),
                          FADE.fieldOf("fade_out_ticks").forGetter(FormView::fadeOutTicks),
                          Codec.floatRange(0.0F, 1.0F)
                              .optionalFieldOf("darkness", 0.0F)
                              .forGetter(FormView::darkness),
                          FormViewEcho.CODEC.optionalFieldOf("echo").forGetter(FormView::echo),
                          FormViewTint.CODEC.optionalFieldOf("tint").forGetter(FormView::tint),
                          FormViewTremble.CODEC
                              .optionalFieldOf("tremble")
                              .forGetter(FormView::tremble),
                          FormViewWash.CODEC.optionalFieldOf("wash").forGetter(FormView::wash))
                      .apply(instance, FormView::new))
          .validate(FormView::check);

  static DataResult<FormView> check(final FormView view) {
    if (view.echo.isEmpty()
        && view.tint.isEmpty()
        && view.tremble.isEmpty()
        && view.wash.isEmpty()
        && view.darkness == 0.0F) {
      return DataResult.error(
          () -> "the view for form " + view.form + " changes nothing: give it at least one part");
    }
    return DataResult.success(view);
  }
}
