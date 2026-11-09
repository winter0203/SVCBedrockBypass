package cn.frostcraft.svcbedrockbypass;

import cn.frostcraft.svcbedrockbypass.detection.BedrockPlayerDetector;
import cn.frostcraft.svcbedrockbypass.listener.SvcKickListener;
import cn.frostcraft.svcbedrockbypass.listener.SvcKickReasonMatcher;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class SVCBedrockBypassPlugin extends JavaPlugin {

    private BedrockPlayerDetector bedrockPlayerDetector;
    private SvcKickReasonMatcher kickReasonMatcher;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        bedrockPlayerDetector = new BedrockPlayerDetector(this);
        kickReasonMatcher = new SvcKickReasonMatcher(this);

        Bukkit.getPluginManager().registerEvents(
                new SvcKickListener(this, bedrockPlayerDetector, kickReasonMatcher),
                this
        );

        Plugin voiceChat = Bukkit.getPluginManager().getPlugin("voicechat");
        if (voiceChat == null || !voiceChat.isEnabled()) {
            getLogger().warning("未检测到 Simple Voice Chat（插件 ID: voicechat）。本插件已加载，但暂时不会有可拦截的语音检查踢出。");
        } else {
            getLogger().info("已检测到 Simple Voice Chat " + voiceChat.getDescription().getVersion());
        }

        if (!bedrockPlayerDetector.hasAvailableApi()) {
            getLogger().warning("没有检测到可用的 Floodgate/Geyser 服务端 API。基岩版识别不会生效；请确认 Geyser 或 Floodgate 已安装并启用。");
        } else {
            getLogger().info("基岩版识别已启用（Floodgate / Geyser API）。");
        }

        getLogger().info("SVCBedrockBypass v" + getDescription().getVersion() + " 已启用。");
    }

    public boolean isBypassEnabled() {
        return getConfig().getBoolean("enabled", true);
    }

    public boolean isDebugEnabled() {
        return getConfig().getBoolean("debug", false);
    }
}
