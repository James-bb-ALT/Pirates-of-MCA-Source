import dev.arandu.piratesmca.BrawlDamagePolicy;
import dev.arandu.piratesmca.CrewLedger;
import java.nio.file.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CombatAndClockTest {
    @Test void fatalBrawlHitStopsAtQuarterHealth() {
        assertEquals(15f,BrawlDamagePolicy.cap(20f,20f,1000f,true));
        assertEquals(0f,BrawlDamagePolicy.cap(5f,20f,1000f,true));
        assertEquals(0f,BrawlDamagePolicy.cap(3f,20f,1000f,true));
    }
    @Test void externalDamageRemainsLethal() {
        assertEquals(1000f,BrawlDamagePolicy.cap(20f,20f,1000f,false));
    }
    @Test void ultimatumPausesOfflineAndSurvivesReload() throws Exception {
        Path f=Files.createTempDirectory("pmca-clock-").resolve("crew.properties");
        var l=new CrewLedger(f);var c=l.getOrCreate(UUID.randomUUID(),"SEAPOST");
        l.captain(c,UUID.randomUUID(),true);
        for(int i=0;i<4;i++){var m=l.add(UUID.randomUUID(),c.id,"bruiser");m.resentment=90;m.ambition=90;}
        assertTrue(l.startConspiracy(c,100));assertTrue(l.beginUltimatum(c,100));
        l.tickUltimatum(c,24000,false);assertEquals(1200,c.ultimatumRemaining);
        l.tickUltimatum(c,24000,true);assertEquals(1180,c.ultimatumRemaining,"never replay an unloaded gap");
        l.save();var copy=new CrewLedger(f);var recovered=copy.crew(c.id);
        assertEquals(1180,recovered.ultimatumRemaining);
        for(int i=0;i<58;i++)copy.tickUltimatum(recovered,20,true);
        assertEquals(CrewLedger.Phase.ULTIMATUM,recovered.phase);
        copy.tickUltimatum(recovered,20,true);assertEquals(CrewLedger.Phase.REBELLION,recovered.phase);
    }
}
