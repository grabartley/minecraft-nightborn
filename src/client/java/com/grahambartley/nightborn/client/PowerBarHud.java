package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.screen.PowerSlotLook;
import com.grahambartley.nightborn.screen.SkillTheme;
import com.grahambartley.nightborn.skill.PowerBar;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerCooldowns;
import com.grahambartley.nightborn.skill.PowerLayout;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillPower;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.registry.Registry;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

public final class PowerBarHud {

  static final Identifier COOLDOWN = Identifier.of(Nightborn.MOD_ID, "hud/power_cooldown");
  static final int COST_COLOUR = 0xE04040;
  static final int KEY_COLOUR = 0xC8B8A0;
  static final int READY_COLOUR = 0xFFFFFF;
  static final float KEY_SCALE = 0.5F;

  private PowerBarHud() {}

  public static void render(final DrawContext context, final RenderTickCounter tickCounter) {
    final MinecraftClient client = MinecraftClient.getInstance();
    final ClientPlayerEntity player = client.player;
    if (player == null
        || client.world == null
        || client.options.hudHidden
        || player.isSpectator()) {
      return;
    }
    final Registry<Skill> skills = Skills.skills(client.world.getRegistryManager()).orElse(null);
    final SkillTree tree = SkillTree.of(player);
    final PowerLayout bar = PowerBar.layout(skills, SkillAttachment.get(player, tree));
    if (bar.isEmpty()) {
      return;
    }
    final boolean rightHanded = player.getMainArm() == Arm.RIGHT;
    final boolean offhandOnRight = !rightHanded && !player.getOffHandStack().isEmpty();
    final boolean indicatorOnRight =
        rightHanded && client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR;
    final List<PowerBarLayout.Point> points =
        PowerBarLayout.slots(
            context.getScaledWindowWidth(),
            context.getScaledWindowHeight(),
            bar.used(),
            offhandOnRight,
            indicatorOnRight);
    final PowerCooldowns cooldowns = PowerCooldownAttachment.get(player);
    final long now = client.world.getTime();
    final int blood = BloodAttachment.get(player);
    final SkillTheme theme = SkillTheme.of(tree);
    for (int i = 0; i < bar.used(); i++) {
      final PowerBarLayout.Point at = points.get(i);
      final Identifier id = bar.at(i).orElse(null);
      context.drawGuiTexture(
          theme.powerSlot(id != null ? PowerSlotLook.FILLED : PowerSlotLook.EMPTY),
          at.x(),
          at.y(),
          PowerBarLayout.SLOT,
          PowerBarLayout.SLOT);
      if (id != null) {
        final Skill skill = skills.get(id);
        final SkillPower power = skill.power().orElseThrow();
        drawPower(
            context,
            client,
            at,
            skill,
            cooldowns.ticksLeft(id, now),
            Powers.cooldownTicks(player, id, power),
            Powers.bloodCost(player, id, power),
            blood);
      }
      drawKey(context, client, at, i);
    }
  }

  private static void drawPower(
      final DrawContext context,
      final MinecraftClient client,
      final PowerBarLayout.Point at,
      final Skill skill,
      final long ticksLeft,
      final int cooldownTicks,
      final int cost,
      final int blood) {
    final int size = PowerBarLayout.SLOT;
    final int inset = (size - PowerSlotView.ICON) / 2;
    final boolean thirsty = blood < cost;
    if (thirsty) {
      context.setShaderColor(0.5F, 0.3F, 0.3F, 1.0F);
    }
    context.drawGuiTexture(
        skill.icon(), at.x() + inset, at.y() + inset, PowerSlotView.ICON, PowerSlotView.ICON);
    context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    final float shade = PowerCooldowns.fraction(ticksLeft, cooldownTicks);
    if (shade > 0.0F) {
      final int top = PowerSlotView.shadeTop(shade);
      context.drawGuiTexture(
          COOLDOWN,
          PowerSlotView.ICON,
          PowerSlotView.ICON,
          0,
          top,
          at.x() + inset,
          at.y() + inset + top,
          PowerSlotView.ICON,
          PowerSlotView.ICON - top);
      final String seconds = Long.toString(PowerSlotView.seconds(ticksLeft));
      context.drawCenteredTextWithShadow(
          client.textRenderer, seconds, at.x() + size / 2, at.y() + inset + 4, READY_COLOUR);
    }
    if (cost > 0 && shade <= 0.0F) {
      final String shown = Integer.toString(cost);
      context.drawTextWithShadow(
          client.textRenderer,
          shown,
          at.x() + inset + PowerSlotView.ICON + 1 - client.textRenderer.getWidth(shown),
          at.y() + inset + PowerSlotView.ICON / 2 + 1,
          COST_COLOUR);
    }
  }

  private static void drawKey(
      final DrawContext context,
      final MinecraftClient client,
      final PowerBarLayout.Point at,
      final int slot) {
    final int inset = (PowerBarLayout.SLOT - PowerSlotView.ICON) / 2;
    final String key =
        PowerSlotView.keyLabel(
            SkillKeys.power(slot).getBoundKeyLocalizedText().getString(),
            SkillKeys.power(slot).isUnbound());
    context.getMatrices().push();
    context.getMatrices().translate(at.x() + inset - 1, at.y() + inset - 1, 0.0F);
    context.getMatrices().scale(KEY_SCALE, KEY_SCALE, 1.0F);
    context.drawTextWithShadow(client.textRenderer, key, 0, 0, KEY_COLOUR);
    context.getMatrices().pop();
  }
}
