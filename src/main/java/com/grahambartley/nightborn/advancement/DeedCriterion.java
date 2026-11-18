package com.grahambartley.nightborn.advancement;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedMatch;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class DeedCriterion extends AbstractCriterion<DeedCriterion.Conditions> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "deed");
  public static final DeedCriterion INSTANCE = new DeedCriterion();

  private DeedCriterion() {}

  public static void register() {
    Registry.register(Registries.CRITERION, ID, INSTANCE);
  }

  @Override
  public Codec<Conditions> getConditionsCodec() {
    return Conditions.CODEC;
  }

  public static void done(final ServerPlayerEntity player, final Deed deed) {
    INSTANCE.trigger(player, conditions -> conditions.matches(deed));
  }

  public record Conditions(Optional<LootContextPredicate> player, DeedMatch match)
      implements AbstractCriterion.Conditions {

    public static final Codec<Conditions> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC
                            .optionalFieldOf("player")
                            .forGetter(Conditions::player),
                        DeedMatch.MAP_CODEC.forGetter(Conditions::match))
                    .apply(instance, Conditions::new));

    public boolean matches(final Deed done) {
      return match.matches(done);
    }
  }
}
