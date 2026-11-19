package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GiftAttributesTest {

  @ParameterizedTest(name = "{0}: {1} in {2}..{3}")
  @CsvSource({
    "finisher_blood, 0.0, 0.0, 8.0",
    "feeding_blood, 1.0, 1.0, 2.0",
    "thrall_health, 1.0, 1.0, 2.0",
    "familiar_time, 1.0, 1.0, 2.0",
    "bat_form_cost, 0.0, -8.0, 0.0",
    "feeding_suspicion, 1.0, 0.2, 1.0",
    "hunter_shot_damage, 1.0, 0.2, 1.0",
    "holy_water_time, 1.0, 0.2, 1.0",
    "fire_damage, 1.0, 0.2, 1.0",
    "blessing_time, 1.0, 1.0, 2.0",
    "tracking_cooldown, 1.0, 0.2, 1.0",
    "vigil_range, 0.0, 0.0, 32.0",
    "night_damage, 1.0, 0.2, 1.0",
    "mind_time, 1.0, 0.2, 1.0",
    "strike_heal, 0.0, 0.0, 4.0",
    "strike_heal_interval, 0.0, 0.0, 200.0",
    "worn_ward, 0.0, 0.0, 1.0",
    "mob_bite_break, 0.0, 0.0, 0.8",
    "prayer_healing, 1.0, 1.0, 2.0",
    "holy_water_radius, 0.0, 0.0, 4.0",
    "guild_price, 1.0, 0.2, 1.0",
    "villager_price, 1.0, 0.2, 1.0",
    "chest_luck, 0.0, 0.0, 4.0",
    "cobweb_slow, 1.0, 0.0, 1.0",
    "levitation_drain, 1.0, 0.2, 1.0",
    "house_damage, 1.0, 1.0, 2.0",
    "house_damage_range, 0.0, 0.0, 32.0",
    "daylight_heal, 0.0, 0.0, 4.0",
    "daylight_heal_interval, 0.0, 0.0, 200.0",
    "house_noise, 1.0, 0.2, 1.0",
    "frost_ward, 0.0, 0.0, 1.0",
    "bare_hand_damage, 0.0, 0.0, 4.0",
    "feeding_notice, 1.0, 0.2, 1.0",
    "stake_damage, 1.0, 1.0, 2.0",
    "bite_escape, 1.0, 1.0, 2.0",
    "soil_dig_speed, 1.0, 1.0, 2.0",
    "bottle_blood, 1.0, 1.0, 2.0",
    "trap_cooldown, 1.0, 0.2, 1.0",
    "small_flinch, 0.0, 0.0, 1.0",
    "dusk_sight, 0.0, 0.0, 1.0",
    "face_learning, 1.0, 0.2, 1.0",
    "vampire_insight, 0.0, 0.0, 1.0",
    "bite_weakness_time, 1.0, 0.2, 1.0",
    "fog_clear_sight, 0.0, 0.0, 1.0",
    "cloud_time, 1.0, 0.2, 1.0",
    "name_range, 0.0, 0.0, 32.0"
  })
  @DisplayName("each gift starts neutral, and no reduction passes 80%")
  void bounds(final String path, final double base, final double min, final double max) {
    final ClampedEntityAttribute clamped =
        (ClampedEntityAttribute)
            GiftAttributes.ALL.stream()
                .filter(a -> a.getKey().orElseThrow().getValue().getPath().equals(path))
                .findFirst()
                .orElseThrow()
                .value();
    assertEquals(base, clamped.getDefaultValue());
    assertEquals(min, clamped.getMinValue());
    assertEquals(max, clamped.getMaxValue());
    assertEquals("attribute.name.nightborn." + path, clamped.getTranslationKey());
  }

  @Test
  @DisplayName("every gift attribute is listed once")
  void allListed() {
    assertEquals(46, GiftAttributes.ALL.stream().distinct().count());
  }
}
