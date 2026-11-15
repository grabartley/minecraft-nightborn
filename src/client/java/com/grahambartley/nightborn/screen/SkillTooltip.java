package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.client.PowerSlotView;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillNames;
import com.grahambartley.nightborn.skill.SkillPurchase;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class SkillTooltip {

  private SkillTooltip() {}

  public record Power(int blood, int cooldownTicks) {}

  public static List<Text> lines(
      final Identifier id,
      final Skill skill,
      final SkillPurchase.Verdict verdict,
      final int titleColour) {
    return lines(
        id,
        skill.cost(),
        skill.requires(),
        skill.power().map(p -> new Power(p.blood(), p.cooldownTicks())),
        verdict,
        titleColour);
  }

  static List<Text> lines(
      final Identifier id,
      final int cost,
      final List<Identifier> requires,
      final Optional<Power> power,
      final SkillPurchase.Verdict verdict,
      final int titleColour) {
    final List<Text> lines = new ArrayList<>();
    lines.add(
        Text.translatable(SkillNames.skill(id))
            .styled(style -> style.withColor(TextColor.fromRgb(titleColour))));
    lines.add(Text.translatable(SkillNames.description(id)).formatted(Formatting.GRAY));
    lines.add(Text.translatable("nightborn.skills.cost", cost).formatted(Formatting.WHITE));
    if (!requires.isEmpty()) {
      MutableText needs = Text.empty();
      for (int i = 0; i < requires.size(); i++) {
        if (i > 0) {
          needs = needs.append(", ");
        }
        needs = needs.append(Text.translatable(SkillNames.skill(requires.get(i))));
      }
      lines.add(Text.translatable("nightborn.skills.needs", needs).formatted(Formatting.WHITE));
    }
    power.ifPresent(shown -> lines.add(powerLine(shown)));
    lines.add(status(verdict));
    return lines;
  }

  static Text powerLine(final Power power) {
    final long seconds = PowerSlotView.seconds(power.cooldownTicks());
    final MutableText line =
        power.blood() > 0
            ? Text.translatable("nightborn.skills.power", power.blood(), seconds)
            : Text.translatable("nightborn.skills.power.free", seconds);
    return line.formatted(Formatting.DARK_RED);
  }

  static Text status(final SkillPurchase.Verdict verdict) {
    return switch (verdict) {
      case ALLOWED ->
          Text.translatable("nightborn.skills.click_to_buy").formatted(Formatting.GREEN);
      case OWNED -> Text.translatable(verdict.translationKey()).formatted(Formatting.DARK_GREEN);
      default -> Text.translatable(verdict.translationKey()).formatted(Formatting.RED);
    };
  }
}
