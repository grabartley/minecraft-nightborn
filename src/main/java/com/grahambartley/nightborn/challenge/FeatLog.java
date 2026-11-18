package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record FeatLog(
    Map<Identifier, Tally> tallies,
    Map<Identifier, Boolean> earned,
    List<Identifier> spoiled,
    Optional<Identifier> title) {

  public static final FeatLog EMPTY = new FeatLog(Map.of(), Map.of(), List.of(), Optional.empty());

  public static final Codec<FeatLog> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.unboundedMap(Identifier.CODEC, Tally.CODEC)
                          .optionalFieldOf("tallies", Map.of())
                          .forGetter(FeatLog::tallies),
                      Codec.unboundedMap(Identifier.CODEC, Codec.BOOL)
                          .optionalFieldOf("earned_as_vampire", Map.of())
                          .forGetter(FeatLog::earned),
                      Identifier.CODEC
                          .listOf()
                          .optionalFieldOf("spoiled", List.of())
                          .forGetter(FeatLog::spoiled),
                      Identifier.CODEC.optionalFieldOf("title").forGetter(FeatLog::title))
                  .apply(instance, FeatLog::new));

  public static final PacketCodec<ByteBuf, FeatLog> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, Tally.PACKET_CODEC),
          FeatLog::tallies,
          PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, PacketCodecs.BOOL),
          FeatLog::earned,
          Identifier.PACKET_CODEC.collect(PacketCodecs.toList()),
          FeatLog::spoiled,
          PacketCodecs.optional(Identifier.PACKET_CODEC),
          FeatLog::title,
          FeatLog::new);

  public FeatLog {
    tallies = Map.copyOf(tallies);
    earned = Map.copyOf(earned);
    spoiled = List.copyOf(new LinkedHashSet<>(spoiled));
    title = title.filter(earned::containsKey);
  }

  public boolean open(final Identifier feat) {
    return !earned.containsKey(feat) && !spoiled.contains(feat);
  }

  public Tally tally(final Identifier feat) {
    return tallies.getOrDefault(feat, Tally.EMPTY);
  }

  public FeatLog counted(final Identifier feat, final Tally tally) {
    final Map<Identifier, Tally> next = new LinkedHashMap<>(tallies);
    next.put(feat, tally);
    return new FeatLog(next, earned, spoiled, title);
  }

  public FeatLog earning(final Identifier feat, final boolean asVampire) {
    final Map<Identifier, Tally> nextTallies = new LinkedHashMap<>(tallies);
    nextTallies.remove(feat);
    final Map<Identifier, Boolean> nextEarned = new LinkedHashMap<>(earned);
    nextEarned.put(feat, asVampire);
    return new FeatLog(nextTallies, nextEarned, spoiled, title);
  }

  public FeatLog spoiling(final Identifier feat) {
    final Map<Identifier, Tally> nextTallies = new LinkedHashMap<>(tallies);
    nextTallies.remove(feat);
    final Set<Identifier> nextSpoiled = new LinkedHashSet<>(spoiled);
    nextSpoiled.add(feat);
    return new FeatLog(nextTallies, earned, List.copyOf(nextSpoiled), title);
  }

  public FeatLog wearing(final Optional<Identifier> chosen) {
    return new FeatLog(tallies, earned, spoiled, chosen.filter(earned::containsKey));
  }

  public Optional<Boolean> worn() {
    return title.map(earned::get);
  }
}
