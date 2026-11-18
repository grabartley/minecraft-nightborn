package com.grahambartley.nightborn.enchant;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.Prey;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public record HasBloodCondition(LootContext.EntityTarget entity) implements LootCondition {

  public static final MapCodec<HasBloodCondition> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      LootContext.EntityTarget.CODEC
                          .fieldOf("entity")
                          .forGetter(HasBloodCondition::entity))
                  .apply(instance, HasBloodCondition::new));

  public static final LootConditionType TYPE =
      Registry.register(
          Registries.LOOT_CONDITION_TYPE,
          Identifier.of(Nightborn.MOD_ID, "has_blood"),
          new LootConditionType(CODEC));

  public static void register() {}

  @Override
  public LootConditionType getType() {
    return TYPE;
  }

  @Override
  public Set<LootContextParameter<?>> getRequiredParameters() {
    return Set.of(this.entity.getParameter());
  }

  @Override
  public boolean test(final LootContext context) {
    return context.get(this.entity.getParameter()) instanceof LivingEntity living
        && Prey.hasDrinkableBlood(living);
  }
}
