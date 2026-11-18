package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import net.minecraft.util.Identifier;

public record SkillTheme(
    SkillTree tree, int title, int text, int muted, int problem, int ringHover, int ringEdge) {

  public static SkillTheme of(final SkillTree tree) {
    return switch (tree) {
      case VAMPIRE ->
          new SkillTheme(tree, 0xE6C77A, 0xD8C8B0, 0x7A6A5A, 0xC0504A, 0x48F08A50, 0xFFB8963E);
      case HUNTER ->
          new SkillTheme(tree, 0xD6DEE2, 0xC8C0B4, 0x6E6A64, 0xB85A4E, 0x38E8EEF2, 0xFF8A9298);
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

  public Identifier powerSlot(final PowerSlotLook look) {
    return sprite(
        switch (look) {
          case FILLED -> "power_slot";
          case EMPTY -> "power_slot_empty";
          case LIFTED -> "power_slot_lifted";
          case TARGET -> "power_slot_drop";
        });
  }

  public Identifier wheelRing() {
    return sprite("wheel_ring");
  }

  public Identifier wheelSlot(final boolean hovered, final boolean current) {
    return sprite(hovered ? "wheel_slot_hover" : current ? "wheel_slot_current" : "wheel_slot");
  }

  public Identifier experienceBar() {
    return sprite("experience_bar");
  }

  public Identifier experienceFill() {
    return sprite("experience_fill");
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
