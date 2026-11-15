package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.coating.Coat;
import com.grahambartley.nightborn.coating.Coating;
import com.grahambartley.nightborn.coating.CoatingTerms;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoatingTooltipTest {

  private static final CoatingTerms TERMS = new CoatingTerms(3, 5, 6000, 20, 60);

  @ParameterizedTest(name = "{0}, {1} used, ammo {2}: {4} {5}")
  @CsvSource({
    "HOLY_WATER, 0, false, -1, nightborn.coating.left, '5, 05:00'",
    "GARLIC, 2, false, 1000, nightborn.coating.left, '3, 04:10'",
    "GARLIC, 4, false, 1000, nightborn.coating.left.one, '04:10'",
    "HOLY_WATER, 0, true, -1, nightborn.coating.until_fired, ''"
  })
  @DisplayName("the tooltip names the coating and how much of it is left")
  void lines(
      final Coating coating,
      final int used,
      final boolean ammo,
      final long started,
      final String key,
      final String args) {
    final List<Text> lines =
        CoatingTooltip.lines(new Coat(coating, used, started), TERMS, 2000L, ammo);
    assertEquals(coating.translationKey(), key(lines.get(0)));
    assertEquals(key, key(lines.get(1)));
    final Object[] actual = ((TranslatableTextContent) lines.get(1).getContent()).getArgs();
    assertEquals(
        args, String.join(", ", java.util.Arrays.stream(actual).map(String::valueOf).toList()));
  }

  private static String key(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }
}
