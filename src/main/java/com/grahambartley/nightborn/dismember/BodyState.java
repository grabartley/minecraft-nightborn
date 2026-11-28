package com.grahambartley.nightborn.dismember;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record BodyState(Map<BodyPart, LostPart> parts, long lastCutAt) {

  public static final long NEVER = Long.MIN_VALUE / 2;

  public static final BodyState WHOLE = new BodyState(Map.of(), NEVER);

  public static final Codec<BodyState> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.unboundedMap(BodyPart.CODEC, LostPart.CODEC)
                          .optionalFieldOf("parts", Map.of())
                          .forGetter(BodyState::parts),
                      Codec.LONG
                          .optionalFieldOf("last_cut_at", NEVER)
                          .forGetter(BodyState::lastCutAt))
                  .apply(instance, BodyState::new));

  public static final PacketCodec<ByteBuf, BodyState> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.map(
              size -> new EnumMap<>(BodyPart.class), BodyPart.PACKET_CODEC, LostPart.PACKET_CODEC),
          BodyState::parts,
          PacketCodecs.VAR_LONG,
          BodyState::lastCutAt,
          BodyState::new);

  public BodyState {
    parts =
        parts.entrySet().stream()
            .filter(entry -> entry.getValue().condition() != LimbCondition.WHOLE)
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public LimbCondition condition(final BodyPart part) {
    final LostPart lost = parts.get(part);
    return lost == null ? LimbCondition.WHOLE : lost.condition();
  }

  public boolean missing(final BodyPart part) {
    return condition(part) == LimbCondition.MISSING;
  }

  public boolean cutRecently(final long now, final long cooldownTicks) {
    return now - lastCutAt < cooldownTicks;
  }

  public BodyState cut(final BodyPart part, final long now, final int bleedTicks) {
    final Map<BodyPart, LostPart> next = new EnumMap<>(BodyPart.class);
    next.putAll(parts);
    next.put(part, LostPart.missing(now, bleedTicks));
    return new BodyState(next, now);
  }

  public List<BodyPart> openWounds(final long now) {
    return Arrays.stream(BodyPart.values())
        .filter(part -> parts.containsKey(part) && parts.get(part).bleeding(now))
        .toList();
  }

  public Optional<BodyPart> newestCut() {
    return Arrays.stream(BodyPart.values())
        .filter(part -> missing(part) && parts.get(part).lostAt() == lastCutAt)
        .findFirst();
  }

  public boolean bleeding(final long now) {
    return parts.values().stream().anyMatch(lost -> lost.bleeding(now));
  }

  public BodyState closeWounds(final long now) {
    if (!bleeding(now)) {
      return this;
    }
    final Map<BodyPart, LostPart> next = new EnumMap<>(BodyPart.class);
    parts.forEach((part, lost) -> next.put(part, lost.closed(now)));
    return new BodyState(next, lastCutAt);
  }

  public BodyState afterDeath() {
    return new BodyState(
        parts.entrySet().stream()
            .filter(entry -> entry.getValue().condition() == LimbCondition.REPLACED)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
        NEVER);
  }

  public boolean whole() {
    return parts.isEmpty();
  }
}
