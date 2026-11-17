package com.grahambartley.nightborn.name;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NameStyleTest {

  private static final Path DATA = Path.of("src/main/resources/data/nightborn/nightborn");
  private static final Identifier MEN = Identifier.of("nightborn", "men");
  private static final Identifier WOMEN = Identifier.of("nightborn", "women");

  static NameStyle read(final String style) throws IOException {
    return NameStyle.CODEC
        .parse(
            JsonOps.INSTANCE,
            JsonParser.parseString(Files.readString(DATA.resolve("name_style/" + style + ".json"))))
        .getOrThrow();
  }

  @ParameterizedTest(name = "gendered={0} female={1}")
  @CsvSource({
    "true, false, nightborn:men",
    "true, true, nightborn:women",
    "false, false, nightborn:men;nightborn:women",
    "false, true, nightborn:men;nightborn:women"
  })
  @DisplayName("a gendered style draws from one sex's pool, an ungendered one from both")
  void poolsFor(final boolean gendered, final boolean female, final String pools) {
    assertEquals(
        List.of(pools.split(";")).stream().map(Identifier::of).toList(),
        new NameStyle(MEN, WOMEN, Optional.empty(), Optional.empty(), gendered).poolsFor(female));
  }

  @ParameterizedTest(name = "{0} and {1}: {2}")
  @CsvSource({
    "vampire, eldest, true",
    "vampire, vampire_lord, true",
    "vampire, hunter, false",
    "hunter, guild_master, true",
    "hunter, thrall, false",
    "thrall, human_familiar, true",
    "ghostly_familiar, thrall, true"
  })
  @DisplayName("mobs that share given-name pools avoid each other's names")
  void kin(final String one, final String other, final boolean kin) throws IOException {
    assertEquals(kin, read(one).kin(read(other)));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "vampire, nightborn:vampire_epithets, '', true",
    "vampire_lord, nightborn:vampire_epithets, '', true",
    "eldest, nightborn:eldest_epithets, '', true",
    "hunter, '', blue, true",
    "guild_master, '', blue, true",
    "human_familiar, '', '', false",
    "thrall, '', '', false",
    "ghostly_familiar, '', '', false"
  })
  @DisplayName("each named mob's style is data, and every pool it names ships")
  void shipped(
      final String style, final String epithets, final String color, final boolean gendered)
      throws IOException {
    final NameStyle read = read(style);
    assertEquals(
        Optional.of(epithets).filter(e -> !e.isEmpty()).map(Identifier::of), read.epithets());
    assertEquals(
        Optional.of(color)
            .filter(c -> !c.isEmpty())
            .map(c -> TextColor.fromFormatting(Formatting.byName(c))),
        read.color());
    assertEquals(gendered, read.gendered());
    for (final Identifier pool :
        List.of(read.men(), read.women(), read.epithets().orElse(read.men()))) {
      assertTrue(
          Files.exists(DATA.resolve("name_pool/" + pool.getPath() + ".json")), pool.toString());
    }
  }
}
