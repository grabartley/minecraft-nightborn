package com.grahambartley.nightborn.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.config.SunSettings;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SettingTextTest {

  @Test
  @DisplayName("status lists a header, each section once and every setting")
  void statusListsEverything() {
    final List<Text> lines = SettingText.status(NightbornConfig.DEFAULTS);
    assertEquals(
        1 + NightbornConfig.sections().size() + NightbornConfig.SETTINGS.size(), lines.size());
  }

  @Test
  @DisplayName("a setting's line shows its name, id and current value")
  void valueLine() {
    final Text line =
        SettingText.value(
            SunSettings.SUN_DAMAGE, NightbornConfig.DEFAULTS.with(SunSettings.SUN_DAMAGE, 9));
    final TranslatableTextContent content = (TranslatableTextContent) line.getContent();
    assertEquals("nightborn.command.config.value", content.getKey());
    assertEquals("sun.sunDamage", content.getArgs()[1]);
    assertEquals("9", content.getArgs()[2]);
  }

  @Test
  @DisplayName("a number setting's range shows its bounds and default")
  void rangeLine() {
    final TranslatableTextContent content =
        (TranslatableTextContent) SettingText.range(SunSettings.SUN_DAMAGE).getContent();
    assertEquals("nightborn.command.config.range.number", content.getKey());
    assertEquals(List.of("sun.sunDamage", 1, 40, 2), List.of(content.getArgs()));
  }
}
