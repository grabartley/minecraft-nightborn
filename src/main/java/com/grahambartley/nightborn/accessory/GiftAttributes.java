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
  public static final double MOST_EXTRA_DAMAGE = 4.0;

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
  public static final RegistryEntry<EntityAttribute> LEVITATION_DRAIN =
      register("levitation_drain", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> HOUSE_DAMAGE =
      register("house_damage", 1.0, 1.0, MOST_BOOST);
  public static final RegistryEntry<EntityAttribute> HOUSE_DAMAGE_RANGE =
      register("house_damage_range", 0.0, 0.0, MOST_RANGE);
  public static final RegistryEntry<EntityAttribute> DAYLIGHT_HEAL =
      register("daylight_heal", 0.0, 0.0, MOST_HEAL);
  public static final RegistryEntry<EntityAttribute> DAYLIGHT_HEAL_INTERVAL =
      register("daylight_heal_interval", 0.0, 0.0, MOST_INTERVAL_TICKS);
  public static final RegistryEntry<EntityAttribute> HOUSE_NOISE =
      register("house_noise", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> FROST_WARD =
      register("frost_ward", 0.0, 0.0, 1.0);
  public static final RegistryEntry<EntityAttribute> BARE_HAND_DAMAGE =
      register("bare_hand_damage", 0.0, 0.0, MOST_EXTRA_DAMAGE);
  public static final RegistryEntry<EntityAttribute> FEEDING_NOTICE =
      register("feeding_notice", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> STAKE_DAMAGE =
      register("stake_damage", 1.0, 1.0, MOST_BOOST);
  public static final RegistryEntry<EntityAttribute> BITE_ESCAPE =
      register("bite_escape", 1.0, 1.0, MOST_BOOST);
  public static final RegistryEntry<EntityAttribute> SOIL_DIG_SPEED =
      register("soil_dig_speed", 1.0, 1.0, MOST_BOOST);
  public static final RegistryEntry<EntityAttribute> BOTTLE_BLOOD =
      register("bottle_blood", 1.0, 1.0, MOST_BOOST);
  public static final RegistryEntry<EntityAttribute> TRAP_COOLDOWN =
      register("trap_cooldown", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> SMALL_FLINCH =
      register("small_flinch", 0.0, 0.0, 1.0);
  public static final RegistryEntry<EntityAttribute> DUSK_SIGHT =
      register("dusk_sight", 0.0, 0.0, 1.0);
  public static final RegistryEntry<EntityAttribute> FACE_LEARNING =
      register("face_learning", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> VAMPIRE_INSIGHT =
      register("vampire_insight", 0.0, 0.0, 1.0);
  public static final RegistryEntry<EntityAttribute> BITE_WEAKNESS_TIME =
      register("bite_weakness_time", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> FOG_CLEAR_SIGHT =
      register("fog_clear_sight", 0.0, 0.0, 1.0);
  public static final RegistryEntry<EntityAttribute> CLOUD_TIME =
      register("cloud_time", 1.0, LEAST_SHARE, 1.0);
  public static final RegistryEntry<EntityAttribute> NAME_RANGE =
      register("name_range", 0.0, 0.0, MOST_RANGE);

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
          COBWEB_SLOW,
          LEVITATION_DRAIN,
          HOUSE_DAMAGE,
          HOUSE_DAMAGE_RANGE,
          DAYLIGHT_HEAL,
          DAYLIGHT_HEAL_INTERVAL,
          HOUSE_NOISE,
          FROST_WARD,
          BARE_HAND_DAMAGE,
          FEEDING_NOTICE,
          STAKE_DAMAGE,
          BITE_ESCAPE,
          SOIL_DIG_SPEED,
          BOTTLE_BLOOD,
          TRAP_COOLDOWN,
          SMALL_FLINCH,
          DUSK_SIGHT,
          FACE_LEARNING,
          VAMPIRE_INSIGHT,
          BITE_WEAKNESS_TIME,
          FOG_CLEAR_SIGHT,
          CLOUD_TIME,
          NAME_RANGE);

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
