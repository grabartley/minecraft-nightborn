package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.faith.Flame;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FlameTooltipTest {

  @ParameterizedTest(name = "burns until {0}, now {1}: {2} left")
  @CsvSource({"3800, 2000, '01:30'", "2400, 2000, '00:20'", "2000, 2000, '00:00'"})
  @DisplayName("a weapon alight says so and shows how long it burns")
  void lines(final long until, final long now, final String left) {
    final List<Text> lines = FlameTooltip.lines(new Flame(until, 3.0F, 100), now);
    assertEquals("nightborn.blood_fire.alight", key(lines.get(0)));
    assertEquals("nightborn.coating.left.time", key(lines.get(1)));
    assertEquals(
        left, String.valueOf(((TranslatableTextContent) lines.get(1).getContent()).getArgs()[0]));
  }

  private static String key(final Text text) {
    return ((TranslatableTextContent) text.getContent()).getKey();
  }
}
