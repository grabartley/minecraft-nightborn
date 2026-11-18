package com.grahambartley.nightborn.inquisitor;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;

public record InquisitorQuarry(UUID player, boolean bell, Optional<UUID> turnedOn) {

  static final String PLAYER_KEY = "Quarry";
  static final String BELL_KEY = "QuarryRang";
  static final String TURNED_KEY = "TurnedOn";

  public static InquisitorQuarry of(final UUID player, final boolean bell, final boolean vampire) {
    return new InquisitorQuarry(
        player,
        bell,
        InquisitorFoes.turnsOn(vampire, bell) ? Optional.of(player) : Optional.empty());
  }

  public boolean turnedOn(final UUID player) {
    return turnedOn.filter(player::equals).isPresent();
  }

  public void write(final NbtCompound nbt) {
    nbt.putUuid(PLAYER_KEY, player);
    nbt.putBoolean(BELL_KEY, bell);
    turnedOn.ifPresent(id -> nbt.putUuid(TURNED_KEY, id));
  }

  public static Optional<InquisitorQuarry> read(final NbtCompound nbt) {
    if (!nbt.containsUuid(PLAYER_KEY)) {
      return Optional.empty();
    }
    return Optional.of(
        new InquisitorQuarry(
            nbt.getUuid(PLAYER_KEY),
            nbt.getBoolean(BELL_KEY),
            nbt.containsUuid(TURNED_KEY)
                ? Optional.of(nbt.getUuid(TURNED_KEY))
                : Optional.empty()));
  }
}
