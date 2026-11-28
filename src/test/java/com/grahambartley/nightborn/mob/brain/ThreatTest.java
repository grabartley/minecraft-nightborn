package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.config.CombatSettings;
import com.grahambartley.nightborn.config.NightbornConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ThreatTest {

  private static final CombatSettings SETTINGS = NightbornConfig.DEFAULTS.combat();

  @ParameterizedTest(name = "swing {0}, aim {1}, {2} blocks: {3}")
  @CsvSource({
    "true, false, 2, true",
    "true, false, 4, true",
    "true, false, 4.5, false",
    "false, true, 5, true",
    "false, true, 12, true",
    "false, true, 3, false",
    "true, true, 4.5, false",
    "true, true, 3, true",
    "false, false, 2, false",
    "false, false, 12, false"
  })
  @DisplayName("a foe threatens with a swing up close or an aimed shot from afar")
  void at(final boolean swinging, final boolean aiming, final double distance, final boolean at) {
    assertEquals(at, new Threat(swinging, aiming).at(distance, SETTINGS));
  }

  @ParameterizedTest(name = "swing range {0}, aim range {1}, {2} blocks: {3}")
  @CsvSource({"2, 5, 3, false", "6, 5, 5, true", "4, 10, 8, false", "4, 2, 3, true"})
  @DisplayName("the server settings decide how near a swing and how far a shot threatens")
  void rangesAreSettings(
      final int swingRange, final int aimRange, final double distance, final boolean at) {
    final CombatSettings settings =
        NightbornConfig.DEFAULTS
            .with(CombatSettings.SWING_THREAT_RANGE, swingRange)
            .with(CombatSettings.AIM_THREAT_RANGE, aimRange)
            .combat();
    assertEquals(at, new Threat(true, distance >= aimRange).at(distance, settings));
  }

  @ParameterizedTest(name = "using {0}, holding {1}: draws {2}")
  @CsvSource({
    "true, bow, true",
    "true, crossbow, true",
    "true, trident, true",
    "true, shield, false",
    "true, bread, false",
    "false, bow, false"
  })
  @DisplayName("a foe draws on the mob only while it is using a bow, crossbow or trident")
  void draws(final boolean using, final String item, final boolean draws) {
    final LivingEntity foe = mock(LivingEntity.class);
    when(foe.isUsingItem()).thenReturn(using);
    when(foe.getActiveItem())
        .thenReturn(new ItemStack(Registries.ITEM.get(Identifier.ofVanilla(item))));
    assertEquals(draws, Threat.draws(foe));
    assertEquals(new Threat(false, draws), Threat.of(foe));
  }
}
