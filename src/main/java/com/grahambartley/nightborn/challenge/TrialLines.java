package com.grahambartley.nightborn.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class TrialLines {

  static final String PROGRESS = "nightborn.journal.trials.progress";
  static final String DONE = "nightborn.journal.trials.done";
  static final String UNTAKEN = "nightborn.journal.trials.untaken";

  public record Line(Text status, Text description) {}

  private TrialLines() {}

  public static List<Line> of(final TrialOffer offer, final Map<Identifier, Trial> trials) {
    final List<Line> lines = new ArrayList<>();
    for (final OfferedTrial offered : offer.trials()) {
      final Trial trial = trials.get(offered.trial());
      if (trial != null) {
        lines.add(new Line(status(offered, trial), trial.description()));
      }
    }
    return List.copyOf(lines);
  }

  static Text status(final OfferedTrial offered, final Trial trial) {
    if (offered.done()) {
      return Text.translatable(DONE, trial.name());
    }
    if (!offered.taken()) {
      return Text.translatable(UNTAKEN, trial.name());
    }
    return Text.translatable(
        PROGRESS, trial.name(), trial.goal().progress(offered.tally()), trial.goal().count());
  }
}
