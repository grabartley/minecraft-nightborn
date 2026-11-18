package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.VampireMobAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class OccultAttributesTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"-1.0, 0.0", "0.0, 0.0", "2.0, 2.0", "20.0, 8.0"})
  @DisplayName("the familiar limit is registered, starts at zero and clamps to eight")
  void familiarLimit(final double raw, final double expected) {
    assertEquals("nightborn:familiar_limit", OccultAttributes.FAMILIAR_LIMIT.getIdAsString());
    assertEquals(
        "attribute.name.nightborn.familiar_limit",
        OccultAttributes.FAMILIAR_LIMIT.value().getTranslationKey());
    assertEquals(0.0, OccultAttributes.FAMILIAR_LIMIT.value().getDefaultValue());
    assertEquals(expected, OccultAttributes.FAMILIAR_LIMIT.value().clamp(raw));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"player", "vampire"})
  @DisplayName("players and vampire mobs carry the familiar limit their skills raise")
  void carried(final String who) {
    final DefaultAttributeContainer attributes =
        who.equals("player")
            ? PlayerEntity.createPlayerAttributes().build()
            : VampireMobAttributes.create().build();
    assertTrue(attributes.has(OccultAttributes.FAMILIAR_LIMIT));
  }
}
