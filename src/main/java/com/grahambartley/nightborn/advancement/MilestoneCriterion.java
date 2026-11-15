package com.grahambartley.nightborn.advancement;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class MilestoneCriterion extends AbstractCriterion<MilestoneCriterion.Conditions> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "milestone");
  public static final MilestoneCriterion INSTANCE = new MilestoneCriterion();

  private MilestoneCriterion() {}

  public static void register() {
    Registry.register(Registries.CRITERION, ID, INSTANCE);
  }

  @Override
  public Codec<Conditions> getConditionsCodec() {
    return Conditions.CODEC;
  }

  public static void reach(@Nullable final Entity player, final Milestone milestone) {
    reach(player, milestone, null, 1);
  }

  public static void reach(
      @Nullable final Entity player, final Milestone milestone, @Nullable final Entity other) {
    reach(player, milestone, other, 1);
  }

  public static void reach(
      @Nullable final Entity player, final Milestone milestone, final int count) {
    reach(player, milestone, null, count);
  }

  private static void reach(
      @Nullable final Entity player,
      final Milestone milestone,
      @Nullable final Entity other,
      final int count) {
    if (!(player instanceof ServerPlayerEntity serverPlayer)) {
      return;
    }
    final LootContext otherContext =
        other == null
            ? null
            : EntityPredicate.createAdvancementEntityLootContext(serverPlayer, other);
    INSTANCE.trigger(
        serverPlayer,
        conditions ->
            conditions.matches(
                milestone,
                count,
                predicate -> otherContext != null && predicate.test(otherContext)));
  }

  public record Conditions(
      Optional<LootContextPredicate> player,
      Milestone milestone,
      Optional<LootContextPredicate> entity,
      NumberRange.IntRange count)
      implements AbstractCriterion.Conditions {

    public static final Codec<Conditions> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC
                            .optionalFieldOf("player")
                            .forGetter(Conditions::player),
                        Milestone.CODEC.fieldOf("milestone").forGetter(Conditions::milestone),
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC
                            .optionalFieldOf("entity")
                            .forGetter(Conditions::entity),
                        NumberRange.IntRange.CODEC
                            .optionalFieldOf("count", NumberRange.IntRange.ANY)
                            .forGetter(Conditions::count))
                    .apply(instance, Conditions::new));

    public boolean matches(
        final Milestone reached,
        final int reachedCount,
        final Predicate<LootContextPredicate> other) {
      return this.milestone == reached
          && this.count.test(reachedCount)
          && this.entity.map(other::test).orElse(true);
    }

    @Override
    public void validate(final LootContextPredicateValidator validator) {
      AbstractCriterion.Conditions.super.validate(validator);
      validator.validateEntityPredicate(this.entity, ".entity");
    }
  }
}
