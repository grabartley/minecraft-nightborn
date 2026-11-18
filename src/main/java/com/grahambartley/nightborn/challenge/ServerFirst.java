package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import java.util.Locale;
import java.util.Map;
import net.minecraft.util.StringIdentifiable;

public enum ServerFirst implements StringIdentifiable {
  ANCIENT(new DeedMatch(DeedKinds.STAGE_REACHED, Map.of(DeedKinds.TOP, "true"), Map.of())),
  MASTER(new DeedMatch(DeedKinds.RANK_REACHED, Map.of(DeedKinds.TOP, "true"), Map.of())),
  ELDEST(DeedMatch.of(DeedKinds.ELDEST_ENDED)),
  INQUISITOR(DeedMatch.of(DeedKinds.INQUISITOR_DRAINED));

  public static final Codec<ServerFirst> CODEC =
      StringIdentifiable.createCodec(ServerFirst::values);

  private final DeedMatch deed;

  ServerFirst(final DeedMatch deed) {
    this.deed = deed;
  }

  public boolean marks(final Deed done) {
    return deed.matches(done);
  }

  public String key() {
    return "nightborn.first." + asString();
  }

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }
}
