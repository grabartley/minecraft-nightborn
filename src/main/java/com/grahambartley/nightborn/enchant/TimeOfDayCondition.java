package com.grahambartley.nightborn.enchant;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.core.NightSight;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public record TimeOfDayCondition(TimeOfDayCondition.Time time) implements LootCondition {

  public enum Time implements StringIdentifiable {
    DAY,
    NIGHT;

    public static final Codec<Time> CODEC = StringIdentifiable.createCodec(Time::values);

    public boolean holds(final boolean day, final boolean night) {
      return this == DAY ? day : night;
    }

    @Override
    public String asString() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  public static final MapCodec<TimeOfDayCondition> CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(Time.CODEC.fieldOf("time").forGetter(TimeOfDayCondition::time))
                  .apply(instance, TimeOfDayCondition::new));

  public static final LootConditionType TYPE =
      Registry.register(
          Registries.LOOT_CONDITION_TYPE,
          Identifier.of(Nightborn.MOD_ID, "time_of_day"),
          new LootConditionType(CODEC));

  public static void register() {}

  @Override
  public LootConditionType getType() {
    return TYPE;
  }

  @Override
  public boolean test(final LootContext context) {
    return this.time.holds(context.getWorld().isDay(), NightSight.isNight(context.getWorld()));
  }
}
