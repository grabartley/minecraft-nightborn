package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FirstNewsTest {

  @ParameterizedTest(name = "vampire {0}, factions shown {1}: named {2}")
  @CsvSource({"false, false, true", "false, true, true", "true, false, false", "true, true, true"})
  void aVampireIsNamedOnlyWhenTheServerShowsFactions(
      final boolean vampire, final boolean shown, final boolean named) {
    assertEquals(named, FirstNews.named(vampire, shown));
    final Text line = FirstNews.line(ServerFirst.ANCIENT, Text.literal("Mina"), vampire, shown);
    final TranslatableTextContent content = (TranslatableTextContent) line.getContent();
    assertEquals(
        named ? "nightborn.first.ancient" : "nightborn.first.ancient.unnamed", content.getKey());
    assertEquals(named ? 1 : 0, content.getArgs().length);
  }
}
