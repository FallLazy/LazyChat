package dev.falllazy.lazyChat.api.tools;

public class FeatureChecker {
    public static class Bubble {
        public static boolean isSupported() {
            try {
                Class.forName("import org.bukkit.entity.TextDisplay");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }
}
