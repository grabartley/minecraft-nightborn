package com.grahambartley.nightborn.guild;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface GuildFighter {

  boolean armed();

  void setArmed(boolean armed);

  void show(HunterAction action);

  void speak(HunterVoice voice);

  boolean foeOf(LivingEntity target);

  Optional<GuildMasterEntity> master();

  @Nullable
  HunterKit carriedKit();

  default float blowDamage(final ItemStack weapon) {
    return KitDamage.blow(carriedKit(), weapon);
  }

  default float boltDamage() {
    return KitDamage.bolt(carriedKit());
  }
}
