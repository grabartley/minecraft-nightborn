package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.rank.GuildRanks;
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
    return KitDamage.blow(carriedKit(), weapon, kitRank());
  }

  default float boltDamage() {
    return KitDamage.bolt(carriedKit(), kitRank());
  }

  default int kitRank() {
    return this instanceof LivingEntity hunter ? GuildRanks.of(hunter) : 0;
  }
}
