package com.grahambartley.nightborn.challenge;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class FeatLines {

  static final String WORN = "nightborn.journal.feats.worn";

  public record Line(Identifier feat, Text name, Text title, boolean worn) {

    public Text shownTitle() {
      return worn ? Text.translatable(WORN, title) : title;
    }

    public Optional<Identifier> choiceOnClick() {
      return worn ? Optional.empty() : Optional.of(feat);
    }
  }

  private FeatLines() {}

  public static List<Line> of(final FeatLog log, final Map<Identifier, Feat> feats) {
    return log.earned().keySet().stream()
        .filter(feats::containsKey)
        .sorted(Comparator.comparing(Identifier::toString))
        .map(
            id ->
                new Line(
                    id,
                    feats.get(id).name(),
                    feats.get(id).title(),
                    log.title().filter(id::equals).isPresent()))
        .toList();
  }
}
