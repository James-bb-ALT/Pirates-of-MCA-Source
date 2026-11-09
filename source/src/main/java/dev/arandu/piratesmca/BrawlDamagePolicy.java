package dev.arandu.piratesmca;

/** Pure policy; the runtime must prove source attribution before applying it. */
public final class BrawlDamagePolicy {
    private BrawlDamagePolicy() {}
    public static float cap(float health,float maxHealth,float incoming,boolean brawlPair) {
        if(!brawlPair)return incoming;
        return Math.max(0f,Math.min(incoming,health-maxHealth*0.25f));
    }
}
