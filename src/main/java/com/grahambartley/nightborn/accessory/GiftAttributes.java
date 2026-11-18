package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class GiftAttributes {

  public static final double LEAST_SHARE = 0.2;
  public static final double MOST_RESISTANCE = 0.8;
  public static final double MOST_BOOST = 2.0;
  public static final double MOST_EXTRA_BLOOD = 8.0;
  public static final double MOST_RANGE = 32.0;
  public static final double MOST_LUCK = 4.0;
  public static final double MOST_HEAL = 4.0;
  public static final double MOST_INTERVAL_TICKS = 200.0;
  public static final double MOST_RADIUS = 4.0;
  public static final double MOST_COST_CUT = 8.0;

  public static final RegistryEntry<EntityAttribute> FINISHER_BLOOD =
      register("finisher_blood", 0.0, 0.0, MOST_EXTRA_BLOOD);
  public static final RegistryEntry<EntityAttribute> FEEDING_BLOOD =
      register("feeding_blood", 1.0, 1.0, MOST_BOOST);
  public static final RegistryEntry<EntityAttribute> THRALL_HEALTH =
      register("thrall_health", 1.0, 1.0, MOST_BOOST);
  public static final RegistryEntry<EntityAttribute> FAMILIAR_TIME =
      register("familiar_time", 1.0, 1.0, MOST_BOOST);
  public static final RegistryEntry<EntityAttribute> BAT_FORM_COST =
      register("bat_form_cost", 0.0, -MOST_COST_CUT, 0.0);
  public static final RegistryEntry<EntityAttribute> FEEDING_SUSPICION =
      register("feeding_suspicion", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> HUNTER_SHOT_DAMAGE =
      register("hunter_shot_damage", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> HOLY_WATER_TIME =
      register("holy_water_time", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> FIRE_DAMAGE =
      register("fire_damage", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> BLESSING_TIME =
      register("blessing_time", 1.0, 1.0, MOST_BOOST);
  public static final RegistryEntry<EntityAttribute> TRACKING_COOLDOWN =
      register("tracking_cooldown", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> VIGIL_RANGE =
      register("vigil_range", 0.0, 0.0, MOST_RANGE);
  public static final RegistryEntry<EntityAttribute> NIGHT_DAMAGE =
      register("night_damage", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> MIND_TIME =
      register("mind_time", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> STRIKE_HEAL =
      register("strike_heal", 0.0, 0.0, MOST_HEAL);
  public static final RegistryEntry<EntityAttribute> STRIKE_HEAL_INTERVAL =
      register("strike_heal_interval", 0.0, 0.0, MOST_INTERVAL_TICKS);
  public static final RegistryEntry<EntityAttribute> WORN_WARD =
      register("worn_ward", 0.0, 0.0, 1.0);
  public static final RegistryEntry<EntityAttribute> MOB_BITE_BREAK =
      register("mob_bite_break", 0.0, 0.0, MOST_RESISTANCE);
  public static final RegistryEntry<EntityAttribute> PRAYER_HEALING =
      register("prayer_healing", 1.0, 1.0, MOST_BOOST);
  public static final RegistryEntry<EntityAttribute> HOLY_WATER_RADIUS =
      register("holy_water_radius", 0.0, 0.0, MOST_RADIUS);
  public static final RegistryEntry<EntityAttribute> GUILD_PRICE =
      register("guild_price", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> VILLAGER_PRICE =
      register("villager_price", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> CHEST_LUCK =
      register("chest_luck", 0.0, 0.0, MOST_LUCK);
  public static final RegistryEntry<EntityAttribute> COBWEB_SLOW =
      register("cobweb_slow", 1.0, 0.0, 1.0);

  public static final List<RegistryEntry<EntityAttribute>> ALL =
      List.of(
          FINISHER_BLOOD,
          FEEDING_BLOOD,
          THRALL_HEALTH,
          FAMILIAR_TIME,
          BAT_FORM_COST,
          FEEDING_SUSPICION,
          HUNTER_SHOT_DAMAGE,
          HOLY_WATER_TIME,
          FIRE_DAMAGE,
          BLESSING_TIME,
          TRACKING_COOLDOWN,
          VIGIL_RANGE,
          NIGHT_DAMAGE,
          MIND_TIME,
          STRIKE_HEAL,
          STRIKE_HEAL_INTERVAL,
          WORN_WARD,
          MOB_BITE_BREAK,
          PRAYER_HEALING,
          HOLY_WATER_RADIUS,
          GUILD_PRICE,
          VILLAGER_PRICE,
          CHEST_LUCK,
          COBWEB_SLOW);

  private GiftAttributes() {}

  public static void register() {}

  public static DefaultAttributeContainer.Builder addTo(
      final DefaultAttributeContainer.Builder builder) {
    ALL.forEach(builder::add);
    return builder;
  }

  public static double value(
      final LivingEntity entity, final RegistryEntry<EntityAttribute> attribute) {
    return entity.getAttributes().hasAttribute(attribute)
        ? entity.getAttributeValue(attribute)
        : attribute.value().getDefaultValue();
  }

  public static String translationKey(final String path) {
    return "attribute.name." + Nightborn.MOD_ID + "." + path;
  }

  private static RegistryEntry<EntityAttribute> register(
      final String path, final double base, final double min, final double max) {
    return Registry.registerReference(
        Registries.ATTRIBUTE,
        Identifier.of(Nightborn.MOD_ID, path),
        new ClampedEntityAttribute(translationKey(path), base, min, max).setTracked(true));
  }
}
