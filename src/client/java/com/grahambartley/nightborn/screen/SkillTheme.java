package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import net.minecraft.util.Identifier;

public record SkillTheme(
    SkillTree tree,
    int title,
    int text,
    int muted,
    int problem,
    int ring,
    int ringHover,
    int ringEdge) {

  public static SkillTheme of(final SkillTree tree) {
    return switch (tree) {
      case VAMPIRE ->
          new SkillTheme(
              tree, 0xE6C77A, 0xD8C8B0, 0x7A6A5A, 0xC0504A, 0xD0200A10, 0xE8541A26, 0xFFB8963E);
      case HUNTER ->
          new SkillTheme(
              tree, 0xD6DEE2, 0xC8C0B4, 0x6E6A64, 0xB85A4E, 0xD0181A1C, 0xE83C4248, 0xFF8A9298);
    };
  }

  private Identifier sprite(final String name) {
    return Identifier.of(Nightborn.MOD_ID, "skills/" + tree.asString() + "/" + name);
  }

  public Identifier panel() {
    return sprite("panel");
  }

  public Identifier header() {
    return sprite("header");
  }

  public Identifier tooltip() {
    return sprite("tooltip");
  }

  public Identifier point() {
    return sprite("point");
  }

  public Identifier powerSlot() {
    return sprite("power_slot");
  }

  public Identifier line(final boolean owned) {
    return sprite(owned ? "line_owned" : "line");
  }

  public Identifier frame(final SkillPurchase.Verdict verdict) {
    return sprite(
        switch (verdict) {
          case OWNED -> "frame_owned";
          case ALLOWED -> "frame_available";
          default -> "frame_locked";
        });
  }
}
