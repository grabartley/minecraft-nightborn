package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.sound.NightbornSounds;
import java.util.function.Supplier;
import net.minecraft.sound.SoundEvent;

public enum CoffinStyle {
  WOOD("coffin", () -> NightbornSounds.COFFIN_OPEN, () -> NightbornSounds.COFFIN_CLOSE),
  STONE(
      "stone_coffin",
      () -> NightbornSounds.STONE_COFFIN_OPEN,
      () -> NightbornSounds.STONE_COFFIN_CLOSE);

  private final String model;
  private final Supplier<SoundEvent> open;
  private final Supplier<SoundEvent> close;

  CoffinStyle(
      final String model, final Supplier<SoundEvent> open, final Supplier<SoundEvent> close) {
    this.model = model;
    this.open = open;
    this.close = close;
  }

  public String model() {
    return this.model;
  }

  public SoundEvent lid(final boolean opening) {
    return (opening ? this.open : this.close).get();
  }
}
