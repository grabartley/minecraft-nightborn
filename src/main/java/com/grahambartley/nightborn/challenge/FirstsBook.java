package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Uuids;

public record FirstsBook(Map<ServerFirst, Holder> holders) {

  public record Holder(UUID player, String name) {

    public static final Codec<Holder> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Uuids.STRING_CODEC.fieldOf("player").forGetter(Holder::player),
                        Codec.STRING.fieldOf("name").forGetter(Holder::name))
                    .apply(instance, Holder::new));
  }

  public static final FirstsBook EMPTY = new FirstsBook(Map.of());

  public static final Codec<FirstsBook> CODEC =
      Codec.unboundedMap(ServerFirst.CODEC, Holder.CODEC)
          .xmap(FirstsBook::new, FirstsBook::holders);

  public FirstsBook {
    holders = Map.copyOf(holders);
  }

  public Optional<Holder> holder(final ServerFirst first) {
    return Optional.ofNullable(holders.get(first));
  }

  public Optional<FirstsBook> claim(final ServerFirst first, final Holder holder) {
    if (holders.containsKey(first)) {
      return Optional.empty();
    }
    final Map<ServerFirst, Holder> next = new EnumMap<>(ServerFirst.class);
    next.putAll(holders);
    next.put(first, holder);
    return Optional.of(new FirstsBook(next));
  }
}
