package com.grahambartley.nightborn.guild;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;

public interface GuildFighter {

  boolean armed();

  void setArmed(boolean armed);

  void show(HunterAction action);

  void speak(HunterVoice voice);

  boolean foeOf(LivingEntity target);

  boolean onWatch();

  boolean canFight();

  Optional<GuildMasterEntity> master();
}
