package com.grahambartley.nightborn.lord;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.downed.DownedState;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BossPresenceTest {

  @ParameterizedTest(name = "{0} at {1} of {2}")
  @CsvSource({
    "STANDING, 150, 150, RED, 1.0",
    "STANDING, 30, 150, RED, 0.2",
    "DOWNED, 1, 150, WHITE, 0.006666667",
    "ESCAPING, 45, 150, PURPLE, 0.3"
  })
  @DisplayName("the boss bar follows the boss's health and whether it is downed or escaping")
  void follows(
      final String phase,
      final float health,
      final float max,
      final BossBar.Color color,
      final float percent) {
    final BossPresence presence = new BossPresence(Text.literal("Vampire Lord"));
    presence.update(
        Text.literal("Vampire Lord"),
        health,
        max,
        phase.equals("STANDING") ? null : DownedState.Phase.valueOf(phase));
    assertEquals(color, presence.bar().getColor());
    assertEquals(percent, presence.bar().getPercent(), 1.0E-6F);
    assertEquals(BossBar.Style.NOTCHED_10, presence.bar().getStyle());
  }
}
