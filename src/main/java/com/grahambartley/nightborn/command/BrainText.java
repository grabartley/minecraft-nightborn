package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.mob.brain.PowerChoice;
import com.grahambartley.nightborn.mob.brain.Think;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.text.Text;

final class BrainText {

  static final int SHOWN = 3;

  private BrainText() {}

  static List<Text> lines(final Text name, final Think think, final long now) {
    final List<Text> lines = new ArrayList<>();
    lines.add(
        Text.translatable(
            "nightborn.command.brain.think", name, now - think.tick(), think.intent().key()));
    if (think.ranked().isEmpty()) {
      lines.add(Text.translatable("nightborn.command.brain.nothing"));
      return lines;
    }
    final Optional<PowerChoice.Scored> chosen = think.chosen();
    think.ranked().stream()
        .limit(SHOWN)
        .forEach(scored -> lines.add(line(scored, chosen.filter(scored::equals).isPresent())));
    return lines;
  }

  private static Text line(final PowerChoice.Scored scored, final boolean chosen) {
    final String key = chosen ? "chosen" : scored.affordable() ? "use" : "costly";
    return Text.translatable(
        "nightborn.command.brain." + key,
        scored.option().power().toString(),
        String.format(Locale.ROOT, "%.2f", scored.score()));
  }
}
