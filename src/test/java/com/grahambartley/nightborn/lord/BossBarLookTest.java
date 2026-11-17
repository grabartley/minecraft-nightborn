package com.grahambartley.nightborn.lord;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.downed.DownedState;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BossBarLookTest {

  @ParameterizedTest(name = "{0} -> {1} {2}")
  @CsvSource({
    "STANDING, RED, Lord Aldric",
    "DOWNED, WHITE, nightborn.boss.downed",
    "ESCAPING, PURPLE, nightborn.boss.escaping"
  })
  @DisplayName("the bar turns its colour and title when the boss is downed or escaping")
  void shows(final String phase, final BossBar.Color color, final String title) {
    final BossBarLook look =
        BossBarLook.of(phase.equals("STANDING") ? null : DownedState.Phase.valueOf(phase));
    assertEquals(color, look.color());
    final Text shown = look.title(Text.literal("Lord Aldric"));
    final String key =
        shown.getContent() instanceof TranslatableTextContent translatable
            ? translatable.getKey()
            : shown.getString();
    assertEquals(title, key);
  }

  @ParameterizedTest(name = "{0} in {1}")
  @CsvSource({"nightborn.boss.phase.court, RED", "nightborn.boss.phase.blood, PURPLE"})
  @DisplayName("a phase gives the bar its own colour and puts the boss's name in its title")
  void phase(final String key, final BossBar.Color color) {
    final BossBarLook look = BossBarLook.phase(color, key);
    assertEquals(color, look.color());
    final TranslatableTextContent title =
        (TranslatableTextContent) look.title(Text.literal("The Eldest")).getContent();
    assertEquals(key, title.getKey());
    assertEquals("The Eldest", ((Text) title.getArgs()[0]).getString());
  }

  @ParameterizedTest(name = "{0} of {1} -> {2}")
  @CsvSource({
    "150, 150, 1.0",
    "75, 150, 0.5",
    "0, 150, 0.0",
    "200, 150, 1.0",
    "-5, 150, 0.0",
    "5, 0, 0.0"
  })
  @DisplayName("the bar fills with the share of health left, never past full or below empty")
  void percent(final float health, final float max, final float percent) {
    assertEquals(percent, BossBarLook.percent(health, max), 1.0E-6F);
  }
}
