package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.config.IntSetting;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

final class IntSettingSlider extends SliderWidget {

  private final IntSetting setting;
  private final SliderScale scale;
  private final IntConsumer sink;
  private int current;

  IntSettingSlider(
      final int width,
      final int height,
      final IntSetting setting,
      final int initial,
      final IntConsumer sink) {
    super(
        0,
        0,
        width,
        height,
        Text.empty(),
        new SliderScale(setting.min(), setting.max()).fractionOf(initial));
    this.setting = setting;
    this.scale = new SliderScale(setting.min(), setting.max());
    this.current = scale.clamp(initial);
    this.sink = sink;
    updateMessage();
  }

  @Override
  protected void updateMessage() {
    setMessage(Text.translatable(setting.translationKey()).append(": " + current));
  }

  @Override
  protected void applyValue() {
    current = scale.valueAt(this.value);
    sink.accept(current);
  }
}
