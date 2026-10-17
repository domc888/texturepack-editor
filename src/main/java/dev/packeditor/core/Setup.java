package dev.packeditor.core;

import java.util.LinkedHashMap;
import java.util.Map;

/** A complete set of looks: target id -> look. Missing targets are vanilla. */
public final class Setup {
    private final LinkedHashMap<String, LookSpec> looks = new LinkedHashMap<>();

    public LookSpec get(String targetId) { return looks.getOrDefault(targetId, LookSpec.VANILLA); }

    public void set(String targetId, LookSpec look) {
        if (look == null || look.kind == LookSpec.Kind.VANILLA) looks.remove(targetId);
        else looks.put(targetId, look);
    }

    public void clear() { looks.clear(); }

    public Map<String, LookSpec> entries() { return java.util.Collections.unmodifiableMap(looks); }

    public int size() { return looks.size(); }

    public Setup copy() {
        Setup s = new Setup();
        s.looks.putAll(looks);
        return s;
    }

    /** Number of targets whose look differs between the two setups. */
    public int differences(Setup other) {
        int n = 0;
        for (Target t : Targets.ALL) if (!get(t.id()).equals(other.get(t.id()))) n++;
        return n;
    }

    @Override public boolean equals(Object o) { return o instanceof Setup s && s.looks.equals(looks); }

    @Override public int hashCode() { return looks.hashCode(); }
}
