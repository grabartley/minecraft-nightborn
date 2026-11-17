package com.grahambartley.nightborn.name;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record NamePool(List<String> names) {

  public static final NamePool EMPTY = new NamePool(List.of());

  public NamePool {
    names = List.copyOf(names);
  }

  public static final Codec<NamePool> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(Codec.STRING.listOf().fieldOf("names").forGetter(NamePool::names))
                  .apply(instance, NamePool::new));
}
