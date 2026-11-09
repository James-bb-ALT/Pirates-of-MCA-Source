import dev.arandu.piratesmca.CrewLedger;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LedgerPersistenceTest {
    @Test void malformedCaptainUuidCannotBecomeAValidVacancy() throws Exception {
        var l=new CrewLedger(Files.createTempDirectory("pmca-captain-uuid-").resolve("crews.properties"));
        var c=l.getOrCreate(UUID.randomUUID(),"VESSEL");var captain=l.add(UUID.randomUUID(),c.id,"friendly");l.captain(c,captain.id,false);
        var state=l.exportState();state.put("crew."+c.id+".captain","broken-uuid");
        assertThrows(IllegalStateException.class,()->CrewLedger.fromState(state));
    }
    @Test void malformedWitnessIdCannotBeDroppedAndPermitDuplicateConsequences() throws Exception {
        var l=new CrewLedger(Files.createTempDirectory("pmca-witness-uuid-").resolve("crews.properties"));
        var c=l.getOrCreate(UUID.randomUUID(),"VESSEL");var m=l.add(UUID.randomUUID(),c.id,"friendly");
        var state=l.exportState();state.put("member."+m.id+".witnessedEvents","broken-uuid");
        assertThrows(IllegalStateException.class,()->CrewLedger.fromState(state));
    }
    @Test void alpha5DuplicateOfficesAreRepairedAndOriginalBackedUp() throws Exception {
        Path file=Files.createTempDirectory("pmca-migration-").resolve("crews.properties");
        var l=new CrewLedger(file);var c=l.getOrCreate(UUID.randomUUID(),"SEAPOST");
        var m=l.add(UUID.randomUUID(),c.id,"greedy");l.officer(c,m.id,CrewLedger.Role.QUARTERMASTER);l.save();
        Properties p=new Properties();try(var in=Files.newInputStream(file)){p.load(in);}
        p.setProperty("crew."+c.id+".mate",m.id.toString());
        try(var out=Files.newOutputStream(file)){p.store(out,"Alpha 5 duplicate office fixture");}
        byte[] original=Files.readAllBytes(file);var migrated=new CrewLedger(file);
        assertNull(migrated.crew(c.id).firstMate);assertEquals(m.id,migrated.crew(c.id).quartermaster);
        migrated.save();assertArrayEquals(original,Files.readAllBytes(file.resolveSibling("crews.properties.pre-alpha6.bak")));
        var reloaded=new CrewLedger(file);assertNull(reloaded.crew(c.id).firstMate);
        assertEquals(CrewLedger.Role.QUARTERMASTER,reloaded.member(m.id).role);
    }
    @Test void failedWritePreservesPreviousSnapshotAndCanBeRetried() throws Exception {
        Path file=Files.createTempDirectory("pmca-save-failure-").resolve("crews.properties");
        var l=new CrewLedger(file);var c=l.getOrCreate(UUID.randomUUID(),"SEAPOST");l.save();
        byte[] previous=Files.readAllBytes(file);var m=l.add(UUID.randomUUID(),c.id,"friendly");
        Path blocked=file.resolveSibling("crews.properties.tmp");Files.createDirectory(blocked);
        assertThrows(UncheckedIOException.class,l::save);assertArrayEquals(previous,Files.readAllBytes(file));
        Files.delete(blocked);l.save();assertNotNull(new CrewLedger(file).member(m.id));
    }
    @Test void corruptRosterCannotSilentlyOverwriteWorldState() throws Exception {
        Path file=Files.createTempDirectory("pmca-roster-").resolve("crews.properties");
        UUID crew=UUID.randomUUID(),missing=UUID.randomUUID();
        String original="schema=2\ncrew."+crew+".members="+missing+"\n";Files.writeString(file,original);
        assertThrows(IllegalStateException.class,()->new CrewLedger(file));assertEquals(original,Files.readString(file));
    }
    @Test void missingNpcCaptainCannotBecomeASilentVacancy() throws Exception {
        Path file=Files.createTempDirectory("pmca-captain-corrupt-").resolve("crews.properties");
        UUID crew=UUID.randomUUID(),missing=UUID.randomUUID();
        String original="schema=2\ncrew."+crew+".captain="+missing+"\n";Files.writeString(file,original);
        assertThrows(IllegalStateException.class,()->new CrewLedger(file));assertEquals(original,Files.readString(file));
    }
    @Test void orphanMemberCannotBeDiscardedDuringNextSave() throws Exception {
        Path file=Files.createTempDirectory("pmca-orphan-").resolve("crews.properties");
        UUID crew=UUID.randomUUID(),member=UUID.randomUUID();
        String original="schema=2\nmember."+member+".crew="+crew+"\n";Files.writeString(file,original);
        assertThrows(IllegalStateException.class,()->new CrewLedger(file));assertEquals(original,Files.readString(file));
    }
}
