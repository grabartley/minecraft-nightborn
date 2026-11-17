package com.grahambartley.nightborn.name;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

public record NamePool(List<String> names) {

  public static final NamePool EMPTY = new NamePool(List.of());

  public NamePool {
    names = List.copyOf(names);
  }

  public static final Codec<NamePool> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING
                          .validate(NamePool::valid)
                          .listOf()
                          .fieldOf("names")
                          .forGetter(NamePool::names))
                  .apply(instance, NamePool::new));

  static DataResult<String> valid(final String name) {
    return NameRules.check(name).name().equals(Optional.of(name))
        ? DataResult.success(name)
        : DataResult.error(() -> "Not a usable name: " + name);
  }
}
