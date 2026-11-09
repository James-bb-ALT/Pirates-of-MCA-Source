import dev.arandu.piratesmca.CrewLedger;
import java.util.*;
import java.nio.file.*;
public final class tests_ledger {
    public static void main(String[] x)throws Exception{
        Path f=Files.createTempDirectory("pirates-test-").resolve("data/crews.properties");
        CrewLedger l=new CrewLedger(f);UUID id=UUID.randomUUID();var c=l.getOrCreate(id,"seapost_100_20_100");
        UUID p=UUID.randomUUID();l.captain(c,p,true);
        var a=l.add(UUID.randomUUID(),id,"bruiser");var b=l.add(UUID.randomUUID(),id,"greedy");
        a.resentment=75;b.resentment=75;a.ambition=75;b.ambition=75;
        if(l.requiredRebels(c)!=2)throw new AssertionError("motim threshold");
        if(!l.startConspiracy(c,1000) || !l.beginUltimatum(c,1000))throw new AssertionError("conspiracy");
        if(c.ultimatumUntil!=2200)throw new AssertionError("ultimatum 60 sec");
        l.beginRebellion(c);
        l.finishRebellion(c,3000,false);l.save();
        CrewLedger reloaded=new CrewLedger(f);var recovered=reloaded.crew(id);
        if(recovered==null || recovered.playerCaptain || recovered.members.size()!=2)throw new AssertionError("save/load");
        if(reloaded.member(a.id).resentment!=75)throw new AssertionError("stats lost");
        if(!"seapost_100_20_100".equals(recovered.anchor))throw new AssertionError("origin lost");
        UUID id2=UUID.randomUUID();var c2=reloaded.getOrCreate(id2,"seapost_101_20_100");
        if(reloaded.add(a.id,id2,"friendly").crew.equals(id2))throw new AssertionError("cross crew merged");
        UUID soloId=UUID.randomUUID(), sailor=UUID.randomUUID();
        var solo=reloaded.getOrCreate(soloId,"UNASSIGNED");
        var free=reloaded.add(sailor,soloId,"bruiser");
        if(!reloaded.isFreeSolo(free) || solo.captain!=null || free.role!=CrewLedger.Role.CREW)
            throw new AssertionError("free /summon sailor should not be a captain");
        reloaded.captain(solo,sailor,false); // simulate historical Alpha 3 auto-captain
        reloaded.repairAutoCaptain(free);
        if(solo.captain!=null || free.role!=CrewLedger.Role.CREW || solo.phase!=CrewLedger.Phase.CALM)
            throw new AssertionError("legacy solo captain migration");
        UUID destination=UUID.randomUUID();var newCrew=reloaded.getOrCreate(destination,"PLAYER_FOUNDED");
        if(!reloaded.transfer(free,newCrew)||free.role!=CrewLedger.Role.CREW||!newCrew.members.contains(sailor))
            throw new AssertionError("recruit transfer");
        reloaded.updateProfile(free,"greedy");
        if(!"greedy".equals(free.profile))throw new AssertionError("profile sync");
        reloaded.captain(newCrew,p,true);reloaded.save();
        CrewLedger again=new CrewLedger(f);
        if(!p.equals(again.crew(destination).captain)||!again.crew(destination).playerCaptain
              || !again.member(sailor).crew.equals(destination))throw new AssertionError("player found persistence");
        System.out.println("CrewLedger tests OK: mutiny, capitancy, persistence, separation, free-solo migration, transfer");
    }
}
