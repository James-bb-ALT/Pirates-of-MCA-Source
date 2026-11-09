import dev.arandu.piratesmca.CrewLedger;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderStateTest {
    @Test void targetAndAnchorSurviveSaveAndDismissClearsThem() throws Exception {
        var file=Files.createTempDirectory("pmca-orders-").resolve("crews.properties");
        var l=new CrewLedger(file);var c=l.getOrCreate(UUID.randomUUID(),"ORDERS");
        var m=l.add(UUID.randomUUID(),c.id,"friendly");
        var directive=new CrewLedger.Directive("minecraft:overworld",1.25,64,-3.75,UUID.randomUUID());
        l.order(m,CrewLedger.Order.ATTACK,300,directive);l.save();
        var copy=new CrewLedger(file);var restored=copy.member(m.id);
        assertEquals(directive,restored.directive);assertEquals(300,restored.lastOrder);
        copy.order(restored,CrewLedger.Order.NONE,320);copy.save();
        assertNull(new CrewLedger(file).member(m.id).directive);
    }
    @Test void invalidAnchorIsRejected() {
        assertThrows(IllegalArgumentException.class,()->new CrewLedger.Directive("",0,0,0,null));
        assertThrows(IllegalArgumentException.class,()->new CrewLedger.Directive("overworld",Double.NaN,0,0,null));
    }
    @Test void actingCaptainRemainsAnOfficerAndCannotGrantSecondCaptaincy() throws Exception {
        var l=new CrewLedger(Files.createTempDirectory("pmca-acting-").resolve("crews.properties"));
        var c=l.getOrCreate(UUID.randomUUID(),"ACTING");var mate=l.add(UUID.randomUUID(),c.id,"friendly");
        UUID player=UUID.randomUUID();l.captain(c,player,true);l.officer(c,mate.id,CrewLedger.Role.FIRST_MATE);
        l.actingCaptain(c,mate.id);
        assertEquals(player,c.captain);assertTrue(c.playerCaptain);assertEquals(CrewLedger.Role.FIRST_MATE,mate.role);
        l.captain(c,player,true);assertNull(c.actingCaptain);assertEquals(player,c.captain);
    }
    @Test void removingOfficerClearsInterimAuthorityImmediately() throws Exception {
        var l=new CrewLedger(Files.createTempDirectory("pmca-acting-remove-").resolve("crews.properties"));
        var c=l.getOrCreate(UUID.randomUUID(),"ACTING");var mate=l.add(UUID.randomUUID(),c.id,"friendly");
        l.officer(c,mate.id,CrewLedger.Role.FIRST_MATE);l.actingCaptain(c,mate.id);
        l.officer(c,mate.id,CrewLedger.Role.QUARTERMASTER);
        assertNull(c.actingCaptain);assertNull(c.firstMate);
        assertThrows(IllegalArgumentException.class,()->l.actingCaptain(c,mate.id));
    }
}
