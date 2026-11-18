package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.decor.DecorBlocks;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class FeatTest {

  @ParameterizedTest(name = "{0} has {1} feats")
  @CsvSource({"VAMPIRE, 5", "HUNTER, 5", "ANYONE, 2"})
  void theTwelveFeatsSplitBySide(final Side side, final long count) throws IOException {
    assertEquals(
        count,
        ChallengeDataFiles.load("feat", Feat.CODEC).values().stream()
            .filter(feat -> feat.side() == side)
            .count());
  }

  @Test
  void everyFeatHasItsOwnTrophyAndEnglishText() throws IOException {
    final JsonObject english =
        JsonParser.parseString(Files.readString(TrialTest.LANG)).getAsJsonObject();
    final Map<String, Feat> feats = ChallengeDataFiles.load("feat", Feat.CODEC);
    assertEquals(12, feats.size());
    assertEquals(
        DecorBlocks.FEAT_TROPHIES.stream().map(id -> "nightborn:" + id).collect(Collectors.toSet()),
        feats.values().stream().map(feat -> feat.trophy().toString()).collect(Collectors.toSet()));
    feats.forEach(
        (id, feat) -> {
          assertTrue(english.has(key(feat.name())), id);
          assertTrue(english.has(key(feat.description())), id);
          assertTrue(english.has(key(feat.title())), id);
        });
  }

  private static String key(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }
}
