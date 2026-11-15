package com.grahambartley.nightborn.coating;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record Coat(Coating coating, int hitsUsed, long startedAt) {

  public static final long NOT_STARTED = -1L;

  public static final Codec<Coat> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Coating.CODEC.fieldOf("coating").forGetter(Coat::coating),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .optionalFieldOf("hits_used", 0)
                          .forGetter(Coat::hitsUsed),
                      Codec.LONG
                          .optionalFieldOf("started_at", NOT_STARTED)
                          .forGetter(Coat::startedAt))
                  .apply(instance, Coat::new));

  public static final PacketCodec<ByteBuf, Coat> PACKET_CODEC =
      PacketCodec.tuple(
          Coating.PACKET_CODEC,
          Coat::coating,
          PacketCodecs.VAR_INT,
          Coat::hitsUsed,
          PacketCodecs.VAR_LONG,
          Coat::startedAt,
          Coat::new);

  public static Coat fresh(final Coating coating) {
    return new Coat(coating, 0, NOT_STARTED);
  }

  public boolean started() {
    return startedAt != NOT_STARTED;
  }

  public Coat startingAt(final long now) {
    return started() ? this : new Coat(coating, hitsUsed, now);
  }

  public Coat struck() {
    return new Coat(coating, hitsUsed + 1, startedAt);
  }

  public int hitsLeft(final CoatingTerms terms) {
    return Math.max(0, terms.hits() - hitsUsed);
  }

  public long ticksLeft(final CoatingTerms terms, final long now) {
    return started() ? Math.max(0L, startedAt + terms.ticks() - now) : terms.ticks();
  }

  public boolean worn(final CoatingTerms terms, final long now) {
    return hitsLeft(terms) == 0 || ticksLeft(terms, now) == 0L;
  }

  public boolean live(final CoatingTerms terms, final long now, final boolean ammo) {
    return ammo || !worn(terms, now);
  }

  public float bonus(final CoatingTerms terms, final long now, final boolean ammo) {
    return live(terms, now, ammo) ? terms.damage() : 0.0F;
  }

  public Optional<Coat> afterStrike(final CoatingTerms terms, final long now) {
    return stillOn(startingAt(now).struck(), terms, now);
  }

  public Optional<Coat> afterCheck(final CoatingTerms terms, final long now) {
    return stillOn(startingAt(now), terms, now);
  }

  private static Optional<Coat> stillOn(final Coat coat, final CoatingTerms terms, final long now) {
    return coat.worn(terms, now) ? Optional.empty() : Optional.of(coat);
  }
}
