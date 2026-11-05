package icy.lib.client.anim;

import java.util.HashMap;
import java.util.Map;

public final class Anim {

    private Anim() {}

    public static float clamp01(float t) {
        return t < 0f ? 0f : (t > 1f ? 1f : t);
    }

    public static float easeOutCubic(float t) {
        t = clamp01(t);
        float u = 1f - t;
        return 1f - u * u * u;
    }

    public static final class Lift {
        private final Map<Object, Float> current = new HashMap<>();
        private long lastMs = -1L;
        private float factor = 1f;

        public void beginFrame(float tauSec) {
            long now = System.currentTimeMillis();
            float dt = lastMs < 0L ? 0.016f : Math.min((now - lastMs) / 1000f, 0.05f);
            lastMs = now;
            factor = 1f - (float) Math.exp(-dt / Math.max(1.0e-4f, tauSec));
        }

        public float get(Object key, boolean active, float liftPx) {
            float target = active ? liftPx : 0f;
            float c = current.getOrDefault(key, 0f);
            c += (target - c) * factor;
            if (!active && c < 0.03f) {
                current.remove(key);
                return 0f;
            }
            current.put(key, c);
            return c;
        }

        public float peek(Object key) {
            return current.getOrDefault(key, 0f);
        }
    }
}
