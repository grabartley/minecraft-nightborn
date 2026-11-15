package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.client.PowerSlotView;
import com.grahambartley.nightborn.client.SkillKeys;
import com.grahambartley.nightborn.network.BuySkillPayload;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillLookup;
import com.grahambartley.nightborn.skill.SkillNames;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class SkillScreen extends Screen {

  static final Identifier PANEL = Identifier.of(Nightborn.MOD_ID, "skills/panel");
  static final Identifier LINE = Identifier.of(Nightborn.MOD_ID, "skills/line");
  static final Identifier LINE_OWNED = Identifier.of(Nightborn.MOD_ID, "skills/line_owned");
  static final int TITLE_COLOUR = 0xE6C77A;
  static final int TEXT_COLOUR = 0xD8C8B0;
  static final int MUTED_COLOUR = 0x7A6A5A;
  static final int LINE_WIDTH = 2;
  static final float DIM = 0.45F;

  private final Map<Identifier, Skill> skills = new HashMap<>();
  private final Map<Identifier, SkillTreeView.Node> nodes = new HashMap<>();
  private final Map<Identifier, SkillBranch> branchData = new HashMap<>();
  private List<Identifier> branches = List.of();
  private SkillTree shownTree;
  private SkillLayout layout;

  public SkillScreen() {
    super(Text.translatable("nightborn.skills.title"));
  }

  @Override
  protected void init() {
    final DynamicRegistryManager registries = client.world.getRegistryManager();
    skills.clear();
    nodes.clear();
    branchData.clear();
    skills.putAll(Skills.allSkills(registries));
    branchData.putAll(Skills.allBranches(registries));
    skills.forEach(
        (id, skill) ->
            nodes.put(id, new SkillTreeView.Node(skill.branch(), skill.column(), skill.row())));
    shownTree = tree();
    branches = SkillTreeView.branches(branchData, shownTree);
    layout = SkillLayout.of(width, height, branches.size(), SkillTreeView.rows(nodes, branches));
  }

  private SkillTree tree() {
    return SkillTree.of(client.player);
  }

  private TreeProgress progress() {
    return SkillAttachment.get(client.player, shownTree);
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    if (tree() != shownTree) {
      clearAndInit();
    }
    super.render(context, mouseX, mouseY, delta);
    context.drawGuiTexture(PANEL, layout.left(), layout.top(), layout.width(), layout.height());
    final TreeProgress progress = progress();
    final int centre = layout.left() + layout.width() / 2;
    context.drawCenteredTextWithShadow(
        textRenderer,
        Text.translatable(shownTree.translationKey()),
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
    if (branches.isEmpty()) {
      context.drawCenteredTextWithShadow(
          textRenderer,
          Text.translatable("nightborn.skills.empty"),
          centre,
          layout.top() + SkillLayout.HEADER,
          MUTED_COLOUR);
      return;
    }
    for (int b = 0; b < branches.size(); b++) {
      drawBranchTitle(context, b, progress);
      for (final Identifier id : SkillTreeView.skillsIn(branches.get(b), nodes)) {
        drawLines(context, b, id, progress);
      }
    }
    List<Text> tooltip = null;
    for (int b = 0; b < branches.size(); b++) {
      for (final Identifier id : SkillTreeView.skillsIn(branches.get(b), nodes)) {
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
    final Identifier id = branches.get(index);
    final int centre = layout.branchLeft(index, branches.size()) + layout.branchWidth() / 2;
    final boolean locked = SkillPurchase.locked(progress, id, branchData.get(id));
    final boolean chosen = progress.bloodlines().contains(id);
    Text title = Text.translatable(SkillNames.branch(id));
    if (locked) {
      title = Text.translatable("nightborn.skills.branch_locked", title);
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
    final Identifier sprite = progress.owns(id) ? LINE_OWNED : LINE;
    for (final Identifier parentId : skill.requires()) {
      final Skill parent = skills.get(parentId);
      if (parent == null || !parent.branch().equals(skill.branch())) {
        continue;
      }
      final int fromX =
          layout.nodeX(branchIndex, branches.size(), parent.column()) + SkillLayout.NODE / 2;
      final int fromY = layout.nodeY(parent.row()) + SkillLayout.NODE;
      for (final SkillTreeView.Segment segment :
          SkillTreeView.elbow(fromX, fromY, toX, toY, LINE_WIDTH)) {
        context.drawGuiTexture(sprite, segment.x(), segment.y(), segment.width(), segment.height());
      }
    }
  }

  private void drawNode(
      final DrawContext context,
      final int x,
      final int y,
      final Skill skill,
      final SkillPurchase.Verdict verdict) {
    context.drawGuiTexture(SkillTreeView.frame(verdict), x, y, SkillLayout.NODE, SkillLayout.NODE);
    final int inset = (SkillLayout.NODE - PowerSlotView.ICON) / 2;
    if (SkillTreeView.dimmed(verdict)) {
      context.setShaderColor(DIM, DIM, DIM, 1.0F);
    }
    context.drawGuiTexture(
        skill.icon(), x + inset, y + inset, PowerSlotView.ICON, PowerSlotView.ICON);
    context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
  }

  private SkillPurchase.Verdict verdict(final Identifier id, final TreeProgress progress) {
    return SkillLookup.of(client.world.getRegistryManager(), progress, shownTree, id).verdict();
  }

  @Override
  public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
    if (button == 0) {
      for (int b = 0; b < branches.size(); b++) {
        for (final Identifier id : SkillTreeView.skillsIn(branches.get(b), nodes)) {
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
