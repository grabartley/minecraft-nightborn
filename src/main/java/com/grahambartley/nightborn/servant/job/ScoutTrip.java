package com.grahambartley.nightborn.servant.job;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ScoutTrip(
    BlockPos target, Identifier place, Optional<ScoutReport> report, boolean lost) {

  public static final Codec<ScoutTrip> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      BlockPos.CODEC.fieldOf("target").forGetter(ScoutTrip::target),
                      Identifier.CODEC.fieldOf("place").forGetter(ScoutTrip::place),
                      ScoutReport.CODEC.optionalFieldOf("report").forGetter(ScoutTrip::report),
                      Codec.BOOL.optionalFieldOf("lost", false).forGetter(ScoutTrip::lost))
                  .apply(instance, ScoutTrip::new));

  public static ScoutTrip to(final BlockPos target, final Identifier place) {
    return new ScoutTrip(target.toImmutable(), place, Optional.empty(), false);
  }

  public boolean returning() {
    return report.isPresent() || lost;
  }

  public ScoutTrip reported(final ScoutReport seen) {
    return new ScoutTrip(target, place, Optional.of(seen), false);
  }

  public ScoutTrip lostTheWay() {
    return new ScoutTrip(target, place, Optional.empty(), true);
  }

  public Text placeName() {
    return placeName(place);
  }

  public static Text placeName(final Identifier place) {
    return Text.translatableWithFallback(placeKey(place), place.getPath());
  }

  public static String placeKey(final Identifier place) {
    return "nightborn.scout.place." + place.getNamespace() + "." + place.getPath();
  }
}
