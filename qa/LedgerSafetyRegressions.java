import dev.arandu.piratesmca.CrewLedger;
import java.nio.file.*;
import java.util.*;

/** Regression evidence for domain rules only; never substitutes for a TC in-game PASS. */
public final class LedgerSafetyRegressions {
    interface Check { void run() throws Exception; }
    static int passed, failed;
    static void check(String name, Check c) {
        try { c.run(); passed++; System.out.println("PASS " + name); }
        catch (Throwable e) { failed++; System.out.println("FAIL " + name + ": " + e); }
    }
    static void expect(boolean b) { if (!b) throw new AssertionError(); }
    static CrewLedger ledger() throws Exception {
        return new CrewLedger(Files.createTempDirectory("pmca-safety-").resolve("crews.properties"));
    }
    static CrewLedger.Crew crew(CrewLedger l) { return l.getOrCreate(UUID.randomUUID(), "SEAPOST"); }
    static CrewLedger.Member member(CrewLedger l, CrewLedger.Crew c) { return l.add(UUID.randomUUID(), c.id, "bruiser"); }
    public static void main(String[] args) {
        check("TC-CREW-07_OFFICER_PROMOTED_CAPTAIN", () -> {
            var l=ledger();var c=crew(l);var m=member(l,c);
            l.officer(c,m.id,CrewLedger.Role.FIRST_MATE);l.captain(c,m.id,false);
            expect(c.firstMate==null && m.role==CrewLedger.Role.CAPTAIN);
        });
        check("TC-CREW-07_CAPTAIN_CANNOT_BE_OFFICER", () -> {
            var l=ledger();var c=crew(l);var m=member(l,c);l.captain(c,m.id,false);
            try { l.officer(c,m.id,CrewLedger.Role.FIRST_MATE);throw new AssertionError("accepted captain"); }
            catch(IllegalArgumentException expected) { expect(m.role==CrewLedger.Role.CAPTAIN); }
        });
        check("TC-CREW-06_TRANSFER_CLEARS_OFFICE", () -> {
            var l=ledger();var a=crew(l);var b=crew(l);var m=member(l,a);
            l.officer(a,m.id,CrewLedger.Role.QUARTERMASTER);l.transfer(m,b);
            expect(a.quartermaster==null && !a.members.contains(m.id) && b.members.contains(m.id));
        });
        check("TC-CREW-11_PLAYER_ONE_CREW", () -> {
            var l=ledger();var a=crew(l);var b=crew(l);UUID p=UUID.randomUUID();l.captain(a,p,true);
            try { l.captain(b,p,true);throw new AssertionError("accepted second crew"); }
            catch(IllegalArgumentException expected) { expect(b.captain==null && p.equals(a.captain)); }
        });
        check("TC-CREW-11_CAPTAIN_MUST_BELONG", () -> {
            var l=ledger();var a=crew(l);var b=crew(l);var m=member(l,a);
            try { l.captain(b,m.id,false);throw new AssertionError("accepted foreign captain"); }
            catch(IllegalArgumentException expected) { expect(b.captain==null); }
        });
        check("TC-SOC-03_CONSPIRACY_BEFORE_MAJORITY", () -> {
            var l=ledger();var c=crew(l);l.captain(c,UUID.randomUUID(),true);
            for(int i=0;i<8;i++)member(l,c);
            var m=l.member(c.members.iterator().next());m.resentment=90;m.ambition=90;
            expect(l.startConspiracy(c,1000));expect(!l.beginUltimatum(c,1000));
        });
        check("TC-SOC-01_EIGHT_REQUIRE_FOUR", () -> {
            var l=ledger();var c=crew(l);l.captain(c,UUID.randomUUID(),true);
            for(int i=0;i<8;i++) {var m=member(l,c);m.ambition=90;m.resentment=i<3?90:0;}
            expect(l.requiredRebels(c)==4);expect(l.startConspiracy(c,1000));expect(!l.beginUltimatum(c,1000));
            l.member(new ArrayList<>(c.members).get(3)).resentment=90;expect(l.beginUltimatum(c,1000));
        });
        check("TC-SOC-02_CAPTAIN_NOT_SUPPORTER", () -> {
            var l=ledger();var c=crew(l);var m=member(l,c);m.resentment=99;l.captain(c,m.id,false);
            member(l,c);expect(l.conspirators(c)==0);
        });
        check("TC-SOC-06_MURDER_CLEARS_OFFICE", () -> {
            var l=ledger();var c=crew(l);var m=member(l,c);l.officer(c,m.id,CrewLedger.Role.FIRST_MATE);
            l.murder(m.id,UUID.randomUUID(),1000);expect(c.firstMate==null && !c.members.contains(m.id));
        });
        check("TC-SAVE-02_ORDER_TIMESTAMPS_ROUNDTRIP", () -> {
            Path f=Files.createTempDirectory("pmca-order-").resolve("crews.properties");
            var l=new CrewLedger(f);var c=crew(l);var m=member(l,c);
            m.lastProvocation=900;l.order(m,CrewLedger.Order.HOLD,1000);l.save();
            var copy=new CrewLedger(f).member(m.id);expect(copy.lastOrder==1000 && copy.lastProvocation==900);
        });
        check("TC-SAVE-02_UNSUPPORTED_SCHEMA_FAILS_CLOSED", () -> {
            Path f=Files.createTempDirectory("pmca-schema-").resolve("crews.properties");Files.writeString(f,"schema=999\n");
            try { new CrewLedger(f);throw new AssertionError("silently accepted schema"); }
            catch(IllegalStateException expected) {expect(Files.readString(f).equals("schema=999\n"));}
        });
        check("TC-SOC-09_128_EVENTS_ROUNDTRIP", () -> {
            Path f=Files.createTempDirectory("pmca-events-").resolve("crews.properties");var l=new CrewLedger(f);var c=crew(l);
            for(int i=0;i<130;i++)l.event(c,"EVENT_"+i);l.save();var copy=new CrewLedger(f).crew(c.id);
            expect(copy.events.size()==128 && copy.events.getFirst().equals("EVENT_2") && copy.events.getLast().equals("EVENT_129"));
        });
        System.out.println("Ledger safety: "+passed+" PASS, "+failed+" FAIL");
        if(failed>0)System.exit(1);
    }
}
