package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.ammo.CrossbowAmmo;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.mob.brain.ActionReadiness;
import com.grahambartley.nightborn.mob.combat.MobFighting;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

final class HunterTactics<T extends MobEntity & GuildFighter> implements MobFighting.Moves {

  static final float BOLT_SPEED = 1.6F;
  static final float BOLT_SPREAD = 6.0F;
  static final float THROW_SPEED = 0.75F;
  static final float THROW_SPREAD = 8.0F;

  private final T hunter;
  private final HunterActionTimer timer = new HunterActionTimer();
  private final DrawnWeapon drawn = new DrawnWeapon();
  private boolean raised;

  HunterTactics(final T hunter) {
    this.hunter = hunter;
  }

  private void draw(final HunterKit kit) {
    final ItemStack weapon = stack(Optional.of(kit.weapon()));
    kit.bolt()
        .ifPresent(
            bolt ->
                weapon.set(
                    DataComponentTypes.CHARGED_PROJECTILES,
                    ChargedProjectilesComponent.of(stack(Optional.of(bolt)))));
    hunter.equipStack(EquipmentSlot.MAINHAND, weapon);
    hunter.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.0F);
    hunter.speak(HunterVoice.ALERT);
  }

  void sheathe() {
    drawn.sheathe();
    raised = false;
    hunter.setArmed(false);
    hunter.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    hunter.show(HunterAction.NONE);
  }

  void shoot() {
    if (timer.begin(HunterAction.CROSSBOW_SHOT)) {
      hunter.playSound(SoundEvents.ITEM_CROSSBOW_LOADING_END.value(), 1.0F, 1.0F);
    }
  }

  private void ready(final HunterKit kit, final boolean hunting) {
    final boolean armed = drawn.tick(hunting);
    if (armed == hunter.armed()) {
      return;
    }
    hunter.setArmed(armed);
    if (armed) {
      draw(kit);
    } else {
      hunter.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }
  }

  @Override
  public void tick() {
    final HunterKit kit = hunter.carriedKit();
    if (kit == null) {
      return;
    }
    final LivingEntity target = hunter.getTarget();
    final boolean hunting = target != null && target.isAlive();
    ready(kit, hunting);
    if (timer.tick() == HunterActionTimer.Step.RELEASE && hunting) {
      release(kit, target);
    }
    if (timer.busy()) {
      hunter.show(timer.action());
      return;
    }
    if (raised
        && !HunterSupport.holds(
            hunting,
            hunting && hunter.foeOf(target),
            hunting && hunter.getVisibilityCache().canSee(target),
            hunting ? hunter.distanceTo(target) : Double.MAX_VALUE,
            ServerConfigService.current().wards().crucifixRange())) {
      raised = false;
    }
    if (!raised) {
      hold(ItemStack.EMPTY);
    }
    hunter.show(raised ? HunterAction.RAISE_CRUCIFIX : HunterAction.NONE);
  }

  @Override
  public List<ActionReadiness> actions() {
    final HunterKit kit = hunter.carriedKit();
    final LivingEntity target = hunter.getTarget();
    if (kit == null || target == null || !target.isAlive()) {
      return List.of();
    }
    return HunterSupport.actions(
        kit.holyWater(),
        kit.crucifix().isPresent(),
        new HunterSupport.Moment(
            hunter.foeOf(target),
            hunter.distanceTo(target),
            timer.busy(),
            timer.canThrow(),
            raised),
        ServerConfigService.current().wards().crucifixRange());
  }

  @Override
  public boolean act(final Identifier action) {
    final HunterKit kit = hunter.carriedKit();
    if (kit == null) {
      return false;
    }
    if (HunterSupport.HOLY_WATER.equals(action)) {
      if (!timer.begin(HunterAction.THROW_HOLY_WATER)) {
        return false;
      }
      raised = false;
      hold(splash());
      hunter.show(timer.action());
      return true;
    }
    if (HunterSupport.CRUCIFIX.equals(action) && kit.crucifix().isPresent()) {
      raised = true;
      if (hold(stack(kit.crucifix()))) {
        hunter.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GOLD.value(), 1.0F, 1.0F);
      }
      hunter.show(HunterAction.RAISE_CRUCIFIX);
      return true;
    }
    return false;
  }

  private void release(final HunterKit kit, final LivingEntity target) {
    if (timer.action() == HunterAction.CROSSBOW_SHOT) {
      final ItemStack bolt = stack(kit.bolt());
      if (bolt.getItem() instanceof CrossbowAmmo ammo) {
        launch(
            ammo.createProjectile(
                hunter.getWorld(), hunter, bolt, hunter.getMainHandStack(), false),
            target,
            target.getBodyY(1.0 / 3.0),
            BOLT_SPEED,
            BOLT_SPREAD);
        hunter.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0F, 1.0F);
      }
      return;
    }
    final PotionEntity potion = new PotionEntity(hunter.getWorld(), hunter);
    potion.setItem(splash());
    launch(potion, target, target.getEyeY(), THROW_SPEED, THROW_SPREAD);
    hunter.playSound(SoundEvents.ENTITY_SPLASH_POTION_THROW, 1.0F, 1.0F);
    hold(ItemStack.EMPTY);
  }

  private void launch(
      final ProjectileEntity projectile,
      final LivingEntity target,
      final double aimY,
      final float speed,
      final float spread) {
    final double dx = target.getX() - hunter.getX();
    final double dz = target.getZ() - hunter.getZ();
    projectile.setVelocity(dx, Aim.rise(aimY, projectile.getY(), dx, dz), dz, speed, spread);
    hunter.getWorld().spawnEntity(projectile);
  }

  private boolean hold(final ItemStack stack) {
    if (ItemStack.areEqual(hunter.getOffHandStack(), stack)) {
      return false;
    }
    hunter.equipStack(EquipmentSlot.OFFHAND, stack);
    return true;
  }

  private static ItemStack splash() {
    return PotionContentsComponent.createStack(Items.SPLASH_POTION, HolyWater.POTION);
  }

  private static ItemStack stack(final Optional<Identifier> id) {
    return id.flatMap(GuildData::item).map(ItemStack::new).orElse(ItemStack.EMPTY);
  }
}
