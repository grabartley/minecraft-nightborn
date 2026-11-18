package com.grahambartley.nightborn.client.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InsightReadoutTest {

  @ParameterizedTest(name = "{0} at {1} of {2}")
  @CsvSource({
    "nightborn:elder, 31.2, 40.0, vampire_stage.nightborn.elder, 32, 40",
    "nightborn:fledgling, 0.5, 20.0, vampire_stage.nightborn.fledgling, 1, 20",
    "nightborn:adult, 30.0, 30.0, vampire_stage.nightborn.adult, 30, 30"
  })
  @DisplayName("the monocle names the vampire's stage and its health, rounded up")
  void line(
      final String stage,
      final float health,
      final float max,
      final String stageKey,
      final int shownHealth,
      final int shownMax) {
    final TranslatableTextContent line =
        (TranslatableTextContent)
            InsightReadout.line(Identifier.of(stage), health, max).getContent();
    assertEquals(InsightReadout.LINE, line.getKey());
    assertEquals(
        stageKey, ((TranslatableTextContent) ((Text) line.getArgs()[0]).getContent()).getKey());
    assertEquals(List.of(shownHealth, shownMax), List.of(line.getArgs()[1], line.getArgs()[2]));
  }
}
