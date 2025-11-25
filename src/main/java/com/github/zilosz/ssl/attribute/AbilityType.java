package com.github.zilosz.ssl.attribute;

import com.github.zilosz.ssl.attribute.impl.AerialAssault;
import com.github.zilosz.ssl.attribute.impl.AgileCombat;
import com.github.zilosz.ssl.attribute.impl.ArcticBreath;
import com.github.zilosz.ssl.attribute.impl.AxeThrow;
import com.github.zilosz.ssl.attribute.impl.Barrage;
import com.github.zilosz.ssl.attribute.impl.BatForm;
import com.github.zilosz.ssl.attribute.impl.BatWave;
import com.github.zilosz.ssl.attribute.impl.Berserk;
import com.github.zilosz.ssl.attribute.impl.BlindingFists;
import com.github.zilosz.ssl.attribute.impl.Bloodlust;
import com.github.zilosz.ssl.attribute.impl.Bloodshot;
import com.github.zilosz.ssl.attribute.impl.BombOmb;
import com.github.zilosz.ssl.attribute.impl.BoneExplosion;
import com.github.zilosz.ssl.attribute.impl.Boombox;
import com.github.zilosz.ssl.attribute.impl.ChainOfSteel;
import com.github.zilosz.ssl.attribute.impl.CoalCluster;
import com.github.zilosz.ssl.attribute.impl.DiseasedFlesh;
import com.github.zilosz.ssl.attribute.impl.DjsPassion;
import com.github.zilosz.ssl.attribute.impl.DrillTornado;
import com.github.zilosz.ssl.attribute.impl.Earthquake;
import com.github.zilosz.ssl.attribute.impl.ElectrostaticLeap;
import com.github.zilosz.ssl.attribute.impl.FrigidSpear;
import com.github.zilosz.ssl.attribute.impl.FrostedTwirl;
import com.github.zilosz.ssl.attribute.impl.GaiaToss;
import com.github.zilosz.ssl.attribute.impl.GhoulishWrath;
import com.github.zilosz.ssl.attribute.impl.GoldRush;
import com.github.zilosz.ssl.attribute.impl.GooeyBullet;
import com.github.zilosz.ssl.attribute.impl.GroundPound;
import com.github.zilosz.ssl.attribute.impl.HatThrow;
import com.github.zilosz.ssl.attribute.impl.HazmatSkin;
import com.github.zilosz.ssl.attribute.impl.HungryFish;
import com.github.zilosz.ssl.attribute.impl.IncarnationSlam;
import com.github.zilosz.ssl.attribute.impl.Jetpack;
import com.github.zilosz.ssl.attribute.impl.MixTapeDrop;
import com.github.zilosz.ssl.attribute.impl.OlympicDive;
import com.github.zilosz.ssl.attribute.impl.Rasengan;
import com.github.zilosz.ssl.attribute.impl.Rasenshuriken;
import com.github.zilosz.ssl.attribute.impl.RocketLauncher;
import com.github.zilosz.ssl.attribute.impl.RopedArrow;
import com.github.zilosz.ssl.attribute.impl.Scarecrow;
import com.github.zilosz.ssl.attribute.impl.ShadowAmbush;
import com.github.zilosz.ssl.attribute.impl.ShadowCloneJutsu;
import com.github.zilosz.ssl.attribute.impl.SpookyScream;
import com.github.zilosz.ssl.attribute.impl.SpringTrap;
import com.github.zilosz.ssl.attribute.impl.SquidDash;
import com.github.zilosz.ssl.attribute.impl.StickySituation;
import com.github.zilosz.ssl.attribute.impl.Teleport;
import com.github.zilosz.ssl.attribute.impl.Thunderbolt;
import com.github.zilosz.ssl.attribute.impl.VoltTackle;
import com.github.zilosz.ssl.attribute.impl.WebGrapple;
import com.github.zilosz.ssl.attribute.impl.WebbedSnare;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public enum AbilityType implements Supplier<Ability> {
  AERIAL_ASSAULT("aerial-assault", AerialAssault::new),
  AGILE_COMBAT("agile-combat", AgileCombat::new),
  ARCTIC_BREATH("arctic-breath", ArcticBreath::new),
  AXE_THROW("axe-throw", AxeThrow::new),
  BARRAGE("barrage", Barrage::new),
  BAT_FORM("bat-form", BatForm::new),
  BAT_WAVE("bat-wave", BatWave::new),
  BERSERK("berserk", Berserk::new),
  BLINDING_FISTS("blinding-fists", BlindingFists::new),
  BLOOD_LUST("blood-lust", Bloodlust::new),
  BLOOD_SHOT("blood-shot", Bloodshot::new),
  BOMB_OMB("bomb-omb", BombOmb::new),
  BONE_EXPLOSION("bone-explosion", BoneExplosion::new),
  BONE_TIPPED_ARROW("bone-tipped-arrow", ItemGiver::new),
  BOOMBOX("boombox", Boombox::new),
  CHAIN_OF_STEEL("chain-of-steel", ChainOfSteel::new),
  COAL_CLUSTER("coal-cluster", CoalCluster::new),
  DISEASED_FLESH("diseased-flesh", DiseasedFlesh::new),
  DJS_PASSION("djs-passion", DjsPassion::new),
  DRILL_TORNADO("drill-tornado", DrillTornado::new),
  EARTHQUAKE("earthquake", Earthquake::new),
  ELECTROSTATIC_LEAP("electrostatic-leap", ElectrostaticLeap::new),
  FRIGID_SPEAR("frigid-spear", FrigidSpear::new),
  FROSTED_TWIRL("frosted-twirl", FrostedTwirl::new),
  GAIA_TOSS("gaia-toss", GaiaToss::new),
  GHOULISH_WRATH("ghoulish-wrath", GhoulishWrath::new),
  GOLD_RUSH("gold-rush", GoldRush::new),
  GOOEY_BULLET("gooey-bullet", GooeyBullet::new),
  GROUND_POUND("ground-pound", GroundPound::new),
  HAT_THROW("hat-throw", HatThrow::new),
  HAZMAT_SKIN("hazmat-skin", HazmatSkin::new),
  HUNGRY_FISH("hungry-fish", HungryFish::new),
  ICE_SKATES("ice-skates", PermanentPotion::new),
  INCARNATION_SLAM("incarnation-slam", IncarnationSlam::new),
  JETPACK("jetpack", Jetpack::new),
  MIX_TAPE_DROP("mix-tape-drop", MixTapeDrop::new),
  OLYMPIC_DIVE("olympic-dive", OlympicDive::new),
  RASENGAN("rasengan", Rasengan::new),
  RASENSHURIKEN("rasenshuriken", Rasenshuriken::new),
  ROCKET_LAUNCHER("rocket-launcher", RocketLauncher::new),
  ROPED_ARROW("roped-arrow", RopedArrow::new),
  SCARECROW("scarecrow", Scarecrow::new),
  SHADOW_AMBUSH("shadow-ambush", ShadowAmbush::new),
  SHADOW_CLONE_JUTSU("shadow-clone-jutsu", ShadowCloneJutsu::new),
  SPOOKY_SCREAM("spooky-scream", SpookyScream::new),
  SPRING_TRAP("spring-trap", SpringTrap::new),
  SQUID_DASH("squid-dash", SquidDash::new),
  STICKY_SITUATION("sticky-situation", StickySituation::new),
  TELEPORT("teleport", Teleport::new),
  THUNDERBOLT("thunderbolt", Thunderbolt::new),
  VOLT_TACKLE("volt-tackle", VoltTackle::new),
  WEBBED_SNARE("webbed-snare", WebbedSnare::new),
  WEB_GRAPPLE("web-grapple", WebGrapple::new);

  private final String fileName;
  private final Supplier<Ability> supplier;

  @Override
  public Ability get() {
    return supplier.get();
  }
}
