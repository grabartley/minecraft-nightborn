package com.grahambartley.nightborn.client.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.mob.stage.VampireStaging;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

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

  @ParameterizedTest(name = "{0} at {1} of {2}")
  @CsvSource({"12.1, 20.0, 13, 20", "60.0, 60.0, 60, 60"})
  @DisplayName("a vampire with no stage is read by its health alone")
  void noStage(final float health, final float max, final int shownHealth, final int shownMax) {
    final TranslatableTextContent line =
        (TranslatableTextContent)
            InsightReadout.line(VampireStaging.NONE, health, max).getContent();
    assertEquals(InsightReadout.HEALTH_ONLY, line.getKey());
    assertEquals(List.of(shownHealth, shownMax), List.of(line.getArgs()));
  }

  static Stream<String> stages() throws IOException {
    try (Stream<Path> files =
        Files.list(Path.of("src/main/resources/data/nightborn/nightborn/vampire_stage"))) {
      return files
          .map(file -> file.getFileName().toString().replace(".json", ""))
          .toList()
          .stream();
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("stages")
  @DisplayName("every stage a vampire can have is named in every shipped language")
  void everyStageIsNamed(final String stage) throws IOException {
    for (final String lang :
        List.of("en_us", "de_de", "es_es", "fr_fr", "pt_br", "ru_ru", "zh_cn")) {
      assertTrue(
          Files.readString(Path.of("src/main/resources/assets/nightborn/lang/" + lang + ".json"))
              .contains("\"vampire_stage.nightborn." + stage + "\""),
          lang + " " + stage);
    }
  }
}
