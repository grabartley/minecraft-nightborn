package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.batform.BatShape;
import com.grahambartley.nightborn.batform.BatShifter;
import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.core.VampireCore;
import com.grahambartley.nightborn.decor.SeatEntity;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.house.MemberLife;
import com.grahambartley.nightborn.mob.combat.Fighting;
import com.grahambartley.nightborn.mob.goal.RetreatGoal;
import com.grahambartley.nightborn.mob.home.HomeLife;
import com.grahambartley.nightborn.mob.home.HouseholdTie;
import com.grahambartley.nightborn.mob.home.MobBlood;
import com.grahambartley.nightborn.mob.household.HouseholdBonds;
import com.grahambartley.nightborn.mob.household.HouseholdLife;
import com.grahambartley.nightborn.mob.household.SeatedHeight;
import com.grahambartley.nightborn.mob.look.BustNbt;
import com.grahambartley.nightborn.mob.look.Figure;
import com.grahambartley.nightborn.mob.look.RigWearer;
import com.grahambartley.nightborn.mob.look.VampireLook;
import com.grahambartley.nightborn.mob.look.VampireLooks;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.mob.look.VampireSize;
import com.grahambartley.nightborn.mob.look.VampireVoice;
import com.grahambartley.nightborn.mob.stage.Staged;
import com.grahambartley.nightborn.mob.stage.VampireStage;
import com.grahambartley.nightborn.mob.stage.VampireStages;
import com.grahambartley.nightborn.mob.stage.VampireStaging;
import com.grahambartley.nightborn.name.MobNaming;
import com.grahambartley.nightborn.name.NameBearer;
import com.grahambartley.nightborn.nemesis.Nemeses;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerShow;
import com.grahambartley.nightborn.skill.SkillHolder;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.structure.HouseholdArrival;
import com.grahambartley.nightborn.structure.HouseholdMember;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.weakened.Weakened;
import com.grahambartley.nightborn.wild.Wanderers;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VampireEntity extends HostileEntity
    implements BatShifter,
        Figure,
        GeoEntity,
        HouseholdMember,
        NameBearer,
        PowerShow,
        RigWearer,
        SkillHolder,
        Staged {

  static final String LOOK_KEY = "Look";
  static final String HEIGHT_KEY = "Height";
  static final String WIDTH_KEY = "Width";
  static final String STAGE_KEY = "Stage";
  static final String EPITHET_KEY = "Epithet";
  static final String MOURN_CLIP = "mourn";

  private static final TrackedData<String> LOOK =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<Float> HEIGHT =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.FLOAT);
  private static final TrackedData<Float> WIDTH =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.FLOAT);
  private static final TrackedData<String> STAGE =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<String> ACTION =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<Integer> ACTION_SEQUENCE =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Boolean> READING =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<Float> BUST =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.FLOAT);
  private static final TrackedData<String> EPITHET =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.STRING);

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final VampireAnimator animator = new VampireAnimator();
  private final BatShape batShape = new BatShape();
  private final HomeLife homeLife = new HomeLife();
  private final HouseholdTie householdTie = new HouseholdTie();
  private final HouseholdLife householdLife = new HouseholdLife();
  private final MobBlood blood = new MobBlood();
  private final Fighting fighting = new Fighting();
  private final VampireHunt hunt = new VampireHunt(blood, homeLife);
  private final VampireHiss hissing = new VampireHiss();
  private RetreatGoal retreat;
  private boolean undressed;

  public VampireEntity(final EntityType<? extends VampireEntity> type, final World world) {
    super(type, world);
    this.experiencePoints = 10;
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(LOOK, "");
    builder.add(HEIGHT, VampireSize.BASE_HEIGHT);
    builder.add(WIDTH, 1.0F);
    builder.add(STAGE, "");
    builder.add(ACTION, "");
    builder.add(ACTION_SEQUENCE, 0);
    builder.add(READING, false);
    builder.add(BUST, Figure.FLAT);
    builder.add(EPITHET, "");
  }

  @Override
  public void onTrackedDataSet(final TrackedData<?> data) {
    super.onTrackedDataSet(data);
    if (HEIGHT.equals(data) || WIDTH.equals(data)) {
      calculateDimensions();
    }
  }

  @Override
  public EntityDimensions getBaseDimensions(final EntityPose pose) {
    if (BatForm.isBat(this)) {
      return BatForm.dimensions(this);
    }
    return VampireSize.dimensions(super.getBaseDimensions(pose), height(), width());
  }

  @Override
  public EntityDimensions standingDimensions() {
    return VampireSize.dimensions(super.getBaseDimensions(EntityPose.STANDING), height(), width())
        .scaled(getScale());
  }

  @Override
  protected void initGoals() {
    retreat = VampireGoals.addTo(this, goalSelector, targetSelector);
  }

  public boolean hunts(final LivingEntity target) {
    return hunt.hunts(this, target);
  }

  public boolean attacksOnSight(final LivingEntity target) {
    return MemberLife.guards(this, target) || Nemeses.isQuarry(this, target);
  }

  public boolean hungry() {
    return blood.hungry(this, ServerConfigService.current()) || MemberLife.onErrand(this);
  }

  public HomeLife homeLife() {
    return homeLife;
  }

  public HouseholdTie householdTie() {
    return householdTie;
  }

  public HouseholdLife householdLife() {
    return householdLife;
  }

  public boolean reading() {
    return dataTracker.get(READING);
  }

  public void setReading(final boolean reading) {
    dataTracker.set(READING, reading);
  }

  public void lendHand(final ItemStack prop) {
    householdLife
        .handLoan()
        .lend(getEquippedStack(EquipmentSlot.MAINHAND), getDropChance(EquipmentSlot.MAINHAND));
    setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0F);
    equipStack(EquipmentSlot.MAINHAND, prop);
  }

  public void returnHand() {
    householdLife
        .handLoan()
        .take()
        .ifPresent(
            kept -> {
              equipStack(EquipmentSlot.MAINHAND, kept.stack());
              setEquipmentDropChance(EquipmentSlot.MAINHAND, kept.dropChance());
            });
  }

  public void speak(final VampireVoice voice) {
    playSound(MobSounds.vampire(rig(), voice), getSoundVolume(), getSoundPitch());
  }

  @Override
  public void settleAt(final BlockPos home) {
    homeLife.settle(home);
  }

  @Override
  public Optional<BlockPos> home() {
    return homeLife.home();
  }

  @Override
  public boolean tryAttack(final Entity target) {
    return hunt.bite(this, target);
  }

  @Override
  public void tick() {
    super.tick();
    if (batShape.changed(BatForm.isBat(this))) {
      calculateDimensions();
    }
    if (!getWorld().isClient() && isAlive()) {
      MobNaming.ensure(this);
      blood.tick(this, ServerConfigService.current());
      if (isSleeping() && !homeLife.bedtime(this) && !CoffinRise.rising(this)) {
        wakeUp();
      }
    }
  }

  @Override
  protected void mobTick() {
    super.mobTick();
    if (undressed && getWorld() instanceof ServerWorld world) {
      undressed = false;
      for (final EquipmentSlot slot : EquipmentSlot.values()) {
        if (slot.isArmorSlot()) {
          equipStack(slot, ItemStack.EMPTY);
        }
      }
      VampireDressing.dress(this, world.getRegistryManager());
    }
    tickHome();
    hissing.tickWard(this);
    if (!BatForm.isBat(this)) {
      fightAsVampire();
      if (levitatesAtWill()) {
        MobLevitation.drift(this);
      }
    }
  }

  public void tickHome() {
    hunt.tick(this, this::setOutToFeed);
    MemberLife.tick(this);
  }

  protected void fightAsVampire() {
    fighting.tick(
        this,
        Fighting.fights(
            getTarget() != null,
            Downed.state(this) != null,
            isSleeping(),
            retreat != null && retreat.active(),
            Weakened.sources(this).level(false) > 0),
        stage().combat(),
        ServerConfigService.current().combat());
  }

  protected boolean levitatesAtWill() {
    return true;
  }

  @Override
  public void showPower(final String clip) {
    showAction(clip);
  }

  public void showAction(final String clip) {
    dataTracker.set(ACTION, clip);
    dataTracker.set(ACTION_SEQUENCE, dataTracker.get(ACTION_SEQUENCE) + 1);
  }

  public String action() {
    return dataTracker.get(ACTION);
  }

  int actionSequence() {
    return dataTracker.get(ACTION_SEQUENCE);
  }

  public Identifier stageId() {
    return VampireStaging.parse(dataTracker.get(STAGE));
  }

  public void becomeStage(final Identifier id) {
    dataTracker.set(
        STAGE, VampireStaging.become(this, getWorld().getRegistryManager(), id, random).toString());
  }

  public VampireStage stage() {
    return VampireStages.stage(getWorld().getRegistryManager(), stageId())
        .orElse(VampireStage.PLAIN);
  }

  @Override
  public boolean bearsWomansName() {
    return rig() == VampireRig.FEMALE;
  }

  @Override
  public String epithet() {
    return dataTracker.get(EPITHET);
  }

  @Override
  public void bearEpithet(final String epithet) {
    dataTracker.set(EPITHET, epithet);
  }

  @Override
  public Optional<TextColor> nameColor() {
    return stage().look().nameColor();
  }

  @Override
  public SkillTree skillTree() {
    return SkillTree.VAMPIRE;
  }

  @Override
  public int stageRank() {
    return stage().rank();
  }

  @Override
  public RegistryKey<LootTable> getLootTableId() {
    return stage().loot().orElseGet(super::getLootTableId);
  }

  private void setOutToFeed() {
    if (getWorld() instanceof ServerWorld world) {
      BatEscort.gather(world, this, ServerConfigService.current().mobs().batEscortChance());
    }
  }

  @Override
  public void setTarget(@Nullable final LivingEntity target) {
    final boolean noticed = target != null && getTarget() == null;
    super.setTarget(target);
    if (noticed) {
      hiss();
    }
  }

  @Override
  public boolean canTarget(final LivingEntity target) {
    return fights(target) && !Wanderers.holdsBack(this) && super.canTarget(target);
  }

  protected boolean fights(final LivingEntity target) {
    return Nemeses.isQuarry(this, target)
        || !Vampires.isVampire(target)
        || VampireFeud.answers(
            target == getAttacker(), StageRanks.of(target), stageRank(), answersEveryVampire());
  }

  protected boolean answersEveryVampire() {
    return false;
  }

  @Override
  public boolean isAngryAt(final PlayerEntity player) {
    return VampireFeud.angryAt(Vampires.isVampire(player), getTarget() == player);
  }

  @Override
  public void setAttacker(@Nullable final LivingEntity attacker) {
    super.setAttacker(attacker);
    if (attacker != null) {
      hunt.provokedBy(attacker, fights(attacker));
    }
  }

  @Override
  public boolean damage(final DamageSource source, final float amount) {
    final boolean hurt = super.damage(source, amount);
    if (hurt && source.getAttacker() instanceof LivingEntity attacker) {
      hiss();
      HouseholdBonds.alert(
          this, attacker, ServerConfigService.current().mobs().vampireAlertRange());
    }
    return hurt;
  }

  @Override
  public void onDeath(final DamageSource source) {
    returnHand();
    super.onDeath(source);
    if (getWorld() instanceof ServerWorld world) {
      HouseholdBonds.mourn(world, this, ServerConfigService.current().mobs().vampireAlertRange());
    }
  }

  public void mourn(final ServerWorld world) {
    householdLife.grieve(
        world.getTime(),
        householdLife.temperament(world.getRegistryManager(), random::nextDouble).mourning());
    if (getTarget() == null && !isSleeping()) {
      showAction(MOURN_CLIP);
    }
  }

  @Override
  public boolean saveNbt(final NbtCompound nbt) {
    if (!(getVehicle() instanceof SeatEntity seat) || !saveSelfNbt(nbt)) {
      return super.saveNbt(nbt);
    }
    nbt.put("Pos", toNbtList(getX(), seat.getY(), getZ()));
    return true;
  }

  @Override
  public Vec3d getVehicleAttachmentPos(final Entity vehicle) {
    return vehicle instanceof SeatEntity
        ? new Vec3d(0.0, SeatedHeight.lift(height(), getScale()), 0.0)
        : super.getVehicleAttachmentPos(vehicle);
  }

  @Override
  public void remove(final RemovalReason reason) {
    if (reason.shouldDestroy() && getWorld() instanceof ServerWorld world) {
      MemberLife.ended(world, this);
    }
    super.remove(reason);
  }

  @Override
  protected boolean isImmobile() {
    return super.isImmobile() || isSleeping();
  }

  @Override
  public boolean cannotDespawn() {
    return homeLife.home().isPresent() || super.cannotDespawn();
  }

  @Override
  public boolean canImmediatelyDespawn(final double distanceSquared) {
    return homeLife.home().isEmpty() && super.canImmediatelyDespawn(distanceSquared);
  }

  @Override
  protected boolean isDisallowedInPeaceful() {
    return homeLife.home().isEmpty();
  }

  public void warnOff() {
    hiss();
  }

  void hiss() {
    hissing.hiss(this);
  }

  @Override
  public EntityData initialize(
      final ServerWorldAccess world,
      final LocalDifficulty difficulty,
      final SpawnReason spawnReason,
      @Nullable final EntityData entityData) {
    final NightbornConfig config = ServerConfigService.current();
    VampireDressing.dress(this, world.getRegistryManager());
    VampireCore.tick(this, config.core(), config.blood());
    setHealth(getMaxHealth());
    blood.start(this, random.nextInt(Integer.MAX_VALUE), config);
    if (entityData instanceof HouseholdArrival arrival && arrival.head()) {
      householdTie.becomeHead();
    }
    dataTracker.set(
        STAGE,
        VampireStaging.begin(
                this,
                world.getRegistryManager(),
                entityData instanceof HouseholdArrival arrival ? arrival.stages() : Map.of(),
                random)
            .toString());
    return super.initialize(world, difficulty, spawnReason, entityData);
  }

  @Override
  protected void initEquipment(final Random random, final LocalDifficulty difficulty) {}

  void shapeBust(final double roll) {
    dataTracker.set(BUST, rig().bust(roll));
  }

  public void setAppearance(final Identifier look, final float height, final float width) {
    dataTracker.set(LOOK, look.toString());
    dataTracker.set(HEIGHT, VampireSize.clampHeight(height));
    dataTracker.set(WIDTH, VampireSize.clampWidth(width));
  }

  public Identifier lookId() {
    final Identifier id = Identifier.tryParse(dataTracker.get(LOOK));
    return id == null ? VampireLooks.NONE : id;
  }

  public Optional<VampireLook> look() {
    return VampireLooks.look(getWorld().getRegistryManager(), lookId());
  }

  public VampireRig rig() {
    return look().map(VampireLook::rig).orElse(VampireRig.MALE);
  }

  public float height() {
    return dataTracker.get(HEIGHT);
  }

  public float width() {
    return dataTracker.get(WIDTH);
  }

  @Override
  public float bust() {
    return dataTracker.get(BUST);
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putString(LOOK_KEY, dataTracker.get(LOOK));
    nbt.putFloat(HEIGHT_KEY, height());
    nbt.putFloat(WIDTH_KEY, width());
    BustNbt.write(nbt, bust());
    nbt.putString(STAGE_KEY, dataTracker.get(STAGE));
    nbt.putString(EPITHET_KEY, epithet());
    homeLife.write(nbt);
    householdTie.write(nbt);
    householdLife.write(nbt, getRegistryManager());
    blood.write(nbt);
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    homeLife.read(nbt);
    householdTie.read(nbt);
    householdLife.read(nbt, getRegistryManager());
    returnHand();
    blood.read(nbt);
    dataTracker.set(STAGE, nbt.getString(STAGE_KEY));
    bearEpithet(nbt.getString(EPITHET_KEY));
    undressed = nbt.contains(LOOK_KEY) && !nbt.contains(LOOK_KEY, NbtElement.STRING_TYPE);
    if (nbt.contains(LOOK_KEY, NbtElement.STRING_TYPE)) {
      final Identifier look = Identifier.tryParse(nbt.getString(LOOK_KEY));
      setAppearance(
          look == null ? VampireLooks.NONE : look,
          nbt.contains(HEIGHT_KEY) ? nbt.getFloat(HEIGHT_KEY) : VampireSize.BASE_HEIGHT,
          nbt.contains(WIDTH_KEY) ? nbt.getFloat(WIDTH_KEY) : 1.0F);
      dataTracker.set(BUST, BustNbt.read(nbt, rig()));
    }
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return MobSounds.vampire(rig(), VampireVoice.AMBIENT);
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource source) {
    return MobSounds.vampire(rig(), VampireVoice.HURT);
  }

  @Override
  protected SoundEvent getDeathSound() {
    return MobSounds.vampire(rig(), VampireVoice.DEATH);
  }

  @Override
  public float getSoundPitch() {
    return super.getSoundPitch() * VampireVoice.pitch(height());
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    animator.register(this, controllers);
  }

  protected Optional<RawAnimation> poseClip(final VampirePose pose) {
    return Optional.empty();
  }

  public VampirePose shownPose() {
    return animator.shownPose();
  }

  public boolean holdsHead() {
    return animator.holdsHead();
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
