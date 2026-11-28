package com.grahambartley.nightborn.guild;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface GuildFighter {

  boolean armed();

  void setArmed(boolean armed);

  void show(HunterAction action);

  void speak(HunterVoice voice);

  boolean foeOf(LivingEntity target);

  Optional<GuildMasterEntity> master();

  float blowDamage(ItemStack weapon);

  float boltDamage();
}
