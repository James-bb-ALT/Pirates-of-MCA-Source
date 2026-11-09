package dev.arandu.piratesmca;

import java.util.*;

/** Server-owned causal event; knowledge and each consequence are idempotent by event ID. */
public final class SocialEvent {
    public final UUID id,crew,actor,target;
    public final String kind,reason;
    public final long tick;
    public final int severity;
    public final Set<UUID> witnesses=new LinkedHashSet<>();
    public final Set<String> appliedConsequences=new LinkedHashSet<>();
    public SocialEvent(UUID id,UUID crew,String kind,long tick,UUID actor,UUID target,String reason,int severity,Collection<UUID> witnesses) {
        this.id=Objects.requireNonNull(id);this.crew=Objects.requireNonNull(crew);
        this.kind=Objects.requireNonNull(kind);this.actor=actor;this.target=target;
        if(!kind.matches("[A-Z_]{1,64}"))throw new IllegalArgumentException("Invalid event kind");
        this.tick=Math.max(0,tick);this.reason=Objects.requireNonNull(reason);
        if(reason.length()>2048)throw new IllegalArgumentException("Event reason too long");
        this.severity=Math.clamp(severity,0,100);this.witnesses.addAll(witnesses);
    }
}
