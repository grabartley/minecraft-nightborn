package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.client.SkillKeys;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.network.BuySkillPayload;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillNames;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class SkillScreen extends Screen {

  static final Identifier PANEL = Identifier.of(Nightborn.MOD_ID, "skills/panel");
  static final Identifier FRAME_OWNED = Identifier.of(Nightborn.MOD_ID, "skills/frame_owned");
  static final Identifier FRAME_AVAILABLE =
      Identifier.of(Nightborn.MOD_ID, "skills/frame_available");
  static final Identifier FRAME_LOCKED = Identifier.of(Nightborn.MOD_ID, "skills/frame_locked");
  static final Identifier LINE = Identifier.of(Nightborn.MOD_ID, "skills/line");
  static final Identifier LINE_OWNED = Identifier.of(Nightborn.MOD_ID, "skills/line_owned");
  static final int TITLE_COLOUR = 0xE6C77A;
  static final int TEXT_COLOUR = 0xD8C8B0;
  static final int MUTED_COLOUR = 0x7A6A5A;
  static final int LINE_WIDTH = 2;
  static final int ICON = 16;

  private List<Map.Entry<Identifier, SkillBranch>> branches = List.of();
  private Registry<Skill> skills;
  private SkillLayout layout;

  public SkillScreen() {
    super(Text.translatable("nightborn.skills.title"));
  }

  @Override
  protected void init() {
    final DynamicRegistryManager registries = client.world.getRegistryManager();
    skills = Skills.skills(registries).orElse(null);
    branches =
        Skills.branches(registries)
            .map(
                registry ->
                    registry.getIds().stream()
                        .map(id -> Map.entry(id, registry.get(id)))
                        .filter(entry -> entry.getValue().tree() == tree())
                        .sorted(
                            Comparator.comparingInt(
                                    (Map.Entry<Identifier, SkillBranch> e) -> e.getValue().order())
                                .thenComparing(Map.Entry::getKey))
                        .toList())
            .orElse(List.of());
    layout = SkillLayout.of(width, height, branches.size());
  }

  private SkillTree tree() {
    return SkillTree.of(FactionAttachment.get(client.player));
  }

  private TreeProgress progress() {
    return SkillAttachment.get(client.player, tree());
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);
    context.drawGuiTexture(PANEL, layout.left(), layout.top(), layout.width(), layout.height());
    final TreeProgress progress = progress();
    final int centre = layout.left() + layout.width() / 2;
    context.drawCenteredTextWithShadow(
        textRenderer,
        Text.translatable(tree().translationKey()),
        centre,
        layout.top() + SkillLayout.PAD,
        TITLE_COLOUR);
    context.drawCenteredTextWithShadow(
        textRenderer,
        Text.translatable(
            "nightborn.skills.points", progress.earned(), progress.spent(), progress.free()),
        centre,
        layout.top() + SkillLayout.PAD + 11,
        TEXT_COLOUR);
    if (branches.isEmpty() || skills == null) {
      context.drawCenteredTextWithShadow(
          textRenderer,
          Text.translatable("nightborn.skills.empty"),
          centre,
          layout.top() + layout.height() / 2,
          MUTED_COLOUR);
      return;
    }
    List<Text> tooltip = null;
    for (int b = 0; b < branches.size(); b++) {
      drawBranchTitle(context, b, progress);
      for (final Identifier id : skillsIn(branches.get(b).getKey())) {
        drawLines(context, b, id, progress);
      }
    }
    for (int b = 0; b < branches.size(); b++) {
      for (final Identifier id : skillsIn(branches.get(b).getKey())) {
        final Skill skill = skills.get(id);
        final int x = layout.nodeX(b, branches.size(), skill.column());
        final int y = layout.nodeY(skill.row());
        final SkillPurchase.Verdict verdict = verdict(id, progress);
        drawNode(context, x, y, skill, verdict);
        if (SkillLayout.contains(x, y, mouseX, mouseY)) {
          tooltip = SkillTooltip.lines(id, skill, verdict);
        }
      }
    }
    if (tooltip != null) {
      context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
    }
  }

  private void drawBranchTitle(
      final DrawContext context, final int index, final TreeProgress progress) {
    final Map.Entry<Identifier, SkillBranch> branch = branches.get(index);
    final int centre = layout.branchLeft(index, branches.size()) + layout.branchWidth() / 2;
    final boolean locked = SkillPurchase.locked(progress, branch.getKey(), branch.getValue());
    final boolean chosen = progress.bloodlines().contains(branch.getKey());
    Text title = Text.translatable(SkillNames.branch(branch.getKey()));
    if (locked) {
      title =
          Text.translatable("nightborn.skills.branch_locked", title)
              .formatted(Formatting.STRIKETHROUGH);
    }
    context.drawCenteredTextWithShadow(
        textRenderer,
        title,
        centre,
        layout.top() + SkillLayout.HEADER - 12,
        locked ? MUTED_COLOUR : chosen ? TITLE_COLOUR : TEXT_COLOUR);
  }

  private void drawLines(
      final DrawContext context,
      final int branchIndex,
      final Identifier id,
      final TreeProgress progress) {
    final Skill skill = skills.get(id);
    final int toX =
        layout.nodeX(branchIndex, branches.size(), skill.column()) + SkillLayout.NODE / 2;
    final int toY = layout.nodeY(skill.row());
    for (final Identifier parentId : skill.requires()) {
      final Skill parent = skills.get(parentId);
      if (parent == null || !parent.branch().equals(skill.branch())) {
        continue;
      }
      final int fromX =
          layout.nodeX(branchIndex, branches.size(), parent.column()) + SkillLayout.NODE / 2;
      final int fromY = layout.nodeY(parent.row()) + SkillLayout.NODE;
      final int midY = (fromY + toY) / 2;
      final Identifier sprite = progress.owns(id) ? LINE_OWNED : LINE;
      segment(context, sprite, fromX, fromY, fromX, midY);
      segment(context, sprite, Math.min(fromX, toX), midY, Math.max(fromX, toX), midY);
      segment(context, sprite, toX, midY, toX, toY);
    }
  }

  private static void segment(
      final DrawContext context,
      final Identifier sprite,
      final int x1,
      final int y1,
      final int x2,
      final int y2) {
    final int half = LINE_WIDTH / 2;
    context.drawGuiTexture(
        sprite,
        x1 - half,
        y1 - half,
        Math.max(LINE_WIDTH, x2 - x1 + LINE_WIDTH),
        Math.max(LINE_WIDTH, y2 - y1 + LINE_WIDTH));
  }

  private void drawNode(
      final DrawContext context,
      final int x,
      final int y,
      final Skill skill,
      final SkillPurchase.Verdict verdict) {
    final Identifier frame =
        switch (verdict) {
          case OWNED -> FRAME_OWNED;
          case ALLOWED -> FRAME_AVAILABLE;
          default -> FRAME_LOCKED;
        };
    context.drawGuiTexture(frame, x, y, SkillLayout.NODE, SkillLayout.NODE);
    final int inset = (SkillLayout.NODE - ICON) / 2;
    if (verdict != SkillPurchase.Verdict.OWNED && verdict != SkillPurchase.Verdict.ALLOWED) {
      context.setShaderColor(0.45F, 0.45F, 0.45F, 1.0F);
    }
    context.drawGuiTexture(skill.icon(), x + inset, y + inset, ICON, ICON);
    context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
  }

  private List<Identifier> skillsIn(final Identifier branch) {
    return skills.getIds().stream()
        .filter(id -> skills.get(id).branch().equals(branch))
        .sorted()
        .toList();
  }

  private SkillPurchase.Verdict verdict(final Identifier id, final TreeProgress progress) {
    return Skills.check(client.world.getRegistryManager(), progress, tree(), id);
  }

  @Override
  public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
    if (button == 0 && skills != null) {
      for (int b = 0; b < branches.size(); b++) {
        for (final Identifier id : skillsIn(branches.get(b).getKey())) {
          final Skill skill = skills.get(id);
          if (SkillLayout.contains(
              layout.nodeX(b, branches.size(), skill.column()),
              layout.nodeY(skill.row()),
              mouseX,
              mouseY)) {
            if (verdict(id, progress()) == SkillPurchase.Verdict.ALLOWED) {
              ClientPlayNetworking.send(new BuySkillPayload(id));
              client
                  .getSoundManager()
                  .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
          }
        }
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
    if (SkillKeys.openSkills().matchesKey(keyCode, scanCode)) {
      close();
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean shouldPause() {
    return false;
  }
}
