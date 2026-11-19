package com.grahambartley.nightborn.guild.veteran;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.guild.HunterRig;
import com.grahambartley.nightborn.guild.HunterVoice;
import com.grahambartley.nightborn.inquisitor.StakeDownedGoal;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public class VeteranHunterEntity extends HunterEntity {

  public static final Identifier DEFAULT = Identifier.of(Nightborn.MOD_ID, "veteran");
  static final String VETERAN_KEY = "Veteran";

  private static final TrackedData<String> ATTACK =
      DataTracker.registerData(VeteranHunterEntity.class, TrackedDataHandlerRegistry.STRING);

  private final VeteranAttacks attacks = new VeteranAttacks(this);
  private Identifier veteranId = DEFAULT;
  private String shownAttack = "";

  public VeteranHunterEntity(final EntityType<? extends PathAwareEntity> type, final World world) {
    super(type, world);
  }

  public static DefaultAttributeContainer.Builder createVeteranAttributes() {
    return createHunterAttributes();
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(ATTACK, "");
  }

  @Override
  protected void initGoals() {
    super.initGoals();
    goalSelector.add(1, new StakeDownedGoal(this));
  }

  @Override
  public HunterRig rig() {
    return HunterRig.veteran(bearsWomansName());
  }

  public void serveAs(final Identifier id) {
    veteranId = id;
  }

  public Identifier veteranId() {
    return veteranId;
  }

  private Optional<Veteran> veteran() {
    return VeteranData.veteran(getRegistryManager(), veteranId);
  }

  @Override
  public void arm(final String wanted) {
    veteran().ifPresentOrElse(kind -> carry(veteranId.toString(), kind.kit()), () -> super.arm(""));
  }

  @Override
  protected void settle() {
    super.settle();
    veteran()
        .ifPresent(
            kind -> {
              final double hunterHealth =
                  DefaultAttributeRegistry.get(GuildMobs.HUNTER)
                      .getBaseValue(EntityAttributes.GENERIC_MAX_HEALTH);
              base(EntityAttributes.GENERIC_MAX_HEALTH, kind.health(hunterHealth));
              base(EntityAttributes.GENERIC_ARMOR, kind.armor());
              setHealth(getMaxHealth());
            });
  }

  private void base(final RegistryEntry<EntityAttribute> attribute, final double value) {
    final EntityAttributeInstance instance = getAttributeInstance(attribute);
    if (instance != null) {
      instance.setBaseValue(value);
    }
  }

  @Override
  public void fight() {
    super.fight();
    final Map<Identifier, VeteranAttack> known =
        veteran().map(kind -> VeteranData.attacks(getRegistryManager(), kind)).orElse(Map.of());
    attacks.tick(known);
  }

  void shout() {
    speak(HunterVoice.ALERT);
  }

  void showAttack(final String clip) {
    dataTracker.set(ATTACK, clip);
  }

  public String attack() {
    return dataTracker.get(ATTACK);
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putString(VETERAN_KEY, veteranId.toString());
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    veteranId =
        Optional.ofNullable(Identifier.tryParse(nbt.getString(VETERAN_KEY))).orElse(DEFAULT);
    super.readCustomDataFromNbt(nbt);
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    super.registerControllers(controllers);
    controllers.add(new AnimationController<>(this, "attack", ANIMATION_BLEND_TICKS, this::strike));
  }

  private PlayState strike(final AnimationState<VeteranHunterEntity> state) {
    final String clip = attack();
    if (!clip.equals(shownAttack)) {
      shownAttack = clip;
      state.getController().forceAnimationReset();
    }
    return clip.isEmpty()
        ? PlayState.STOP
        : state.setAndContinue(RawAnimation.begin().thenPlay(clip));
  }
}
