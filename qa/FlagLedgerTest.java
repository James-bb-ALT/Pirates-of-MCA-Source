import dev.arandu.piratesmca.CrewLedger;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FlagLedgerTest {
    private CrewLedger ledger() throws Exception{return new CrewLedger(Files.createTempDirectory("pmca-flag-").resolve("crews.properties"));}
    @Test void onlyDefinitivePlayerCaptainCanBindFlag() throws Exception {
        var l=ledger();var c=l.getOrCreate(UUID.randomUUID(),"PLAYER_FOUNDED");var p=UUID.randomUUID();var flag=new CrewLedger.FlagAnchor("minecraft:overworld",2,64,3);
        assertFalse(l.bindFlag(c,p,flag));l.captain(c,p,true);assertFalse(l.bindFlag(c,UUID.randomUUID(),flag));assertTrue(l.bindFlag(c,p,flag));
        assertEquals(c,l.crewAtFlag(flag));
    }
    @Test void bindingAndRelocationPreserveCrewAcrossLoad() throws Exception {
        var l=ledger();var c=l.getOrCreate(UUID.randomUUID(),"SEAPOST");var p=UUID.randomUUID();l.captain(c,p,true);var m=l.add(UUID.randomUUID(),c.id,"friendly");
        var a=new CrewLedger.FlagAnchor("minecraft:overworld",2,64,3);var b=new CrewLedger.FlagAnchor("minecraft:the_nether",2,64,3);
        l.bindFlag(c,p,a);l.bindFlag(c,p,b);var copy=CrewLedger.fromState(l.exportState());
        assertNull(copy.crewAtFlag(a));assertEquals(c.id,copy.crewAtFlag(b).id);assertEquals(c.id,copy.member(m.id).crew);assertEquals("SEAPOST",copy.crew(c.id).anchor);
    }
    @Test void twoCrewsCannotClaimTheSameFlagAndCorruptDuplicatesFailClosed() throws Exception {
        var l=ledger();var a=l.getOrCreate(UUID.randomUUID(),"A");var b=l.getOrCreate(UUID.randomUUID(),"B");var p=UUID.randomUUID();var q=UUID.randomUUID();
        l.captain(a,p,true);l.captain(b,q,true);var flag=new CrewLedger.FlagAnchor("minecraft:overworld",2,64,3);
        assertTrue(l.bindFlag(a,p,flag));assertFalse(l.bindFlag(b,q,flag));assertNull(b.flag);
        b.flag=flag;assertThrows(IllegalStateException.class,()->CrewLedger.fromState(l.exportState()));
    }
    @Test void volunteeringRequiresConsentAndLivingAdultFreeMember() throws Exception {
        var l=ledger();var c=l.getOrCreate(UUID.randomUUID(),"UNASSIGNED");var m=l.add(UUID.randomUUID(),c.id,"friendly");
        assertTrue(l.canVolunteer(m,0,true,false));assertFalse(l.canVolunteer(m,-1,true,false));assertFalse(l.canVolunteer(m,7,false,false));assertFalse(l.canVolunteer(m,7,true,true));
        m.adult=false;assertFalse(l.canVolunteer(m,7,true,false));m.adult=true;m.fear=80;assertFalse(l.canVolunteer(m,7,true,false));m.fear=10;m.morale=34;assertFalse(l.canVolunteer(m,7,true,false));m.morale=55;m.alive=false;assertFalse(l.canVolunteer(m,7,true,false));
    }
}
