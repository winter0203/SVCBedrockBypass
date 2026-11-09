package cn.frostcraft.svcbedrockbypass.detection;

import cn.frostcraft.svcbedrockbypass.SVCBedrockBypassPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Detects Bedrock players without linking against optional APIs. */
public final class BedrockPlayerDetector {

    private static final String FLOODGATE_API = "org.geysermc.floodgate.api.FloodgateApi";
    private static final String GEYSER_API = "org.geysermc.geyser.api.GeyserApi";

    private final SVCBedrockBypassPlugin plugin;
    private final Set<String> warnedApis = ConcurrentHashMap.newKeySet();

    public BedrockPlayerDetector(SVCBedrockBypassPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasAvailableApi() {
        return (isEnabledPlugin("floodgate") && plugin.getConfig().getBoolean("bedrock-detection.floodgate", true))
                || (hasEnabledGeyser() && plugin.getConfig().getBoolean("bedrock-detection.geyser", true));
    }

    public boolean isBedrockPlayer(Player player) {
        return isBedrockPlayer(player.getUniqueId());
    }

    public boolean isBedrockPlayer(UUID uuid) {
        if (plugin.getConfig().getBoolean("bedrock-detection.floodgate", true)
                && isEnabledPlugin("floodgate")
                && queryApi(FLOODGATE_API, "getInstance", "isFloodgatePlayer", uuid,
                "Floodgate", "floodgate")) {
            return true;
        }

        if (!plugin.getConfig().getBoolean("bedrock-detection.geyser", true)) {
            return false;
        }
        String geyserPluginName = findEnabledGeyserPlugin();
        return geyserPluginName != null
                && queryApi(GEYSER_API, "api", "isBedrockPlayer", uuid,
                "Geyser", geyserPluginName);
    }

    private boolean queryApi(
            String apiClassName,
            String accessorName,
            String checkMethodName,
            UUID uuid,
            String apiLabel,
            String providerPluginName
    ) {
        try {
            Plugin provider = Bukkit.getPluginManager().getPlugin(providerPluginName);
            if (provider == null || !provider.isEnabled()) {
                return false;
            }
            ClassLoader classLoader = provider.getClass().getClassLoader();
            Class<?> apiClass = Class.forName(apiClassName, true, classLoader);
            Method accessor = apiClass.getMethod(accessorName);
            Object apiInstance = accessor.invoke(null);
            if (apiInstance == null) {
                warnOnce(apiLabel, "API 返回了空实例，无法识别基岩版玩家。");
                return false;
            }

            Method checkMethod = apiClass.getMethod(checkMethodName, UUID.class);
            return Boolean.TRUE.equals(checkMethod.invoke(apiInstance, uuid));
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            warnOnce(apiLabel, "调用 " + apiLabel + " API 失败：" + exception.getClass().getSimpleName()
                    + (exception.getMessage() == null ? "" : " - " + exception.getMessage()));
            return false;
        }
    }

    private boolean hasEnabledGeyser() {
        return findEnabledGeyserPlugin() != null;
    }

    private String findEnabledGeyserPlugin() {
        if (isEnabledPlugin("Geyser-Spigot")) {
            return "Geyser-Spigot";
        }
        if (isEnabledPlugin("Geyser")) {
            return "Geyser";
        }
        return null;
    }

    private boolean isEnabledPlugin(String pluginName) {
        PluginManager manager = Bukkit.getPluginManager();
        Plugin candidate = manager.getPlugin(pluginName);
        return candidate != null && candidate.isEnabled();
    }

    private void warnOnce(String apiLabel, String message) {
        if (warnedApis.add(apiLabel.toLowerCase(Locale.ROOT))) {
            plugin.getLogger().warning(message);
        }
    }
}
