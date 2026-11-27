package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.guild.GuildOathOrder;
import com.grahambartley.nightborn.guild.GuildRumour;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GuildRiteOrderTest {

  @ParameterizedTest(name = "apart from {0}")
  @ValueSource(strings = {"nightborn:guild_rumour", "nightborn:guild_oath"})
  @DisplayName("the rite is one entry on the wheel with its own icon")
  void entry(final String other) {
    final List<WheelEntry> entries = new GuildRiteOrder().entries(null, null);
    assertEquals(1, entries.size());
    final WheelEntry rite = entries.getFirst();
    assertEquals(GuildRiteOrder.ID, rite.action());
    assertEquals(Identifier.of("nightborn", "wheel/rite"), rite.icon());
    assertEquals(
        "nightborn.guild.rite.priced",
        ((TranslatableTextContent) rite.label().getContent()).getKey());
    assertEquals(
        "nightborn.guild.rite",
        ((TranslatableTextContent) GuildRiteOrder.label(0).getContent()).getKey());
    assertEquals(
        16, ((TranslatableTextContent) GuildRiteOrder.label(16).getContent()).getArgs()[0]);
    assertNotEquals(Identifier.of(other), rite.action());
    assertTrue(List.of(GuildRumour.ID, GuildOathOrder.ID).contains(Identifier.of(other)));
    assertTrue(
        Files.exists(
            Path.of("src/main/resources/assets/nightborn/textures/gui/sprites/wheel/rite.png")));
  }
}
