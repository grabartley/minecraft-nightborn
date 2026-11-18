package com.grahambartley.nightborn.wheel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WheelEntryTest {

  private static final Identifier ACTION = Identifier.of("nightborn", "thrall_order");

  @ParameterizedTest(name = "{0} {1} -> {2}")
  @CsvSource({
    "nightborn:thrall_order, 1, true",
    "nightborn:thrall_order, 2, false",
    "nightborn:scout, 1, false",
    "nightborn:thrall_order, -1, false"
  })
  @DisplayName("an entry answers only to its own action and argument")
  void names(final String action, final int argument, final boolean names) {
    final WheelEntry entry =
        new WheelEntry(ACTION, 1, Identifier.ofVanilla("lead"), Text.literal("Stay"), false);
    assertEquals(names, entry.names(Identifier.of(action), argument));
  }

  @ParameterizedTest(name = "current {0}")
  @CsvSource({"true", "false"})
  @DisplayName("an entry made without an argument carries the no-argument marker")
  void of(final boolean current) {
    final WheelEntry entry =
        WheelEntry.of(ACTION, Identifier.ofVanilla("lead"), Text.literal("Follow"), current);
    assertEquals(WheelEntry.NO_ARGUMENT, entry.argument());
    assertEquals(current, entry.current());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "follow", "stay", "guard", "fight", "feed", "bring_blood", "leave", "gather",
    "fetch_blood", "scout", "stand_watch", "trade", "contracts", "trials", "rank", "news",
    "invite", "hand_over", "challenge", "carry", "ask_after", "place", "oath", "journal"
  })
  @DisplayName("every wheel icon is a sprite of its own that ships in the mod")
  void icon(final String name) throws IOException {
    final Identifier icon = WheelEntry.icon(name);
    assertEquals(Identifier.of("nightborn", "wheel/" + name), icon);
    try (InputStream in =
        WheelEntryTest.class.getResourceAsStream(
            "/assets/nightborn/textures/gui/sprites/" + icon.getPath() + ".png")) {
      assertNotNull(in, icon.toString());
      final BufferedImage image = ImageIO.read(in);
      assertEquals(16, image.getWidth());
      assertEquals(16, image.getHeight());
    }
  }
}
