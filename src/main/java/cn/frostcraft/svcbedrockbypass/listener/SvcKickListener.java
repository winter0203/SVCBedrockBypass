package cn.frostcraft.svcbedrockbypass.listener;

import cn.frostcraft.svcbedrockbypass.SVCBedrockBypassPlugin;
import cn.frostcraft.svcbedrockbypass.detection.BedrockPlayerDetector;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public final class SvcKickListener implements Listener {

    private final SVCBedrockBypassPlugin plugin;
    private final BedrockPlayerDetector bedrockPlayerDetector;
    private final SvcKickReasonMatcher kickReasonMatcher;

    public SvcKickListener(
            SVCBedrockBypassPlugin plugin,
            BedrockPlayerDetector bedrockPlayerDetector,
            SvcKickReasonMatcher kickReasonMatcher
    ) {
        this.plugin = plugin;
        this.bedrockPlayerDetector = bedrockPlayerDetector;
        this.kickReasonMatcher = kickReasonMatcher;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        if (!plugin.isBypassEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (!bedrockPlayerDetector.isBedrockPlayer(player)) {
            return;
        }

        String reason = event.getReason();
        if (!kickReasonMatcher.matches(reason)) {
            if (plugin.isDebugEnabled()) {
                plugin.getLogger().info("未拦截基岩版玩家 " + player.getName()
                        + " 的踢出事件；原因不匹配 Simple Voice Chat 强制检查规则：" + reason);
            }
            return;
        }

        event.setCancelled(true);
        plugin.getLogger().info("已放行基岩版玩家 " + player.getName()
                + "：已取消 Simple Voice Chat 强制安装检查的踢出事件。");
    }
}
