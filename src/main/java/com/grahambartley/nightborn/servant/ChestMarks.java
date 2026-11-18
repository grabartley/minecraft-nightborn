package com.grahambartley.nightborn.servant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.math.GlobalPos;

public record ChestMarks(Optional<GlobalPos> fetch, Optional<GlobalPos> deliver) {

  public static final ChestMarks NONE = new ChestMarks(Optional.empty(), Optional.empty());

  public static final Codec<ChestMarks> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      GlobalPos.CODEC.optionalFieldOf("fetch").forGetter(ChestMarks::fetch),
                      GlobalPos.CODEC.optionalFieldOf("deliver").forGetter(ChestMarks::deliver))
                  .apply(instance, ChestMarks::new));

  public record Marked(ChestMarks marks, ChestMark mark) {}

  public Marked mark(final GlobalPos chest) {
    if (fetch.filter(chest::equals).isPresent()) {
      return new Marked(new ChestMarks(Optional.empty(), deliver), ChestMark.CLEARED);
    }
    if (deliver.filter(chest::equals).isPresent()) {
      return new Marked(new ChestMarks(fetch, Optional.empty()), ChestMark.CLEARED);
    }
    if (fetch.isEmpty()) {
      return new Marked(new ChestMarks(Optional.of(chest), deliver), ChestMark.FETCH);
    }
    return new Marked(new ChestMarks(fetch, Optional.of(chest)), ChestMark.DELIVER);
  }

  public ChestMarks without(final GlobalPos chest) {
    return new ChestMarks(
        fetch.filter(pos -> !pos.equals(chest)), deliver.filter(pos -> !pos.equals(chest)));
  }

  public Optional<GlobalPos> workChest() {
    return deliver.or(() -> fetch);
  }

  public boolean complete() {
    return fetch.isPresent() && deliver.isPresent();
  }
}
