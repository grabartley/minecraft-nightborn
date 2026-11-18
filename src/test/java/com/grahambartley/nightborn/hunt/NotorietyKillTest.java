package com.grahambartley.nightborn.hunt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class NotorietyKillTest {

  static TagKey<EntityType<?>> tag(final String path) {
    return TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("nightborn", path));
  }

  static final TagKey<EntityType<?>> HUNTERS = tag("notoriety/hunter_mobs");
  static final TagKey<EntityType<?>> MASTERS = tag("notoriety/guild_masters");
  static final List<NotorietyKill> KILLS =
      List.of(new NotorietyKill(HUNTERS, 1, 5), new NotorietyKill(MASTERS, 5, 5));

  @ParameterizedTest(name = "{0}, last {1}, multiplier {2} -> {3}")
  @CsvSource({
    "notoriety/hunter_mobs, false, 1, 1",
    "notoriety/hunter_mobs, true, 1, 6",
    "notoriety/guild_masters, false, 1, 5",
    "notoriety/guild_masters, true, 1, 10",
    "notoriety/hunter_mobs, false, 1.25, 1.25",
    "notoriety/hunter_mobs, true, 1.5, 9",
    "notoriety/guild_masters, true, 2, 20",
    "notoriety/villagers, true, 2, 0"
  })
  @DisplayName("a kill adds what data says, more for the last of a band and for an older vampire")
  void worth(final String victim, final boolean last, final double multiplier, final double worth) {
    assertEquals(worth, NotorietyKill.worth(KILLS, tag(victim)::equals, last, multiplier), 0.0001);
  }

  @Test
  @DisplayName("a victim listed twice counts once, at its highest")
  void countsOnce() {
    assertEquals(10.0, NotorietyKill.worth(KILLS, key -> true, true, 1.0), 0.0001);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "'{\"victims\":\"nightborn:notoriety/hunter_mobs\",\"amount\":1,\"last_of_band\":5}', 1, 5",
    "'{\"victims\":\"nightborn:notoriety/guild_masters\",\"amount\":5}', 5, 0"
  })
  @DisplayName("a kill entry reads from data, the last-of-band bonus being optional")
  void parses(final String json, final double amount, final double last) {
    final NotorietyKill kill =
        NotorietyKill.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(amount, kill.amount());
    assertEquals(last, kill.lastOfBand());
  }

  @Test
  @DisplayName("a kill cannot take notoriety away")
  void refusesNegative() {
    assertTrue(
        NotorietyKill.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"victims\":\"nightborn:notoriety/hunter_mobs\",\"amount\":-1}"))
            .isError());
  }
}
