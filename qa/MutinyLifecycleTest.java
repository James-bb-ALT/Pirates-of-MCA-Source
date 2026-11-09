import dev.arandu.piratesmca.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MutinyLifecycleTest {
    @Test void discontentPrecedesConspiracy() throws Exception {
        var l=new CrewLedger(Files.createTempDirectory("pmca-discontent-").resolve("crews.properties"));var c=l.getOrCreate(UUID.randomUUID(),"VESSEL");
        var m=l.add(UUID.randomUUID(),c.id,"bruiser");l.captain(c,UUID.randomUUID(),true);
        m.resentment=45;l.evaluateDiscontent(c,100);assertEquals(CrewLedger.Phase.DISCONTENT,c.phase);assertFalse(l.startConspiracy(c,100));
        m.resentment=70;m.ambition=90;m.fear=0;assertTrue(l.startConspiracy(c,200));assertEquals(CrewLedger.Phase.CONSPIRACY,c.phase);
    }
    @Test void persuasionRequiresKnownCauseAndRespectsCooldownAcrossReload() throws Exception {
        var file=Files.createTempDirectory("pmca-persuasion-").resolve("crews.properties");var l=new CrewLedger(file);var c=l.getOrCreate(UUID.randomUUID(),"VESSEL");
        var leader=l.add(UUID.randomUUID(),c.id,"bruiser");var candidate=l.add(UUID.randomUUID(),c.id,"friendly");var victim=l.add(UUID.randomUUID(),c.id,"friendly");UUID captain=UUID.randomUUID();l.captain(c,captain,true);
        leader.resentment=90;leader.ambition=90;leader.fear=0;candidate.resentment=40;candidate.fear=0;candidate.loyalty=20;
        assertTrue(l.startConspiracy(c,1000));assertFalse(l.persuadeConspirator(leader,candidate,1000));
        var assault=l.assaultRecord(victim.id,captain,1000);l.witnessAssault(candidate.id,assault,1000);
        assertTrue(l.persuadeConspirator(leader,candidate,1001));int anger=candidate.resentment;l.save();
        var copy=new CrewLedger(file);assertFalse(copy.persuadeConspirator(copy.member(leader.id),copy.member(candidate.id),1002));assertEquals(anger,copy.member(candidate.id).resentment);
        assertTrue(copy.persuadeConspirator(copy.member(leader.id),copy.member(candidate.id),1602));
    }
    @Test void voluntaryDeparturePreservesCaptainUniquenessAndSafeExitUntilNewAttack() throws Exception {
        var file=Files.createTempDirectory("pmca-yield-").resolve("crews.properties");var l=new CrewLedger(file);var c=l.getOrCreate(UUID.randomUUID(),"VESSEL");
        var a=l.add(UUID.randomUUID(),c.id,"friendly");l.add(UUID.randomUUID(),c.id,"bruiser");UUID player=UUID.randomUUID();l.captain(c,player,true);
        l.voluntaryDeparture(c,100,true);assertNull(c.captain);assertEquals(player,c.lastDeposedCaptain);assertEquals(1300,c.surrenderProtectionUntil);
        assertNotNull(l.electSuccessor(c,100));assertFalse(c.playerCaptain);assertEquals(CrewLedger.Phase.AFTERMATH,c.phase);l.save();
        var copy=new CrewLedger(file);var restored=copy.crew(c.id);assertEquals(1300,restored.surrenderProtectionUntil);
        copy.breakDepartureProtection(restored,UUID.randomUUID(),a.id,200);assertEquals(1300,restored.surrenderProtectionUntil);
        copy.breakDepartureProtection(restored,player,a.id,200);assertEquals(0,restored.surrenderProtectionUntil);
    }
    @Test void failedMutinyHasResolutionAftermathAndCooldown() throws Exception {
        var l=new CrewLedger(Files.createTempDirectory("pmca-aftermath-").resolve("crews.properties"));var c=l.getOrCreate(UUID.randomUUID(),"VESSEL");
        var a=l.add(UUID.randomUUID(),c.id,"bruiser");var b=l.add(UUID.randomUUID(),c.id,"bruiser");l.captain(c,UUID.randomUUID(),true);
        for(var m:List.of(a,b)){m.resentment=90;m.ambition=90;m.fear=0;}
        assertTrue(l.startConspiracy(c,100));assertTrue(l.beginUltimatum(c,100));l.beginRebellion(c);assertEquals("REBEL",a.allegiance);
        l.finishRebellion(c,200,true);assertEquals(CrewLedger.Phase.AFTERMATH,c.phase);assertEquals(24200,c.cooldownUntil);assertEquals("NEUTRAL",a.allegiance);
        assertTrue(c.socialEvents.stream().anyMatch(e->e.kind.equals("MUTINY_RESOLUTION")));
        c.phase=CrewLedger.Phase.CALM;assertFalse(l.startConspiracy(c,201));
    }
}
