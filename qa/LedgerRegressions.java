import dev.arandu.piratesmca.CrewLedger;
import java.nio.file.*;
import java.util.*;

/** Pure Java domain regressions. Does NOT test Minecraft runtime, Fabric or Mixin. */
public final class LedgerRegressions {
    private static int passed = 0, failed = 0;
    @FunctionalInterface interface Case { void check() throws Exception; }
    static void test(String id, Case c) {
        try { c.check(); ++passed; System.out.println("PASS " + id); }
        catch (Throwable t) { ++failed; System.out.println("FAIL " + id + ": " + t); }
    }
    static CrewLedger ledger() throws Exception {
        return new CrewLedger(Files.createTempDirectory("pmca-ledger-qa-").resolve("data/crews_v2.properties"));
    }
    static void expect(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static CrewLedger.Crew crew(CrewLedger l) { return l.getOrCreate(UUID.randomUUID(), "seapost_10_64_20"); }
    public static void main(String[] args) {
        test("LEDGER-01_FREE_SOLO", () -> {
            var l = ledger(); var c = l.getOrCreate(UUID.randomUUID(), "UNASSIGNED");
            var m = l.add(UUID.randomUUID(), c.id, "bruiser");
            expect(l.isFreeSolo(m), "newly summoned pirate must be available to form a crew");
            expect(c.captain == null && m.role == CrewLedger.Role.CREW, "free pirate cannot auto-become captain");
        });
        test("LEDGER-02_TRANSFER_PERSISTS", () -> {
            Path f=Files.createTempDirectory("pmca-ledger-qa-").resolve("data/crews_v2.properties");
            var l = new CrewLedger(f); var source=l.getOrCreate(UUID.randomUUID(),"UNASSIGNED");
            var sailor=l.add(UUID.randomUUID(),source.id,"friendly");
            var destination=crew(l); var player=UUID.randomUUID();
            expect(l.transfer(sailor,destination), "transfer should succeed");
            l.captain(destination,player,true); l.save(); var reloaded=new CrewLedger(f);
            expect(player.equals(reloaded.crew(destination.id).captain), "player captain lost on reload");
            expect(destination.id.equals(reloaded.member(sailor.id).crew), "crew membership lost on reload");
        });
        test("LEDGER-03_NPC_CAPTAIN_EXCLUDED_FROM_REBEL_DENOMINATOR", () -> {
            var l=ledger(); var c=crew(l); UUID captain=UUID.randomUUID();
            l.add(captain,c.id,"captain"); l.captain(c,captain,false);
            for(int i=0;i<7;i++) l.add(UUID.randomUUID(),c.id,"bruiser");
            // v2 spec: 7 eligible non-captains, ceil(0.4 * 7)=3.
            expect(l.requiredRebels(c)==3,"expected 3 of 7 eligible, got "+l.requiredRebels(c));
        });
        test("LEDGER-04_OFFICER_ROLES_MUTUALLY_EXCLUSIVE", () -> {
            var l=ledger(); var c=crew(l); var m=l.add(UUID.randomUUID(),c.id,"greedy");
            l.officer(c,m.id,CrewLedger.Role.FIRST_MATE);
            l.officer(c,m.id,CrewLedger.Role.QUARTERMASTER);
            expect(!m.id.equals(c.firstMate) || !m.id.equals(c.quartermaster),
                "same NPC simultaneously occupies first_mate and quartermaster");
        });
        test("LEDGER-05_MIGRATE_LEGACY_SOLO_CAPTAIN", () -> {
            var l=ledger(); var c=l.getOrCreate(UUID.randomUUID(),"UNASSIGNED");
            var m=l.add(UUID.randomUUID(),c.id,"captain"); l.captain(c,m.id,false);
            l.repairAutoCaptain(m);
            expect(c.captain==null && m.role==CrewLedger.Role.CREW,"legacy solo captain not demoted");
        });
        test("LEDGER-06_ULTIMATUM_60_SECONDS", () -> {
            var l=ledger(); var c=crew(l); l.captain(c,UUID.randomUUID(),true);
            for(int i=0;i<3;i++){var m=l.add(UUID.randomUUID(),c.id,"bruiser");m.resentment=90;m.ambition=90;}
            expect(l.startConspiracy(c,1000),"conspiracy should begin");
            expect(l.beginUltimatum(c,1000),"ultimatum should begin");
            expect(c.ultimatumUntil==2200,"ultimatum is not 1200 ticks");
        });
        test("LEDGER-07_NO_CROSS_CREW_REASSIGN_BY_PROXIMITY", () -> {
            var l=ledger(); var a=crew(l);var b=crew(l);
            var m=l.add(UUID.randomUUID(),a.id,"friendly");
            expect(l.add(m.id,b.id,"crabby").crew.equals(a.id),"membership changed implicitly");
        });
        System.out.println("Ledger QA: "+passed+" PASS, "+failed+" FAIL. Baseline failures are defects to investigate, not proof of a fix.");
        if(failed>0) System.exit(1);
    }
}
