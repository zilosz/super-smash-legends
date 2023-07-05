package com.github.zilosz.ssl.attribute;

import com.github.zilosz.ssl.attribute.implementation.AerialAssault;
import com.github.zilosz.ssl.attribute.implementation.AgileCombat;
import com.github.zilosz.ssl.attribute.implementation.ArcticBreath;
import com.github.zilosz.ssl.attribute.implementation.AxeThrow;
import com.github.zilosz.ssl.attribute.implementation.Barrage;
import com.github.zilosz.ssl.attribute.implementation.BatForm;
import com.github.zilosz.ssl.attribute.implementation.BatWave;
import com.github.zilosz.ssl.attribute.implementation.Berserk;
import com.github.zilosz.ssl.attribute.implementation.BlindingFists;
import com.github.zilosz.ssl.attribute.implementation.Bloodlust;
import com.github.zilosz.ssl.attribute.implementation.Bloodshot;
import com.github.zilosz.ssl.attribute.implementation.BombOmb;
import com.github.zilosz.ssl.attribute.implementation.BoneExplosion;
import com.github.zilosz.ssl.attribute.implementation.Boombox;
import com.github.zilosz.ssl.attribute.implementation.ChainOfSteel;
import com.github.zilosz.ssl.attribute.implementation.CoalCluster;
import com.github.zilosz.ssl.attribute.implementation.DiseasedFlesh;
import com.github.zilosz.ssl.attribute.implementation.DjsPassion;
import com.github.zilosz.ssl.attribute.implementation.DrillTornado;
import com.github.zilosz.ssl.attribute.implementation.Earthquake;
import com.github.zilosz.ssl.attribute.implementation.ElectrostaticLeap;
import com.github.zilosz.ssl.attribute.implementation.FrigidSpear;
import com.github.zilosz.ssl.attribute.implementation.FrostedTwirl;
import com.github.zilosz.ssl.attribute.implementation.GaiaToss;
import com.github.zilosz.ssl.attribute.implementation.GoldRush;
import com.github.zilosz.ssl.attribute.implementation.GooeyBullet;
import com.github.zilosz.ssl.attribute.implementation.GroundPound;
import com.github.zilosz.ssl.attribute.implementation.HatThrow;
import com.github.zilosz.ssl.attribute.implementation.HazmatSkin;
import com.github.zilosz.ssl.attribute.implementation.HungryFish;
import com.github.zilosz.ssl.attribute.implementation.IncarnationSlam;
import com.github.zilosz.ssl.attribute.implementation.ItemGiver;
import com.github.zilosz.ssl.attribute.implementation.Jetpack;
import com.github.zilosz.ssl.attribute.implementation.MixTapeDrop;
import com.github.zilosz.ssl.attribute.implementation.OlympicDive;
import com.github.zilosz.ssl.attribute.implementation.PermanentPotion;
import com.github.zilosz.ssl.attribute.implementation.Rasengan;
import com.github.zilosz.ssl.attribute.implementation.Rasenshuriken;
import com.github.zilosz.ssl.attribute.implementation.RocketLauncher;
import com.github.zilosz.ssl.attribute.implementation.RopedArrow;
import com.github.zilosz.ssl.attribute.implementation.SaberSpin;
import com.github.zilosz.ssl.attribute.implementation.ShadowAmbush;
import com.github.zilosz.ssl.attribute.implementation.ShadowCloneJutsu;
import com.github.zilosz.ssl.attribute.implementation.SpringTrap;
import com.github.zilosz.ssl.attribute.implementation.SquidDash;
import com.github.zilosz.ssl.attribute.implementation.StickySituation;
import com.github.zilosz.ssl.attribute.implementation.SuperhumanPunch;
import com.github.zilosz.ssl.attribute.implementation.Teleport;
import com.github.zilosz.ssl.attribute.implementation.TerraShot;
import com.github.zilosz.ssl.attribute.implementation.Thunderbolt;
import com.github.zilosz.ssl.attribute.implementation.ViceGrip;
import com.github.zilosz.ssl.attribute.implementation.VoltTackle;
import com.github.zilosz.ssl.attribute.implementation.WebGrapple;
import com.github.zilosz.ssl.attribute.implementation.WebbedSnare;
import lombok.Getter;

import java.util.function.Supplier;

@Getter
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
    SABER_SPIN("saber-spin", SaberSpin::new),
    SHADOW_AMBUSH("shadow-ambush", ShadowAmbush::new),
    SHADOW_CLONE_JUTSU("shadow-clone-jutsu", ShadowCloneJutsu::new),
    SLOWNESS("slowness", PermanentPotion::new),
    SPRING_TRAP("spring-trap", SpringTrap::new),
    SQUID_DASH("squid-dash", SquidDash::new),
    STICKY_SITUATION("sticky-situation", StickySituation::new),
    SUPERHUMAN_PUNCH("superhuman-punch", SuperhumanPunch::new),
    TELEPORT("teleport", Teleport::new),
    TERRA_SHOT("terra-shot", TerraShot::new),
    THUNDERBOLT("thunderbolt", Thunderbolt::new),
    VICE_GRIP("vice-grip", ViceGrip::new),
    VOLT_TACKLE("volt-tackle", VoltTackle::new),
    WEBBED_SNARE("webbed-snare", WebbedSnare::new),
    WEB_GRAPPLE("web-grapple", WebGrapple::new);

    private final String fileName;
    private final Supplier<Ability> supplier;

    AbilityType(String fileName, Supplier<Ability> supplier) {
        this.supplier = supplier;
        this.fileName = fileName;
    }

    @Override
    public Ability get() {
        return this.supplier.get();
    }
}
