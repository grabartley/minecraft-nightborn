package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.age.AgeLadder;
import com.grahambartley.nightborn.age.AgeNeeds;
import com.grahambartley.nightborn.age.AgeOutlook;
import com.grahambartley.nightborn.age.AgeProgress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;

public final class AgeLine {

  public static final int LINE_GAP = 2;
  static final int PANEL_GAP = 6;
  static final int EDGE = 4;
  static final int MAX_LINES = 3;

  private AgeLine() {}

  public record Ahead(AgeOutlook outlook, Text name, Optional<Text> deed) {}

  public static Optional<Ahead> ahead(
      final AgeProgress progress, final AgeLadder ladder, final List<AgeNeeds> needs) {
    return AgeOutlook.of(progress, ladder.size(), needs)
        .flatMap(
            outlook ->
                ladder
                    .rung(outlook.next())
                    .map(
                        rung ->
                            new Ahead(
                                outlook,
                                rung.age().name(),
                                rung.age().deed().map(deed -> deed.description()))));
  }

  public static List<Text> lines(final Optional<Text> stage, final Optional<Ahead> ahead) {
    final List<Text> lines = new ArrayList<>();
    stage.ifPresent(name -> lines.add(Text.translatable("nightborn.age.stage", name)));
    if (ahead.isEmpty()) {
      lines.add(Text.translatable("nightborn.age.oldest"));
      return lines;
    }
    final Ahead next = ahead.get();
    lines.add(
        next.outlook().opened()
            ? Text.translatable("nightborn.age.open", next.name())
            : Text.translatable(
                "nightborn.age.waiting",
                next.outlook().nightsLeft(),
                next.outlook().bloodLeft(),
                next.name()));
    next.deed()
        .ifPresent(
            deed ->
                lines.add(
                    Text.translatable(
                        next.outlook().deedDone()
                            ? "nightborn.age.deed.done"
                            : "nightborn.age.deed",
                        deed)));
    return lines;
  }

  public static int top(final int panelBottom) {
    return panelBottom + PANEL_GAP;
  }

  public static int footer(final int fontHeight) {
    return PANEL_GAP + MAX_LINES * (fontHeight + LINE_GAP) + EDGE;
  }
}
