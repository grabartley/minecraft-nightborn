package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.MobSettings;
import com.grahambartley.nightborn.hunter.FightRoll;
import com.grahambartley.nightborn.hunter.SharedHunt;
import com.grahambartley.nightborn.lord.BossCrowd;
import com.grahambartley.nightborn.mob.goal.HouseholdLifeGoal;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Modifiers;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EldestEntity extends VampireBossEntity implements SharedHunt {

  public static final Identifier STAGE = Identifier.of(Nightborn.MOD_ID, "eldest");
  public static final int ROOM_RADIUS = 6;
  static final Identifier CROWD_HEALTH = Identifier.of(Nightborn.MOD_ID, "boss/crowd");
  static final int CROWD_CHECK_TICKS = 20;
  static final String CROWD_KEY = "Crowd";

  private final FightRoll roll = new FightRoll();
  private int crowd = 1;

  public EldestEntity(final EntityType<? extends EldestEntity> type, final World world) {
    super(type, world);
    this.experiencePoints = 200;
  }

  @Override
  protected Identifier ownStage() {
    return STAGE;
  }

  @Override
  public Set<VampireRig> rigs() {
    return Set.of(VampireRig.eitherCommon(getRandom().nextBoolean()));
  }

  @Override
  protected void initGoals() {
    super.initGoals();
    goalSelector.clear(goal -> goal instanceof HouseholdLifeGoal);
  }

  @Override
  public void settleAt(final BlockPos home) {
    super.settleAt(home);
    homeLife().keepTo(getBlockPos(), ROOM_RADIUS);
  }

  @Override
  public List<UUID> tookPart() {
    return roll.fighters();
  }

  @Override
  public boolean damage(final DamageSource source, final float amount) {
    final boolean hurt = super.damage(source, amount);
    if (hurt && source.getAttacker() instanceof PlayerEntity player) {
      roll.join(player.getUuid());
    }
    return hurt;
  }

  @Override
  protected void mobTick() {
    if (age % CROWD_CHECK_TICKS == 0) {
      gauge(ServerConfigService.current().mobs());
    }
    super.mobTick();
  }

  public void gauge(final MobSettings settings) {
    final int range = settings.eldestCrowdRange();
    final int near =
        getWorld()
            .getEntitiesByClass(
                PlayerEntity.class,
                getBoundingBox().expand(range),
                player -> player.isAlive() && !player.isSpectator())
            .size();
    crowd = BossCrowd.size(crowd, near, getTarget() != null);
    final float before = getMaxHealth();
    Modifiers.set(
        this,
        EntityAttributes.GENERIC_MAX_HEALTH,
        CROWD_HEALTH,
        BossCrowd.bonus(crowd, settings.eldestHealthPerPlayer()),
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    setHealth(BossCrowd.healed(getHealth(), before, getMaxHealth()));
    Modifiers.clampHealth(this);
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    roll.write(nbt);
    nbt.putInt(CROWD_KEY, crowd);
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    roll.read(nbt);
    crowd = Math.max(1, nbt.getInt(CROWD_KEY));
  }
}
