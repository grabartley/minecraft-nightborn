package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.boss.Arena;
import com.grahambartley.nightborn.boss.ArenaBoss;
import com.grahambartley.nightborn.boss.BossBars;
import com.grahambartley.nightborn.boss.BossFight;
import com.grahambartley.nightborn.boss.BossFightPlan;
import com.grahambartley.nightborn.boss.BossFights;
import com.grahambartley.nightborn.boss.BossHealth;
import com.grahambartley.nightborn.boss.FightRoll;
import com.grahambartley.nightborn.boss.FightRules;
import com.grahambartley.nightborn.boss.Fought;
import com.grahambartley.nightborn.boss.HoldGroundGoal;
import com.grahambartley.nightborn.boss.ShownForm;
import com.grahambartley.nightborn.boss.StanceGoal;
import com.grahambartley.nightborn.config.InquisitorSettings;
import com.grahambartley.nightborn.core.IdText;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.feeding.BiteQuarry;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.guild.HunterRig;
import com.grahambartley.nightborn.hunt.HuntAttachment;
import com.grahambartley.nightborn.lord.BossPresence;
import com.grahambartley.nightborn.mob.ClipCache;
import com.grahambartley.nightborn.mob.ShownAction;
import com.grahambartley.nightborn.mob.goal.LieDownedGoal;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;

public class GrandInquisitorEntity extends HunterEntity implements ArenaBoss, BiteQuarry, Fought {

  public static final Identifier FIGHT = Identifier.of(Nightborn.MOD_ID, "grand_inquisitor");
  public static final TagKey<DamageType> SHRUGS_OFF =
      TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, "inquisitor_shrugs_off"));
  public static final Identifier GEO =
      Identifier.of(Nightborn.MOD_ID, "geo/entity/grand_inquisitor.geo.json");
  public static final Identifier ANIMATION =
      Identifier.of(Nightborn.MOD_ID, "animations/entity/grand_inquisitor.animation.json");
  public static final Identifier SKIN =
      Identifier.of(Nightborn.MOD_ID, "textures/entity/grand_inquisitor/hunt.png");

  static final Identifier CROWD = Identifier.of(Nightborn.MOD_ID, "inquisitor/crowd");
  static final double SEAL_REACH = 12.0;
  static final double CROWD_REACH = 32.0;
  static final int TICKS_PER_SECOND = 20;
  static final int BLEND_TICKS = 6;
  static final String CHAT_LINE = "chat.type.text";
  static final String EMPTY_SINCE_KEY = "EmptySince";
  static final long NEVER = Long.MIN_VALUE;

  private static final TrackedData<String> FORM =
      DataTracker.registerData(GrandInquisitorEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<String> SKIN_SHOWN =
      DataTracker.registerData(GrandInquisitorEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<String> CLIP =
      DataTracker.registerData(GrandInquisitorEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<Integer> CLIP_SEQUENCE =
      DataTracker.registerData(GrandInquisitorEntity.class, TrackedDataHandlerRegistry.INTEGER);

  private final BossPresence presence;
  private final BossFight fight = new BossFight();
  private final FightRoll roll = new FightRoll();
  private final InquisitorRoster roster = new InquisitorRoster();
  private final ShownAction shownAction = new ShownAction();
  @Nullable private InquisitorQuarry quarry;
  private long emptySince = NEVER;
  private boolean leaving;

  public GrandInquisitorEntity(
      final EntityType<? extends GrandInquisitorEntity> type, final World world) {
    super(type, world);
    this.presence = new BossPresence(getDisplayName());
    this.experiencePoints = 50;
  }

  public static DefaultAttributeContainer.Builder createInquisitorAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
        .add(EntityAttributes.GENERIC_ARMOR, 4.0)
        .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5)
        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(FORM, "");
    builder.add(SKIN_SHOWN, "");
    builder.add(CLIP, "");
    builder.add(CLIP_SEQUENCE, 0);
  }

  @Override
  protected void initGoals() {
    goalSelector.add(0, new LieDownedGoal(this));
    goalSelector.add(0, new HoldGroundGoal(this, () -> fight.busy(getWorld().getTime())));
    goalSelector.add(1, new SwimGoal(this));
    goalSelector.add(2, new StakeDownedGoal(this));
    goalSelector.add(4, new StanceGoal(this, () -> fight.stance(plan())));
    goalSelector.add(5, new GoToWalkTargetGoal(this, 1.0));
    goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
    goalSelector.add(9, new LookAroundGoal(this));
    targetSelector.add(1, new RevengeGoal(this));
    targetSelector.add(
        2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::attacksOnSight));
    targetSelector.add(
        3,
        new ActiveTargetGoal<>(
            this,
            LivingEntity.class,
            10,
            true,
            false,
            target -> !(target instanceof PlayerEntity) && Vampires.isVampire(target)));
  }

  public void hunt(
      final ServerPlayerEntity player, final boolean bell, final List<ServerPlayerEntity> near) {
    quarry = InquisitorQuarry.of(player.getUuid(), bell, Vampires.isVampire(player));
    setTarget(player);
    if (quarry.turnedOn().isPresent()) {
      plan()
          .flatMap(p -> p.summonerLine(getRandom().nextInt(Integer.MAX_VALUE)))
          .ifPresent(
              line -> {
                final Text said =
                    Text.translatable(CHAT_LINE, getDisplayName(), Text.translatable(line));
                near.forEach(listener -> listener.sendMessage(said, false));
              });
    }
  }

  public Optional<InquisitorQuarry> quarry() {
    return Optional.ofNullable(quarry);
  }

  public BossFight bossFight() {
    return fight;
  }

  @Override
  public Set<UUID> fighters() {
    return roll.fighters();
  }

  public boolean fellIn(final ServerPlayerEntity player) {
    return fight.arena().isPresent() && roster.met(player.getUuid());
  }

  public void fallen(final ServerPlayerEntity player) {
    roster.fell(player.getUuid());
  }

  public boolean leaving() {
    return leaving;
  }

  public Optional<BossFightPlan> plan() {
    return BossFights.fight(getWorld().getRegistryManager(), FIGHT);
  }

  public ShownForm shownForm() {
    return ShownForm.decode(dataTracker.get(FORM));
  }

  public String clip() {
    return dataTracker.get(CLIP);
  }

  int clipSequence() {
    return dataTracker.get(CLIP_SEQUENCE);
  }

  @Override
  public BossPresence presence() {
    return presence;
  }

  @Override
  public void showPower(final String clip) {
    dataTracker.set(CLIP, clip);
    dataTracker.set(CLIP_SEQUENCE, dataTracker.get(CLIP_SEQUENCE) + 1);
  }

  @Override
  public Set<UUID> sworn() {
    return quarry == null ? Set.of() : quarry.turnedOn().map(Set::of).orElse(Set.of());
  }

  @Override
  public boolean attacksOnSight(final LivingEntity target) {
    return InquisitorFoes.seeksOut(
        target instanceof PlayerEntity,
        Vampires.isVampire(target),
        quarry != null && quarry.turnedOn(target.getUuid()),
        getWorld().getDifficulty() == Difficulty.PEACEFUL);
  }

  @Override
  protected boolean foeOf(final LivingEntity target) {
    return InquisitorFoes.fights(
        target instanceof PlayerEntity,
        Vampires.isVampire(target),
        quarry != null && quarry.turnedOn(target.getUuid()),
        roll.has(target.getUuid()));
  }

  @Override
  public boolean canTarget(final LivingEntity target) {
    return fight.holds(target.getPos()) && super.canTarget(target);
  }

  @Override
  public HunterRig rig() {
    return HunterRig.MALE;
  }

  @Override
  public Identifier geo() {
    return GEO;
  }

  @Override
  public Identifier animation() {
    return ANIMATION;
  }

  @Override
  public Identifier texture() {
    return IdText.parse(dataTracker.get(SKIN_SHOWN)).orElse(SKIN);
  }

  @Override
  protected void settle() {}

  @Override
  public void fight() {}

  @Override
  public boolean bitable() {
    final DownedState downed = Downed.state(this);
    return downed != null && downed.isDowned();
  }

  @Override
  public boolean struggles() {
    return false;
  }

  @Override
  public boolean finishable() {
    return false;
  }

  @Override
  protected void mobTick() {
    super.mobTick();
    tickFight();
  }

  public void tickFight() {
    if (!(getWorld() instanceof ServerWorld world)) {
      return;
    }
    final InquisitorSettings settings = ServerConfigService.current().inquisitor();
    if (fight.arena().isEmpty()) {
      seal(world, settings);
    }
    Downed.tick(this, ServerConfigService.current().downed());
    final Optional<BossFightPlan> plan = plan();
    final long now = world.getTime();
    dataTracker.set(FORM, fight.shown(plan, Downed.state(this) != null, now).encode());
    dataTracker.set(
        SKIN_SHOWN, IdText.write(plan.flatMap(p -> p.phase(fight.phase()).look().skin())));
    fight.tick(
        this,
        plan,
        new FightRules(0, settings.drainWindowSeconds() * TICKS_PER_SECOND, settings.riseHealth()),
        () -> {});
    if (forgotten(world)) {
      depart();
      return;
    }
    watch(world, settings, now);
    BossBars.show(this, fight, plan);
  }

  private void seal(final ServerWorld world, final InquisitorSettings settings) {
    final ServerPlayerEntity hunted = quarryPlayer(world);
    if (hunted == null || squaredDistanceTo(hunted) > SEAL_REACH * SEAL_REACH) {
      return;
    }
    fight.claim(new Arena(hunted.getBlockPos(), settings.arenaRadius()));
    playSound(SoundEvents.BLOCK_BELL_RESONATE, 4.0F, 0.6F);
  }

  private void watch(final ServerWorld world, final InquisitorSettings settings, final long now) {
    if (leaving) {
      return;
    }
    final List<ServerPlayerEntity> inside = foesInside(world);
    inside.forEach(player -> roster.meet(player.getUuid()));
    if (!inside.isEmpty()
        || quarryPlayer(world) != null && fight.arena().isEmpty()
        || emptySince == NEVER) {
      emptySince = now;
    }
    final boolean sealed = fight.arena().isPresent();
    final InquisitorEnding ending =
        InquisitorEnding.of(
            sealed ? inside.size() : (quarryPlayer(world) == null ? 0 : 1),
            sealed ? roster.standing() : 1,
            now - emptySince,
            (long) settings.emptyArenaSeconds() * TICKS_PER_SECOND);
    if (ending != InquisitorEnding.NONE && Downed.state(this) == null) {
      InquisitorOutcomes.leave(this, world, ending);
    }
  }

  public void scaleToCrowd(final ServerWorld world, final InquisitorSettings settings) {
    BossHealth.scale(
        this,
        CROWD,
        world
            .getPlayers(
                player ->
                    player.isAlive()
                        && !player.isSpectator()
                        && !player.isCreative()
                        && Vampires.isVampire(player)
                        && player.squaredDistanceTo(this) <= CROWD_REACH * CROWD_REACH)
            .size(),
        settings.healthPerVampire());
  }

  private boolean forgotten(final ServerWorld world) {
    if (quarry == null || leaving) {
      return false;
    }
    final PlayerEntity player = world.getServer().getPlayerManager().getPlayer(quarry.player());
    return player != null
        && !HuntAttachment.get(player).visitor().filter(getUuid()::equals).isPresent();
  }

  public Optional<String> givenName() {
    return Optional.ofNullable(getCustomName()).map(Text::getString);
  }

  private List<ServerPlayerEntity> foesInside(final ServerWorld world) {
    return fight
        .arena()
        .map(
            arena ->
                world.getPlayers(
                    player ->
                        player.isAlive()
                            && !player.isSpectator()
                            && !player.isCreative()
                            && attacksOnSight(player)
                            && arena.holds(player.getX(), player.getY(), player.getZ())))
        .orElse(List.of());
  }

  @Nullable
  private ServerPlayerEntity quarryPlayer(final ServerWorld world) {
    if (quarry == null) {
      return null;
    }
    final PlayerEntity player = world.getPlayerByUuid(quarry.player());
    return player instanceof ServerPlayerEntity hunted && hunted.isAlive() ? hunted : null;
  }

  public void depart() {
    leaving = true;
    discard();
  }

  @Override
  public boolean damage(final DamageSource source, final float amount) {
    if (getWorld().isClient()) {
      return super.damage(source, amount);
    }
    if (bitable() && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      return downedBlow(source, amount);
    }
    final long now = getWorld().getTime();
    if (InquisitorFoes.shrugsOff(
        fight.untouchable(now),
        source.getAttacker() != null && !fight.holds(source.getAttacker().getPos()),
        source.isIn(SHRUGS_OFF))) {
      if (fight.warded(now)) {
        playSound(SoundEvents.ITEM_SHIELD_BLOCK, 1.0F, 1.4F);
      }
      return false;
    }
    if (fight.guards(this, plan(), source.getAttacker())) {
      return false;
    }
    final float before = getHealth();
    final boolean hurt = super.damage(source, amount);
    if (hurt && source.getAttacker() instanceof PlayerEntity player) {
      roll.join(player.getUuid());
    }
    if (hurt) {
      fight.hurt(this, plan(), source.getAttacker(), Math.max(0.0F, before - getHealth()));
    }
    return hurt;
  }

  private boolean downedBlow(final DamageSource source, final float amount) {
    final boolean bitten = source.isOf(NightbornDamage.BITE);
    final boolean turned =
        source.getAttacker() instanceof PlayerEntity player
            && quarry != null
            && quarry.turnedOn(player.getUuid());
    if (!InquisitorDrain.ends(
        bitten,
        Vampires.isVampire(source.getAttacker()),
        turned,
        source.getAttacker() != null && !fight.holds(source.getAttacker().getPos()))) {
      return false;
    }
    if (bitten && source.getAttacker() instanceof PlayerEntity biter) {
      roll.join(biter.getUuid());
    }
    final float taken = turned ? Float.MAX_VALUE : InquisitorDrain.taken(getHealth(), amount);
    return super.damage(source, taken);
  }

  @Override
  protected void applyDamage(final DamageSource source, final float amount) {
    final float capped = bitable() ? amount : fight.cap(plan(), false, amount);
    if (!bitable() && lastPhase() && capped >= getHealth()) {
      super.applyDamage(source, Math.max(0.0F, getHealth() - 1.0F));
      fallToKnees();
      return;
    }
    super.applyDamage(source, capped);
    if (!bitable()) {
      fight.hold(this, plan(), false);
    }
  }

  private boolean lastPhase() {
    return plan().map(p -> fight.phase() >= p.phases().size() - 1).orElse(true);
  }

  private void fallToKnees() {
    final InquisitorSettings settings = ServerConfigService.current().inquisitor();
    setHealth(InquisitorDrain.downedHealth(settings.drainSeconds()));
    setAttached(
        Downed.TYPE,
        new DownedState(
            DownedState.Phase.DOWNED, settings.drainWindowSeconds() * TICKS_PER_SECOND));
    getNavigation().stop();
    playSound(SoundEvents.ENTITY_PLAYER_BIG_FALL, 2.0F, 0.6F);
  }

  @Override
  public boolean cannotDespawn() {
    return true;
  }

  @Override
  public void onRemoved() {
    fight.end(this);
    presence.hideAll();
    super.onRemoved();
  }

  @Override
  public void onStoppedTrackingBy(final ServerPlayerEntity player) {
    super.onStoppedTrackingBy(player);
    presence.hide(player);
  }

  @Override
  public void setCustomName(@Nullable final Text name) {
    super.setCustomName(name);
    presence.update(this);
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    super.registerControllers(controllers);
    controllers.add(
        new AnimationController<>(
            this,
            "phase",
            BLEND_TICKS,
            state ->
                shownForm()
                    .clip(!state.isMoving(), state.isMoving())
                    .map(clip -> state.setAndContinue(ClipCache.loop(clip)))
                    .orElse(PlayState.STOP)));
    controllers.add(
        new AnimationController<>(
            this,
            "form",
            BLEND_TICKS,
            state -> {
              final String form = shownForm().form();
              return form.isEmpty() ? PlayState.STOP : state.setAndContinue(ClipCache.once(form));
            }));
    controllers.add(
        new AnimationController<>(
            this, "boss", BLEND_TICKS, state -> shownAction.play(state, clipSequence(), clip())));
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    fight.write(nbt);
    roll.write(nbt);
    roster.write(nbt);
    if (quarry != null) {
      quarry.write(nbt);
    }
    nbt.putLong(EMPTY_SINCE_KEY, emptySince);
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    fight.read(nbt);
    roll.read(nbt);
    roster.read(nbt);
    quarry = InquisitorQuarry.read(nbt).orElse(null);
    emptySince = nbt.contains(EMPTY_SINCE_KEY) ? nbt.getLong(EMPTY_SINCE_KEY) : NEVER;
  }
}
