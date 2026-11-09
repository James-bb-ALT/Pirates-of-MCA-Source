import dev.arandu.piratesmca.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventMemoryTest {
    @Test void fatalHitDoesNotCreateAnotherAssaultAfterDeath() throws Exception {
        var l=new CrewLedger(Files.createTempDirectory("pmca-death-order-").resolve("crews.properties"));
        var c=l.getOrCreate(UUID.randomUUID(),"VESSEL");var victim=l.add(UUID.randomUUID(),c.id,"friendly");UUID captain=UUID.randomUUID();l.captain(c,captain,true);
        l.murder(victim.id,captain,100);int events=c.socialEvents.size();
        assertNull(l.assaultRecord(victim.id,captain,100));assertEquals(events,c.socialEvents.size());
    }
    @Test void structuredEventAndConsequencesSurviveReload() throws Exception {
        var path=Files.createTempDirectory("pmca-event-").resolve("crews.properties");var l=new CrewLedger(path);
        var c=l.getOrCreate(UUID.randomUUID(),"VESSEL");var actor=UUID.randomUUID();var target=UUID.randomUUID();var witness=UUID.randomUUID();
        var event=l.socialEvent(c,UUID.randomUUID(),"INSULT",1234,actor,target,"disputed share",30,List.of(witness));
        assertTrue(l.claimConsequence(event,"once"));l.save();var copy=new CrewLedger(path);
        var restored=copy.socialEvent(copy.crew(c.id),event.id);
        assertEquals(actor,restored.actor);assertEquals(target,restored.target);assertEquals(1234,restored.tick);
        assertEquals(Set.of(witness),restored.witnesses);assertEquals("disputed share",restored.reason);assertEquals(30,restored.severity);
        assertFalse(copy.claimConsequence(restored,"once"));
    }
    @Test void rumorRequiresKnowledgeAndHasReducedCredibility() throws Exception {
        var path=Files.createTempDirectory("pmca-rumor-").resolve("crews.properties");var l=new CrewLedger(path);
        var c=l.getOrCreate(UUID.randomUUID(),"VESSEL");var sender=l.add(UUID.randomUUID(),c.id,"friendly");var receiver=l.add(UUID.randomUUID(),c.id,"friendly");
        var absent=l.add(UUID.randomUUID(),c.id,"friendly");var victim=l.add(UUID.randomUUID(),c.id,"friendly");var killer=UUID.randomUUID();l.captain(c,killer,true);
        l.witnessMurder(sender.id,victim.id,killer,1000);
        var event=c.socialEvents.stream().filter(e->e.kind.equals("MURDER")).findFirst().orElseThrow();
        assertFalse(l.shareRumor(absent.id,receiver.id,event.id,1100));
        assertTrue(l.shareRumor(sender.id,receiver.id,event.id,1100));
        assertTrue(l.existingRelationship(receiver.id,killer).resentment<l.existingRelationship(sender.id,killer).resentment);
        assertNull(l.existingRelationship(absent.id,killer));assertFalse(absent.witnessedEvents.contains(event.id));
        int resentment=receiver.resentment;l.save();var copy=new CrewLedger(path);
        assertFalse(copy.shareRumor(sender.id,receiver.id,event.id,1200));assertEquals(resentment,copy.member(receiver.id).resentment);
        assertFalse(copy.shareRumor(receiver.id,sender.id,event.id,1200));
    }
    @Test void oneResourceCannotCreateInfiniteRivalry() throws Exception {
        var l=new CrewLedger(Files.createTempDirectory("pmca-resource-").resolve("crews.properties"));var c=l.getOrCreate(UUID.randomUUID(),"VESSEL");
        var a=l.add(UUID.randomUUID(),c.id,"greedy");var b=l.add(UUID.randomUUID(),c.id,"bruiser");UUID item=UUID.randomUUID();
        l.resourceDispute(a,b,item,10000);l.resourceDispute(a,b,item,10001);
        assertEquals(15,l.existingRelationship(a.id,b.id).resentment);
        assertNull(l.provoke(a,b,10002));
        l.resourceDispute(a,b,UUID.randomUUID(),10003);assertNotNull(l.provoke(a,b,10004));
    }
    @Test void provocationRollHappensOnceAcrossReloadAndNeedsARealMotive() throws Exception {
        var path=Files.createTempDirectory("pmca-provocation-").resolve("crews.properties");var l=new CrewLedger(path);var c=l.getOrCreate(UUID.randomUUID(),"VESSEL");
        var a=l.add(UUID.randomUUID(),c.id,"bruiser");var b=l.add(UUID.randomUUID(),c.id,"friendly");
        assertNull(l.provoke(a,b,10000),"Profile alone is not a cause");
        l.relationship(a.id,b.id).resentment=40;
        var event=l.provoke(a,b,10000);assertNotNull(event);assertFalse(l.rollProvocation(event,.2f));l.save();
        var copy=new CrewLedger(path);var restored=copy.socialEvent(copy.crew(c.id),event.id);
        assertFalse(copy.rollProvocation(restored,.01f),"Reload must not retry the 15 percent roll");
        assertNull(copy.provoke(copy.member(a.id),copy.member(b.id),10001));
    }
    @Test void structuredLogRetainsExactly128StableIds() throws Exception {
        var path=Files.createTempDirectory("pmca-event-bound-").resolve("crews.properties");var l=new CrewLedger(path);var c=l.getOrCreate(UUID.randomUUID(),"VESSEL");
        for(int i=0;i<130;i++)l.event(c,"EVENT_"+i);
        var ids=c.socialEvents.stream().map(e->e.id).toList();l.save();var copy=new CrewLedger(path);
        assertEquals(128,ids.size());assertEquals(ids,copy.crew(c.id).socialEvents.stream().map(e->e.id).toList());
    }
}
