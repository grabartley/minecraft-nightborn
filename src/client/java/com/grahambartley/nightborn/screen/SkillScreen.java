package com.grahambartley.nightborn.screen;

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

  static final int LINE_WIDTH = 2;
  static final float DIM = 0.45F;

  private final Map<Identifier, Skill> skills = new HashMap<>();
  private final Map<Identifier, SkillTreeView.Node> nodes = new HashMap<>();
  private final Map<Identifier, SkillBranch> branchData = new HashMap<>();
  private List<Identifier> branches = List.of();
  private SkillTree shownTree;
  private SkillTheme theme;
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
    theme = SkillTheme.of(shownTree);
    branches = SkillTreeView.branches(branchData, shownTree);
    final String most = Integer.toString(SkillPointsRow.SIZED_FOR);
    final SkillPointsRow widest =
        SkillPointsRow.centred(
            0,
            textRenderer.getWidth(most),
            textRenderer.getWidth(Text.translatable("nightborn.skills.earned_spent", most, most)));
    layout =
        SkillLayout.of(
            width, height, branches.size(), SkillTreeView.rows(nodes, branches), widest.width());
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
    context.drawGuiTexture(
        theme.panel(), layout.left(), layout.top(), layout.width(), layout.height());
    final TreeProgress progress = progress();
    final int centre = layout.left() + layout.width() / 2;
    context.drawCenteredTextWithShadow(
        textRenderer,
        Text.translatable(shownTree.translationKey()),
        centre,
        layout.top() + SkillLayout.PAD,
        theme.title());
    List<Text> tooltip = drawPoints(context, progress, centre, mouseX, mouseY);
    if (branches.isEmpty()) {
      context.drawCenteredTextWithShadow(
          textRenderer,
          Text.translatable("nightborn.skills.empty"),
          centre,
          layout.top() + SkillLayout.HEADER,
          theme.muted());
    }
    for (int b = 0; b < branches.size(); b++) {
      drawBranchTitle(context, b, progress);
      for (final Identifier id : SkillTreeView.skillsIn(branches.get(b), nodes)) {
        drawLines(context, b, id, progress);
      }
    }
    for (int b = 0; b < branches.size(); b++) {
      for (final Identifier id : SkillTreeView.skillsIn(branches.get(b), nodes)) {
        final Skill skill = skills.get(id);
        final int x = layout.nodeX(b, branches.size(), skill.column());
        final int y = layout.nodeY(skill.row());
        final SkillPurchase.Verdict verdict = verdict(id, progress);
        drawNode(context, x, y, skill, verdict);
        if (SkillLayout.contains(x, y, mouseX, mouseY)) {
          tooltip = SkillTooltip.lines(id, skill, verdict, theme.title());
        }
      }
    }
    if (tooltip != null) {
      ThemedTooltip.draw(context, textRenderer, theme, tooltip, mouseX, mouseY);
    }
  }

  private List<Text> drawPoints(
      final DrawContext context,
      final TreeProgress progress,
      final int centre,
      final int mouseX,
      final int mouseY) {
    final String count = Integer.toString(progress.free());
    final Text detail =
        Text.translatable("nightborn.skills.earned_spent", progress.earned(), progress.spent());
    final int countWidth = textRenderer.getWidth(count);
    final SkillPointsRow row =
        SkillPointsRow.centred(centre, countWidth, textRenderer.getWidth(detail));
    final int top = layout.pointsY();
    context.drawGuiTexture(
        theme.point(), row.iconX(), top, SkillPointsRow.ICON, SkillPointsRow.ICON);
    context.getMatrices().push();
    context.getMatrices().translate(row.countX(), top + SkillPointsRow.COUNT_TOP, 0.0F);
    context.getMatrices().scale(SkillPointsRow.COUNT_SCALE, SkillPointsRow.COUNT_SCALE, 1.0F);
    context.drawTextWithShadow(
        textRenderer, count, 0, 0, progress.free() > 0 ? theme.title() : theme.muted());
    context.getMatrices().pop();
    context.drawTextWithShadow(
        textRenderer, detail, row.detailX(), top + SkillPointsRow.DETAIL_TOP, theme.muted());
    if (row.overPoints(top, mouseX, mouseY, countWidth)) {
      return List.of(Text.translatable("nightborn.skills.free_points", progress.free()));
    }
    return null;
  }

  private void drawBranchTitle(
      final DrawContext context, final int index, final TreeProgress progress) {
    final Identifier id = branches.get(index);
    final int centre = layout.branchLeft(index, branches.size()) + layout.branchWidth() / 2;
    final boolean locked = SkillPurchase.locked(progress, id, branchData.get(id));
    final boolean chosen = progress.bloodlines().contains(id);
    final Text title =
        Text.translatable(SkillNames.branch(id)).styled(style -> style.withStrikethrough(locked));
    final int bannerWidth = layout.bannerWidth(textRenderer.getWidth(title));
    context.drawGuiTexture(
        theme.header(),
        centre - bannerWidth / 2,
        layout.bannerY(),
        bannerWidth,
        SkillLayout.BANNER);
    context.drawCenteredTextWithShadow(
        textRenderer,
        title,
        centre,
        layout.bannerTextY(),
        locked ? theme.muted() : chosen ? theme.title() : theme.text());
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
    final Identifier sprite = theme.line(progress.owns(id));
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
    context.drawGuiTexture(theme.frame(verdict), x, y, SkillLayout.NODE, SkillLayout.NODE);
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
