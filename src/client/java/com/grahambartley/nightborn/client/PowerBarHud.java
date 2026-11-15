package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.skill.PowerBar;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerCooldowns;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillPower;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.registry.Registry;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

public final class PowerBarHud {

  static final Identifier SLOT = Identifier.of(Nightborn.MOD_ID, "hud/power_slot");
  static final int ICON = 16;
  static final int COOLDOWN_SHADE = 0xB0000000;
  static final int COST_COLOUR = 0xE04040;
  static final int KEY_COLOUR = 0xC8B8A0;
  static final int READY_COLOUR = 0xFFFFFF;
  static final int TICKS_PER_SECOND = 20;

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
    if (skills == null) {
      return;
    }
    final List<Identifier> bar =
        PowerBar.slots(
            SkillAttachment.get(player, SkillTree.of(FactionAttachment.get(player))),
            id -> skills.get(id) != null && skills.get(id).active());
    if (bar.isEmpty()) {
      return;
    }
    final boolean offhandOnRight =
        player.getMainArm() == Arm.LEFT && !player.getOffHandStack().isEmpty();
    final List<PowerBarLayout.Point> points =
        PowerBarLayout.slots(
            context.getScaledWindowWidth(),
            context.getScaledWindowHeight(),
            bar.size(),
            offhandOnRight);
    final PowerCooldowns cooldowns = PowerCooldownAttachment.get(player);
    final long now = client.world.getTime();
    final int blood = BloodAttachment.get(player);
    for (int i = 0; i < bar.size(); i++) {
      final Skill skill = skills.get(bar.get(i));
      final SkillPower power = skill.power().orElseThrow();
      final PowerBarLayout.Point at = points.get(i);
      drawSlot(context, client, at, skill, power, cooldowns.ticksLeft(bar.get(i), now), blood, i);
    }
  }

  private static void drawSlot(
      final DrawContext context,
      final MinecraftClient client,
      final PowerBarLayout.Point at,
      final Skill skill,
      final SkillPower power,
      final long ticksLeft,
      final int blood,
      final int slot) {
    final int size = PowerBarLayout.SLOT;
    context.drawGuiTexture(SLOT, at.x(), at.y(), size, size);
    final int inset = (size - ICON) / 2;
    final boolean thirsty = blood < power.blood();
    if (thirsty) {
      context.setShaderColor(0.5F, 0.3F, 0.3F, 1.0F);
    }
    context.drawGuiTexture(skill.icon(), at.x() + inset, at.y() + inset, ICON, ICON);
    context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    final float shade = PowerCooldowns.fraction(ticksLeft, power.cooldownTicks());
    if (shade > 0.0F) {
      final int top = at.y() + inset + Math.round(ICON * (1.0F - shade));
      context.fill(
          at.x() + inset, top, at.x() + inset + ICON, at.y() + inset + ICON, COOLDOWN_SHADE);
      final String seconds = Long.toString((ticksLeft + TICKS_PER_SECOND - 1) / TICKS_PER_SECOND);
      context.drawCenteredTextWithShadow(
          client.textRenderer, seconds, at.x() + size / 2, at.y() + inset + 4, READY_COLOUR);
    }
    if (power.blood() > 0) {
      final String cost = Integer.toString(power.blood());
      context.drawTextWithShadow(
          client.textRenderer,
          cost,
          at.x() + size - 1 - client.textRenderer.getWidth(cost),
          at.y() + size - 8,
          COST_COLOUR);
    }
    final String key = SkillKeys.power(slot).getBoundKeyLocalizedText().getString();
    if (key.length() <= 2) {
      context.drawTextWithShadow(client.textRenderer, key, at.x() + 2, at.y() + 2, KEY_COLOUR);
    }
  }
}
