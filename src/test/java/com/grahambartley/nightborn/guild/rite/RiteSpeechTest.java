package com.grahambartley.nightborn.guild.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class RiteSpeechTest {

  private static final Text TELLER = Text.literal("Aldous");

  @ParameterizedTest(name = "{0} is told as {1} with {2}")
  @CsvSource({
    "WAIT, nightborn.guild.rite.wait, 12:30",
    "PRICE, nightborn.guild.rite.price, 16",
    "RANK, nightborn.guild.rite.rank, Aldous"
  })
  @DisplayName("each refusal has its own line, naming the Guild Master and what is short")
  void refused(final RiteAccess.Refusal refusal, final String key, final String said) {
    final TranslatableTextContent line =
        (TranslatableTextContent)
            RiteSpeech.refused(refusal, TELLER, 16, 750L).orElseThrow().getContent();
    assertEquals(key, line.getKey());
    assertEquals(TELLER, line.getArgs()[0]);
    assertEquals(
        said,
        String.valueOf(
            line.getArgs()[line.getArgs().length - 1] instanceof Text t
                ? t.getString()
                : line.getArgs()[line.getArgs().length - 1]));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(RiteSpeech.Line.class)
  @DisplayName("each line of the rite is its own key, spoken by the Guild Master")
  void lines(final RiteSpeech.Line said) {
    final TranslatableTextContent line = (TranslatableTextContent) said.by(TELLER).getContent();
    assertTrue(line.getKey().startsWith("nightborn.guild.rite."));
    assertEquals(said.name().toLowerCase(java.util.Locale.ROOT), line.getKey().substring(21));
    assertEquals(TELLER, line.getArgs()[0]);
    assertEquals(Optional.empty(), RiteSpeech.refused(RiteAccess.Refusal.NONE, TELLER, 16, 0L));
  }
}
