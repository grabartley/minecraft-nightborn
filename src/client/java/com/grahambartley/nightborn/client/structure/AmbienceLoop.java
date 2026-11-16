package com.grahambartley.nightborn.client.structure;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public final class AmbienceLoop extends MovingSoundInstance {

  private float target;

  public AmbienceLoop(final SoundEvent sound, final float target) {
    super(sound, SoundCategory.AMBIENT, SoundInstance.createRandom());
    this.repeat = true;
    this.repeatDelay = 0;
    this.relative = true;
    this.attenuationType = SoundInstance.AttenuationType.NONE;
    this.volume = 0.0F;
    this.target = target;
  }

  public void aimAt(final float next) {
    target = next;
  }

  public void fadeOut() {
    target = 0.0F;
  }

  @Override
  public boolean shouldAlwaysPlay() {
    return true;
  }

  @Override
  public void tick() {
    volume = AmbienceMix.approach(volume, target);
    if (volume <= 0.0F && target <= 0.0F) {
      setDone();
    }
  }
}
