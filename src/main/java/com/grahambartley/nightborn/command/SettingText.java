package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.config.BoolSetting;
import com.grahambartley.nightborn.config.IntSetting;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.config.Setting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.Text;

public final class SettingText {

  private SettingText() {}

  public static Text value(final Setting<?> setting, final NightbornConfig config) {
    return Text.translatable(
        "nightborn.command.config.value",
        Text.translatable(setting.translationKey()),
        setting.id(),
        String.valueOf(setting.get(config)));
  }

  public static Text range(final Setting<?> setting) {
    return switch (setting) {
      case IntSetting number ->
          Text.translatable(
              "nightborn.command.config.range.number",
              setting.id(),
              number.min(),
              number.max(),
              number.defaultValue());
      case BoolSetting flag ->
          Text.translatable(
              "nightborn.command.config.range.toggle", setting.id(), flag.defaultValue());
    };
  }

  public static List<Text> status(final NightbornConfig config) {
    final List<Text> lines = new ArrayList<>();
    lines.add(Text.translatable("nightborn.command.status.header"));
    String section = "";
    for (final Setting<?> setting : NightbornConfig.SETTINGS) {
      if (!setting.section().equals(section)) {
        section = setting.section();
        lines.add(Text.translatable(setting.sectionKey()));
      }
      lines.add(value(setting, config));
    }
    return lines;
  }
}
