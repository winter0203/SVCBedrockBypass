package cn.frostcraft.svcbedrockbypass.listener;

import cn.frostcraft.svcbedrockbypass.SVCBedrockBypassPlugin;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Matches only kick reasons configured as the forced-client message. */
public final class SvcKickReasonMatcher {

    private final SVCBedrockBypassPlugin plugin;
    private final List<Pattern> patterns;

    public SvcKickReasonMatcher(SVCBedrockBypassPlugin plugin) {
        this.plugin = plugin;
        this.patterns = loadPatterns();
    }

    public boolean matches(String reason) {
        if (reason == null || reason.isBlank()) {
            return false;
        }

        String normalized = ChatColor.stripColor(reason);
        if (normalized == null) {
            normalized = reason;
        }
        normalized = normalized.trim();

        for (Pattern pattern : patterns) {
            if (pattern.matcher(normalized).find()) {
                return true;
            }
        }
        return false;
    }

    private List<Pattern> loadPatterns() {
        List<Pattern> compiled = new ArrayList<>();
        List<String> configuredPatterns = plugin.getConfig().getStringList("kick-reason-patterns");

        for (String expression : configuredPatterns) {
            if (expression == null || expression.isBlank()) {
                continue;
            }
            try {
                compiled.add(Pattern.compile(expression));
            } catch (PatternSyntaxException exception) {
                plugin.getLogger().warning("忽略无效的踢出原因正则表达式：" + expression + "（" + exception.getDescription() + "）");
            }
        }

        if (compiled.isEmpty()) {
            plugin.getLogger().warning("kick-reason-patterns 没有可用规则；插件不会拦截任何踢出事件。请检查 config.yml。");
        }
        return List.copyOf(compiled);
    }
}
