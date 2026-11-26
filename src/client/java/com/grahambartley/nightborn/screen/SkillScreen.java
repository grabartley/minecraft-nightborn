package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.age.AgeLadder;
import com.grahambartley.nightborn.age.AgeProgress;
import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.client.PowerSlotView;
import com.grahambartley.nightborn.client.SkillKeys;
import com.grahambartley.nightborn.client.SyncedServerConfig;
import com.grahambartley.nightborn.config.SkillSettings;
import com.grahambartley.nightborn.network.BuySkillPayload;
import com.grahambartley.nightborn.rank.GuildRanks;
import com.grahambartley.nightborn.skill.PowerBar;
import com.grahambartley.nightborn.skill.PowerLayout;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillCurve;
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
import org.jetbrains.annotations.Nullable;

public final class SkillScreen extends Screen {

  static final int LINE_WIDTH = 2;
  static final float DIM = 0.45F;
  static final float DRAG_Z = 300.0F;

  private final Map<Identifier, Skill> skills = new HashMap<>();
  private final Map<Identifier, SkillTreeView.Node> nodes = new HashMap<>();
  private final Map<Identifier, SkillBranch> branchData = new HashMap<>();
  private List<Identifier> branches = List.of();
  private SkillTree shownTree;
  private SkillTheme theme;
  private SkillLayout layout;
  @Nullable private PowerDrag drag;

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
            Math.max(
                textRenderer.getWidth(
                    Text.translatable("nightborn.skills.earned_spent", most, most)),
                ExperienceBar.MIN_WIDTH));
    layout =
        SkillLayout.of(
            width,
            height,
            branches.size(),
            SkillTreeView.rows(nodes, branches),
            widest.width(),
            shownTree == SkillTree.VAMPIRE ? AgeLine.footer(textRenderer.fontHeight) : 0);
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
    final int x = (int) layout.unzoom(mouseX);
    final int y = (int) layout.unzoom(mouseY);
    context.getMatrices().push();
    context.getMatrices().scale((float) layout.zoom(), (float) layout.zoom(), 1.0F);
    final List<Text> tooltip = drawTree(context, x, y);
    if (drag != null) {
      drawDragged(context, x, y);
    }
    context.getMatrices().pop();
    if (drag == null && tooltip != null) {
      ThemedTooltip.draw(context, textRenderer, theme, tooltip, mouseX, mouseY);
    }
  }

  @Nullable
  private List<Text> drawTree(final DrawContext context, final int mouseX, final int mouseY) {
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
        drawNode(context, x, y, skill, progress.rank(id), verdict);
        if (SkillLayout.contains(x, y, mouseX, mouseY)) {
          tooltip =
              SkillTooltip.lines(
                  id,
                  SkillTooltip.Facts.of(skill, progress.rank(id)),
                  verdict,
                  shownTree,
                  theme.title());
        }
      }
    }
    if (shownTree == SkillTree.VAMPIRE) {
      drawAge(context, centre);
    }
    final List<Text> barTip = drawPowerBar(context, progress, centre, mouseX, mouseY);
    if (barTip != null) {
      tooltip = barTip;
    }
    return tooltip;
  }

  private void drawAge(final DrawContext context, final int centre) {
    final AgeProgress progress = AgeAttachment.get(client.player);
    final AgeLadder ladder = AgeLadder.of(client.world.getRegistryManager());
    final List<Text> lines =
        AgeLine.lines(
            ladder.rung(progress.rank()).map(rung -> rung.age().name()),
            AgeLine.ahead(progress, ladder, SyncedServerConfig.get().age().needs()));
    final int top = AgeLine.top(layout.bottom());
    for (int line = 0; line < lines.size(); line++) {
      context.drawCenteredTextWithShadow(
          textRenderer,
          lines.get(line),
          centre,
          top + line * (textRenderer.fontHeight + AgeLine.LINE_GAP),
          line == 0 ? theme.title() : theme.muted());
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
    final int detailWidth = Math.max(textRenderer.getWidth(detail), ExperienceBar.MIN_WIDTH);
    final SkillPointsRow row = SkillPointsRow.centred(centre, countWidth, detailWidth);
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
    final List<Text> experience =
        drawExperience(
            context, progress, ExperienceBar.under(row, top, detailWidth), mouseX, mouseY);
    if (row.overPoints(top, mouseX, mouseY, countWidth)) {
      return List.of(Text.translatable("nightborn.skills.free_points", progress.free()));
    }
    return experience;
  }

  private List<Text> drawExperience(
      final DrawContext context,
      final TreeProgress progress,
      final ExperienceBar bar,
      final int mouseX,
      final int mouseY) {
    final SkillSettings settings = SyncedServerConfig.get().skills();
    final int cost =
        SkillCurve.cost(
            progress.earned(), settings.firstPointExperience(), settings.pointCostRise());
    context.drawGuiTexture(
        theme.experienceBar(),
        bar.left(),
        bar.top(),
        bar.right() - bar.left(),
        ExperienceBar.HEIGHT);
    final int filled =
        bar.filled(
            SkillCurve.fraction(
                progress.earned(),
                progress.experience(),
                settings.firstPointExperience(),
                settings.pointCostRise()));
    if (filled > 0) {
      context.drawGuiTexture(
          theme.experienceFill(),
          bar.innerLeft(),
          bar.innerTop(),
          filled,
          bar.innerBottom() - bar.innerTop());
    }
    context.drawCenteredTextWithShadow(
        textRenderer,
        Text.translatable(
            "nightborn.skills.experience", ExperienceBar.shown(progress.experience()), cost),
        bar.centre(),
        bar.textTop(),
        theme.text());
    return bar.contains(mouseX, mouseY)
        ? List.of(Text.translatable("nightborn.skills.experience.tooltip"))
        : null;
  }

  private List<Text> drawPowerBar(
      final DrawContext context,
      final TreeProgress progress,
      final int centre,
      final int mouseX,
      final int mouseY) {
    final PowerBarRow row = PowerBarRow.centred(centre, layout.powerBarY());
    final PowerLayout bar =
        PowerBar.layout(progress, id -> skills.containsKey(id) && skills.get(id).active());
    final int inset = (PowerBarRow.SLOT - PowerSlotView.ICON) / 2;
    final int over = row.slotAt(mouseX, mouseY);
    List<Text> tooltip = null;
    for (int slot = 0; slot < PowerLayout.SLOTS; slot++) {
      final int x = row.slotX(slot);
      final Identifier id = bar.at(slot).orElse(null);
      final boolean lifted = drag != null && drag.fromSlot() == slot;
      context.drawGuiTexture(
          theme.powerSlot(PowerSlotLook.of(id != null, lifted, drag != null && slot == over)),
          x,
          row.top(),
          PowerBarRow.SLOT,
          PowerBarRow.SLOT);
      if (id != null && !lifted) {
        context.drawGuiTexture(
            skills.get(id).icon(),
            x + inset,
            row.top() + inset,
            PowerSlotView.ICON,
            PowerSlotView.ICON);
      }
      drawSmall(
          context,
          PowerSlotView.keyLabel(
              SkillKeys.power(slot).getBoundKeyLocalizedText().getString(),
              SkillKeys.power(slot).isUnbound()),
          CornerText.leftX(x, inset),
          CornerText.topY(row.top(), inset),
          theme.text());
      if (slot == over) {
        tooltip =
            id == null
                ? List.of(Text.translatable("nightborn.skills.bar.empty"))
                : List.of(
                    Text.translatable(SkillNames.skill(id)),
                    Text.translatable("nightborn.skills.bar.filled"));
      }
    }
    return tooltip;
  }

  private void drawDragged(final DrawContext context, final int mouseX, final int mouseY) {
    final Skill skill = skills.get(drag.skill());
    if (skill == null) {
      return;
    }
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, DRAG_Z);
    context.drawGuiTexture(
        skill.icon(),
        mouseX - PowerSlotView.ICON / 2,
        mouseY - PowerSlotView.ICON / 2,
        PowerSlotView.ICON,
        PowerSlotView.ICON);
    context.getMatrices().pop();
  }

  private void drawSmall(
      final DrawContext context, final String text, final int x, final int y, final int colour) {
    context.getMatrices().push();
    context.getMatrices().translate(x, y, 0.0F);
    context.getMatrices().scale(CornerText.SCALE, CornerText.SCALE, 1.0F);
    context.drawTextWithShadow(textRenderer, text, 0, 0, colour);
    context.getMatrices().pop();
  }

  private void drawBranchTitle(
      final DrawContext context, final int index, final TreeProgress progress) {
    final Identifier id = branches.get(index);
    final int centre = layout.branchLeft(index, branches.size()) + layout.branchWidth() / 2;
    final boolean locked = SkillPurchase.locked(progress, id, branchData.get(id));
    final boolean chosen = progress.lines().contains(id);
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
      final int rank,
      final SkillPurchase.Verdict verdict) {
    final SkillPurchase.Verdict look = SkillTreeView.look(verdict, rank > 0);
    context.drawGuiTexture(theme.frame(look), x, y, SkillLayout.NODE, SkillLayout.NODE);
    final int inset = (SkillLayout.NODE - PowerSlotView.ICON) / 2;
    if (SkillTreeView.dimmed(look)) {
      context.setShaderColor(DIM, DIM, DIM, 1.0F);
    }
    context.drawGuiTexture(
        skill.icon(), x + inset, y + inset, PowerSlotView.ICON, PowerSlotView.ICON);
    context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    if (skill.maxRank() > 1) {
      final String label = rank + "/" + skill.maxRank();
      drawSmall(
          context,
          label,
          CornerText.rightX(x, SkillLayout.NODE, inset, textRenderer.getWidth(label)),
          CornerText.bottomY(y, SkillLayout.NODE, inset),
          rank > 0 ? theme.title() : theme.text());
    }
  }

  private SkillPurchase.Verdict verdict(final Identifier id, final TreeProgress progress) {
    return SkillLookup.of(
            client.world.getRegistryManager(),
            progress,
            shownTree,
            id,
            StageRanks.of(client.player),
            GuildRanks.of(client.player))
        .verdict();
  }

  @Override
  public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
    final double x = layout.unzoom(mouseX);
    final double y = layout.unzoom(mouseY);
    if (button == 0) {
      final TreeProgress held = progress();
      final PowerBarRow row =
          PowerBarRow.centred(layout.left() + layout.width() / 2, layout.powerBarY());
      final int slot = row.slotAt(x, y);
      if (slot != PowerBarRow.NONE) {
        PowerBar.layout(held, id -> skills.containsKey(id) && skills.get(id).active())
            .at(slot)
            .ifPresent(id -> drag = new PowerDrag(id, slot));
        return true;
      }
      for (int b = 0; b < branches.size(); b++) {
        for (final Identifier id : SkillTreeView.skillsIn(branches.get(b), nodes)) {
          final Skill skill = skills.get(id);
          if (SkillLayout.contains(
              layout.nodeX(b, branches.size(), skill.column()), layout.nodeY(skill.row()), x, y)) {
            if (verdict(id, progress()) == SkillPurchase.Verdict.ALLOWED) {
              ClientPlayNetworking.send(new BuySkillPayload(id));
              client
                  .getSoundManager()
                  .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            } else if (skill.active() && held.owns(id)) {
              drag = PowerDrag.fromTree(id);
            }
            return true;
          }
        }
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
    if (button == 0 && drag != null) {
      final PowerBarRow row =
          PowerBarRow.centred(layout.left() + layout.width() / 2, layout.powerBarY());
      drag.drop(row.slotAt(layout.unzoom(mouseX), layout.unzoom(mouseY)))
          .ifPresent(
              payload -> {
                ClientPlayNetworking.send(payload);
                client
                    .getSoundManager()
                    .play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.2F));
              });
      drag = null;
      return true;
    }
    return super.mouseReleased(mouseX, mouseY, button);
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
