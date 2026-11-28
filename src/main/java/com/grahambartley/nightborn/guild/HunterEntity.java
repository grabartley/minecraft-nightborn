package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.boss.BossWard;
import com.grahambartley.nightborn.boss.SwornAlly;
import com.grahambartley.nightborn.guild.build.GrowingHunter;
import com.grahambartley.nightborn.guild.build.HunterBuildTicker;
import com.grahambartley.nightborn.guild.build.HunterSkills;
import com.grahambartley.nightborn.guild.party.PartyMember;
import com.grahambartley.nightborn.guild.raiding.RaidConduct;
import com.grahambartley.nightborn.guild.raiding.RaidDoorGoal;
import com.grahambartley.nightborn.guild.raiding.RaidOrders;
import com.grahambartley.nightborn.mob.MobSounds;
import com.grahambartley.nightborn.mob.ShownAction;
import com.grahambartley.nightborn.mob.combat.MobFighter;
import com.grahambartley.nightborn.mob.combat.MobFighting;
import com.grahambartley.nightborn.mob.combat.PowerCasting;
import com.grahambartley.nightborn.mob.look.BustNbt;
import com.grahambartley.nightborn.mob.look.Figure;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.name.MobNaming;
import com.grahambartley.nightborn.name.NameBearer;
import com.grahambartley.nightborn.rank.RankBearer;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.skill.SkillHolder;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.wheel.CommandWheel;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HunterEntity extends PathAwareEntity
    implements GeoEntity,
        MobFighter,
        HunterLooking,
        NameBearer,
        RangedAttackMob,
        SwornAlly,
        SkillHolder,
        PowerShow,
        RankBearer,
        GrowingHunter,
        GuildFighter {

  static final String FEMALE_KEY = "Female";
  static final String KIT_KEY = "Kit";
  public static final int ANIMATION_BLEND_TICKS = 4;
  static final int CAMP_REACH = 12;
  static final String PARTY_LEADER_KEY = "PartyLeader";
  static final String RAID_KEY = "Raid";
  @Nullable private UUID partyLeader;
  @Nullable private RaidOrders raid;
  private final Set<UUID> sworn = new HashSet<>();

  private static final TrackedData<Boolean> FEMALE =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<Float> BUST =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.FLOAT);
  private static final TrackedData<String> LOOK =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<Integer> ACTION =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Boolean> ARMED =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<String> RANK =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<String> POWER =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<Integer> POWER_SEQUENCE =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.INTEGER);

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final HunterTactics<HunterEntity> tactics = new HunterTactics<>(this);
  private final MobFighting fighting =
      MobFighting.movingFirst(PowerCasting.underTell(), this::tickTactics);
  private final WornLook worn = new WornLook();
  private final ShownAction shownPower = new ShownAction();
  private final HunterBuildTicker building = new HunterBuildTicker();
  private String kitId = "";
  private HunterKit kit;
  private final ShownHunterAction shown = new ShownHunterAction();
  private final StallWatch stall = new StallWatch();
  private WanderAroundFarGoal wander;
  private final HunterWares wares = new HunterWares(this);

  public HunterEntity(final EntityType<? extends PathAwareEntity> type, final World world) {
    super(type, world);
    setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0F);
    setEquipmentDropChance(EquipmentSlot.OFFHAND, 0.0F);
  }

  public static DefaultAttributeContainer.Builder createHunterAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
        .add(EntityAttributes.GENERIC_ARMOR, 2.0)
        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(FEMALE, false);
    builder.add(BUST, Figure.FLAT);
    builder.add(LOOK, "");
    builder.add(ACTION, HunterAction.NONE.ordinal());
    builder.add(ARMED, false);
    builder.add(RANK, "");
    builder.add(POWER, "");
    builder.add(POWER_SEQUENCE, 0);
  }

  @Override
  protected void initGoals() {
    goalSelector.add(0, new SwimGoal(this));
    goalSelector.add(1, new CrossbowGoal(this, this::standsOff));
    goalSelector.add(2, new MeleeAttackGoal(this, 1.15, true));
    goalSelector.add(3, new RaidDoorGoal(this, () -> raid != null));
    goalSelector.add(4, new GoToWalkTargetGoal(this, 0.8));
    wander = new WanderAroundFarGoal(this, 0.6);
    goalSelector.add(5, wander);
    goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
    goalSelector.add(7, new LookAroundGoal(this));
    targetSelector.add(0, new KnownFaceTargetGoal(this));
    targetSelector.add(1, new HunterTargetGoal(this));
    targetSelector.add(2, new GuildRevengeGoal(this));
  }

  @Override
  public boolean canMoveVoluntarily() {
    return !isSleeping() && super.canMoveVoluntarily();
  }

  @Override
  public boolean canTarget(final LivingEntity target) {
    return GuildFoes.fights(this, target) && super.canTarget(target);
  }

  @Override
  public boolean foeOf(final LivingEntity target) {
    return GuildFoes.hunts(target) || sworn.contains(target.getUuid());
  }

  @Override
  public Optional<GuildMasterEntity> master() {
    return partyLeader != null && getWorld() instanceof ServerWorld world
        ? Optional.ofNullable(world.getEntity(partyLeader))
            .filter(GuildMasterEntity.class::isInstance)
            .map(GuildMasterEntity.class::cast)
            .filter(GuildMasterEntity::isAlive)
        : Optional.empty();
  }

  @Override
  public void swear(final UUID foe) {
    sworn.add(foe);
  }

  @Override
  protected boolean shouldDropLoot() {
    return !BossWard.keeper(this) && super.shouldDropLoot();
  }

  @Override
  public boolean shouldDropXp() {
    return !BossWard.keeper(this) && super.shouldDropXp();
  }

  @Override
  public HunterRig rig() {
    return HunterRig.hunter(dataTracker.get(FEMALE));
  }

  @Override
  public Identifier texture() {
    return worn.texture(
        rig(), dataTracker.get(LOOK), look -> GuildData.texture(getRegistryManager(), look, rig()));
  }

  @Override
  public void shootAt(final LivingEntity target, final float pullProgress) {
    tactics.shoot();
  }

  public String look() {
    return dataTracker.get(LOOK);
  }

  public String kit() {
    return kitId;
  }

  @Override
  @Nullable
  public HunterKit carriedKit() {
    return kit;
  }

  public boolean standsOff() {
    return kit != null && kit.ranged();
  }

  @Override
  public boolean armed() {
    return dataTracker.get(ARMED);
  }

  @Override
  public void setArmed(final boolean armed) {
    dataTracker.set(ARMED, armed);
  }

  @Override
  public void speak(final HunterVoice voice) {
    playSound(MobSounds.hunter(figure(), voice), getSoundVolume(), getSoundPitch());
  }

  @Override
  public boolean tryAttack(final Entity target) {
    return HunterCries.strike(this, super.tryAttack(target));
  }

  @Override
  public boolean onKilledOther(final ServerWorld world, final LivingEntity other) {
    HunterCries.killed(this, other);
    return super.onKilledOther(world, other);
  }

  @Override
  public void show(final HunterAction action) {
    dataTracker.set(ACTION, action.ordinal());
  }

  public HunterAction action() {
    return HunterAction.byIndex(dataTracker.get(ACTION));
  }

  @Override
  public boolean bearsWomansName() {
    return dataTracker.get(FEMALE);
  }

  @Override
  public float bust() {
    return dataTracker.get(BUST);
  }

  @Override
  public void tick() {
    super.tick();
    if (getWorld().isClient()) {
      return;
    }
    if (!hasPositionTarget()) {
      settle();
    }
    if (grows() && (building.tick(this, fixedRank(), heldKit()) || shownRank().isEmpty())) {
      showRank();
    }
    MobNaming.ensure(this);
    if (PartyMember.wakes(isSleeping(), getWorld().getTimeOfDay())) {
      wakeUp();
    }
    if (raid != null && !RaidConduct.stays(this, raid)) {
      if (partyLeader != null) {
        standDown();
      } else {
        discard();
      }
    }
  }

  public void enlist(final RaidOrders orders, final String wantedKit) {
    if (!hasPositionTarget()) {
      settle();
    }
    arm(wantedKit);
    serve(orders);
    orders.place().ifPresent(place -> setPositionTarget(place, RaidConduct.PLACE_REACH));
  }

  public void rollFrom(final Map<Identifier, Integer> odds) {
    building.rollFrom(odds);
  }

  @Override
  public boolean grows() {
    return true;
  }

  protected Optional<Identifier> fixedRank() {
    return Optional.empty();
  }

  @Override
  public void earn(final float experience) {
    HunterSkills.earn(this, experience, heldKit());
    showRank();
  }

  private Optional<Identifier> heldKit() {
    return Optional.ofNullable(Identifier.tryParse(kitId));
  }

  private void showRank() {
    dataTracker.set(RANK, RankBearer.write(HunterSkills.rank(this)));
  }

  @Override
  public Optional<Identifier> shownRank() {
    return RankBearer.read(dataTracker.get(RANK));
  }

  protected boolean telling() {
    return fighting.telling();
  }

  @Override
  public SkillTree skillTree() {
    return SkillTree.HUNTER;
  }

  @Override
  public void showPower(final String clip) {
    dataTracker.set(POWER, clip);
    dataTracker.set(POWER_SEQUENCE, dataTracker.get(POWER_SEQUENCE) + 1);
  }

  public void standDown() {
    raid = null;
    if (getNavigation() instanceof MobNavigation navigation) {
      navigation.setCanPathThroughDoors(false);
    }
  }

  public Optional<RaidOrders> raid() {
    return Optional.ofNullable(raid);
  }

  private void serve(final RaidOrders orders) {
    raid = orders;
    if (getNavigation() instanceof MobNavigation navigation) {
      navigation.setCanPathThroughDoors(true);
    }
  }

  @Override
  public ActionResult interactMob(final PlayerEntity player, final Hand hand) {
    return HunterReferral.answers(this, player)
        ? CommandWheel.use(player, hand, this)
        : super.interactMob(player, hand);
  }

  public HunterWares wares() {
    return wares;
  }

  @Override
  public void onDeath(final DamageSource source) {
    wares.close();
    super.onDeath(source);
  }

  public void join(final UUID leader) {
    partyLeader = leader;
  }

  public void leaveParty() {
    partyLeader = null;
  }

  public Optional<UUID> partyLeader() {
    return Optional.ofNullable(partyLeader);
  }

  @Override
  protected void mobTick() {
    super.mobTick();
    fight();
    stall.watch(this, wander);
    wares.tick();
  }

  @Override
  public float getPathfindingFavor(final BlockPos pos, final WorldView world) {
    return Footing.favor(this, pos, world);
  }

  public void fight() {
    fighting.tick(
        this,
        MobFighting.Engagement.FIGHTING,
        HunterSkills.thinkTicks(this, shownRank()),
        MobFighting.NO_BLOOD_FLOOR);
  }

  private void tickTactics() {
    if (kit != null) {
      tactics.tick(kit);
    }
  }

  @Override
  public MobFighting mobFighting() {
    return fighting;
  }

  protected void settle() {
    CampHome.keep(this, CAMP_REACH);
    dataTracker.set(FEMALE, getRandom().nextBoolean());
    dataTracker.set(BUST, figure().bust(getRandom().nextDouble()));
    wear("");
    arm("");
  }

  private void wear(final String look) {
    dataTracker.set(LOOK, GuildData.wornLook(look, getRegistryManager(), rig(), getRandom()));
  }

  public void arm(final String wanted) {
    final var kits = GuildData.kits(getRegistryManager());
    final String picked =
        Optional.ofNullable(Identifier.tryParse(wanted))
            .filter(kits::containsKey)
            .or(() -> HunterKit.pick(kits, getRandom().nextDouble()))
            .map(Identifier::toString)
            .orElse("");
    carry(picked, kits.get(Identifier.tryParse(picked)));
  }

  protected void carry(final String id, @Nullable final HunterKit carried) {
    kitId = id;
    kit = carried;
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return MobSounds.hunter(figure(), HunterVoice.AMBIENT);
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource source) {
    return MobSounds.hunter(figure(), HunterVoice.HURT);
  }

  @Override
  protected SoundEvent getDeathSound() {
    return MobSounds.hunter(figure(), HunterVoice.DEATH);
  }

  private VampireRig figure() {
    return rig().figure();
  }

  @Override
  public boolean canBeLeashed() {
    return false;
  }

  @Override
  public boolean cannotDespawn() {
    return raid != null || PartyMember.staysInWorld(partyLeader != null, leaderNear());
  }

  private boolean leaderNear() {
    if (partyLeader == null || !(getWorld() instanceof ServerWorld world)) {
      return false;
    }
    final Entity leader = world.getEntity(partyLeader);
    return leader != null
        && leader.isAlive()
        && leader.squaredDistanceTo(this) < PartyMember.LEADER_NEAR * PartyMember.LEADER_NEAR;
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putBoolean(FEMALE_KEY, dataTracker.get(FEMALE));
    BustNbt.write(nbt, bust());
    nbt.putString(GuildData.LOOK_KEY, dataTracker.get(LOOK));
    nbt.putString(KIT_KEY, kitId);
    CampHome.write(this, nbt);
    if (partyLeader != null) {
      nbt.putUuid(PARTY_LEADER_KEY, partyLeader);
    }
    if (raid != null) {
      RaidOrders.CODEC
          .encodeStart(NbtOps.INSTANCE, raid)
          .ifSuccess(orders -> nbt.put(RAID_KEY, orders));
    }
    wares.write(nbt, getRegistryManager().getOps(NbtOps.INSTANCE));
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    dataTracker.set(FEMALE, nbt.getBoolean(FEMALE_KEY));
    dataTracker.set(BUST, BustNbt.read(nbt, figure()));
    if (!getWorld().isClient()) {
      equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      wear(nbt.getString(GuildData.LOOK_KEY));
      arm(nbt.getString(KIT_KEY));
    }
    CampHome.read(this, nbt, CAMP_REACH);
    partyLeader = nbt.containsUuid(PARTY_LEADER_KEY) ? nbt.getUuid(PARTY_LEADER_KEY) : null;
    if (nbt.contains(RAID_KEY)) {
      RaidOrders.CODEC.parse(NbtOps.INSTANCE, nbt.get(RAID_KEY)).result().ifPresent(this::serve);
    }
    wares.read(nbt, getRegistryManager().getOps(NbtOps.INSTANCE));
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "pose",
            ANIMATION_BLEND_TICKS,
            state ->
                state.setAndContinue(
                    HunterPose.of(
                            handSwinging,
                            false,
                            isSprinting() || isAttacking(),
                            state.getLimbSwingAmount(),
                            armed())
                        .clip())));
    controllers.add(
        new AnimationController<>(
            this, "action", ANIMATION_BLEND_TICKS, state -> shown.play(state, action())));
    controllers.add(
        new AnimationController<>(
            this,
            "power",
            ANIMATION_BLEND_TICKS,
            state ->
                shownPower.play(state, dataTracker.get(POWER_SEQUENCE), dataTracker.get(POWER))));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
