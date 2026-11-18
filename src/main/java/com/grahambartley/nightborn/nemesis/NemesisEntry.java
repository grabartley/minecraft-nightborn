package com.grahambartley.nightborn.nemesis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

public record NemesisEntry(
    UUID mob, Identifier kind, String name, long nextVisit, int token, NbtCompound snapshot) {

  public static final Codec<NemesisEntry> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.INT_STREAM_CODEC.fieldOf("mob").forGetter(NemesisEntry::mob),
                      Identifier.CODEC.fieldOf("kind").forGetter(NemesisEntry::kind),
                      Codec.STRING.optionalFieldOf("name", "").forGetter(NemesisEntry::name),
                      Codec.LONG.fieldOf("next_visit").forGetter(NemesisEntry::nextVisit),
                      Codec.INT.optionalFieldOf("token", 0).forGetter(NemesisEntry::token),
                      NbtCompound.CODEC
                          .optionalFieldOf("snapshot", new NbtCompound())
                          .forGetter(NemesisEntry::snapshot))
                  .apply(instance, NemesisEntry::new));

  public NemesisEntry withSnapshot(final String nextName, final NbtCompound next) {
    return new NemesisEntry(mob, kind, nextName, nextVisit, token, next);
  }

  public NemesisEntry visited(final long next, final int nextToken) {
    return new NemesisEntry(mob, kind, name, next, nextToken, snapshot);
  }

  public boolean current(final NemesisMark mark) {
    return mark.token() == token;
  }
}
