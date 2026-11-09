import dev.arandu.piratesmca.CrewLedger;
import java.nio.file.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SocialStateTest {
    @Test void childrenAndDeadMembersCannotVoteOrLead() throws Exception {
        var l=new CrewLedger(Files.createTempDirectory("pmca-votes-").resolve("crews.properties"));
        var c=l.getOrCreate(UUID.randomUUID(),"SEAPOST");l.captain(c,UUID.randomUUID(),true);
        for(int i=0;i<8;i++) {
            var m=l.add(UUID.randomUUID(),c.id,"bruiser");m.resentment=90;m.ambition=90;
            if(i<4)m.adult=false;
            if(i==4)m.alive=false;
        }
        assertEquals(2,l.requiredRebels(c));assertEquals(3,l.conspirators(c));
        var child=l.member(c.members.iterator().next());
        assertThrows(IllegalArgumentException.class,()->l.officer(c,child.id,CrewLedger.Role.FIRST_MATE));
        l.depose(c,1000);UUID elected=l.electSuccessor(c,1000);
        assertNotNull(elected);assertTrue(l.member(elected).adult && l.member(elected).alive);
    }
    @Test void witnessedMurderIsNotAppliedTwiceAfterReload() throws Exception {
        Path f=Files.createTempDirectory("pmca-witness-").resolve("crews.properties");var l=new CrewLedger(f);
        var c=l.getOrCreate(UUID.randomUUID(),"SEAPOST");
        var victim=l.add(UUID.randomUUID(),c.id,"friendly");var witness=l.add(UUID.randomUUID(),c.id,"friendly");
        UUID killer=UUID.randomUUID();l.relationship(witness.id,victim.id).friendship=80;
        l.witnessMurder(witness.id,victim.id,killer,1000);int resentment=l.relationship(witness.id,killer).resentment;
        assertEquals(60,resentment);l.save();var copy=new CrewLedger(f);
        copy.witnessMurder(witness.id,victim.id,killer,9000);
        assertEquals(resentment,copy.relationship(witness.id,killer).resentment);
    }
    @Test void enemyWitnessHasDifferentReactionAndUninformedMemberHasNone() throws Exception {
        var l=new CrewLedger(Files.createTempDirectory("pmca-enemy-").resolve("crews.properties"));
        var c=l.getOrCreate(UUID.randomUUID(),"SEAPOST");var v=l.add(UUID.randomUUID(),c.id,"friendly");
        var enemy=l.add(UUID.randomUUID(),c.id,"greedy");var uninformed=l.add(UUID.randomUUID(),c.id,"friendly");
        UUID killer=UUID.randomUUID();l.relationship(enemy.id,v.id).friendship=-80;
        l.witnessMurder(enemy.id,v.id,killer,1000);
        assertEquals(0,l.relationship(enemy.id,killer).resentment);
        assertEquals(0,l.relationship(uninformed.id,killer).resentment);
        assertEquals(0,uninformed.resentment);
    }
    @Test void deathIsIdempotentAndDoesNotResurrectOnRegistration() throws Exception {
        var l=new CrewLedger(Files.createTempDirectory("pmca-death-").resolve("crews.properties"));
        var c=l.getOrCreate(UUID.randomUUID(),"SEAPOST");var m=l.add(UUID.randomUUID(),c.id,"friendly");
        l.removeDead(m.id,1000,"FALL");int events=c.events.size();l.removeDead(m.id,1001,"FALL");
        assertEquals(events,c.events.size());l.add(m.id,c.id,"friendly");assertFalse(c.members.contains(m.id));
    }
}
