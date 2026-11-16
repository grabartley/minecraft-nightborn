package com.grahambartley.nightborn.guild;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum ContractGoal implements StringIdentifiable {
  STAKE_VAMPIRES("stake_vampires"),
  CLEAR_HOUSEHOLD("clear_household");

  public static final Codec<ContractGoal> CODEC =
      StringIdentifiable.createCodec(ContractGoal::values);

  private final String id;

  ContractGoal(final String id) {
    this.id = id;
  }

  public boolean countedBy(final HunterContract.Stake facts) {
    return this == STAKE_VAMPIRES || facts.householdCleared();
  }

  @Override
  public String asString() {
    return id;
  }
}
