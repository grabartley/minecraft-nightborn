package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

public record LivesInLootCondition(RegistryEntryList<Structure> structures)
    implements LootCondition {

  public static final MapCodec<LivesInLootCondition> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      RegistryCodecs.entryList(RegistryKeys.STRUCTURE)
                          .fieldOf("structures")
                          .forGetter(LivesInLootCondition::structures))
                  .apply(instance, LivesInLootCondition::new));

  public static final LootConditionType TYPE =
      Registry.register(
          Registries.LOOT_CONDITION_TYPE,
          Identifier.of(Nightborn.MOD_ID, "lives_in"),
          new LootConditionType(CODEC));

  public static void register() {}

  @Override
  public LootConditionType getType() {
    return TYPE;
  }

  @Override
  public Set<LootContextParameter<?>> getRequiredParameters() {
    return Set.of(LootContextParameters.THIS_ENTITY);
  }

  @Override
  public boolean test(final LootContext context) {
    final Optional<BlockPos> home =
        context.get(LootContextParameters.THIS_ENTITY) instanceof HouseholdMember member
            ? member.home()
            : Optional.empty();
    return home.filter(
            pos ->
                context.getWorld().isPosLoaded(pos.getX(), pos.getZ())
                    && context
                        .getWorld()
                        .getStructureAccessor()
                        .getStructureContaining(pos, structures)
                        .hasChildren())
        .isPresent();
  }
}
