package com.grahambartley.nightborn.coating;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record Coat(
    Coating coating, int hitsUsed, long startedAt, int bonusTicks, int blessedTicks) {

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
                          .forGetter(Coat::startedAt),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .optionalFieldOf("bonus_ticks", 0)
                          .forGetter(Coat::bonusTicks),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .optionalFieldOf("blessed_ticks", 0)
                          .forGetter(Coat::blessedTicks))
                  .apply(instance, Coat::new));

  public static final PacketCodec<ByteBuf, Coat> PACKET_CODEC =
      PacketCodec.tuple(
          Coating.PACKET_CODEC,
          Coat::coating,
          PacketCodecs.VAR_INT,
          Coat::hitsUsed,
          PacketCodecs.VAR_LONG,
          Coat::startedAt,
          PacketCodecs.VAR_INT,
          Coat::bonusTicks,
          PacketCodecs.VAR_INT,
          Coat::blessedTicks,
          Coat::new);

  public Coat(
      final Coating coating, final int hitsUsed, final long startedAt, final int bonusTicks) {
    this(coating, hitsUsed, startedAt, bonusTicks, 0);
  }

  public static Coat fresh(final Coating coating) {
    return new Coat(coating, 0, NOT_STARTED, 0);
  }

  public static Coat blessing(final long now, final int ticks) {
    return new Coat(Coating.HOLY_WATER, 0, now, 0, Math.max(1, ticks));
  }

  public boolean blessed() {
    return blessedTicks > 0;
  }

  public boolean started() {
    return startedAt != NOT_STARTED;
  }

  public Coat startingAt(final long now) {
    return started() ? this : new Coat(coating, hitsUsed, now, bonusTicks, blessedTicks);
  }

  public Coat struck() {
    return new Coat(coating, hitsUsed + 1, startedAt, bonusTicks, blessedTicks);
  }

  public int hitsLeft(final CoatingTerms terms) {
    return blessed() ? Integer.MAX_VALUE : Math.max(0, terms.hits() - hitsUsed);
  }

  private int lasts(final CoatingTerms terms) {
    return blessed() ? blessedTicks : terms.ticks();
  }

  public long ticksLeft(final CoatingTerms terms, final long now) {
    final long lasts = (long) lasts(terms) + bonusTicks;
    return started() ? Math.max(0L, startedAt + lasts - now) : lasts;
  }

  public Coat blessedFor(final CoatingTerms terms, final double lastsFactor) {
    if (coating != Coating.HOLY_WATER || bonusTicks > 0) {
      return this;
    }
    final int bonus = (int) Math.max(0L, Math.round(lasts(terms) * (lastsFactor - 1.0)));
    return bonus == 0 ? this : new Coat(coating, hitsUsed, startedAt, bonus, blessedTicks);
  }

  public boolean worn(final CoatingTerms terms, final long now) {
    return hitsLeft(terms) == 0 || ticksLeft(terms, now) == 0L;
  }

  public boolean live(final CoatingTerms terms, final long now, final boolean ammo) {
    return ammo || !worn(terms, now);
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
