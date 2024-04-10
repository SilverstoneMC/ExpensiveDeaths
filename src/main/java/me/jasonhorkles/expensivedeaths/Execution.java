package me.jasonhorkles.expensivedeaths;

import com.google.common.base.Suppliers;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public abstract class Execution {
    private static final Supplier<Boolean> USE_PLACEHOLDERAPI = Suppliers.memoize(() -> Bukkit
        .getPluginManager().isPluginEnabled("PlaceholderAPI"));
    private static final Pattern KEY_CHANCE = Pattern.compile("(?i)(test-?)?(chance|prob(ability)?)");
    private static final Pattern KEY_CANCEL = Pattern.compile("(?i)break|stop|cancel(ling)?");
    private static final Pattern KEY_PERMISSION = Pattern.compile("(?i)(meet-?)?perm(ission)?");
    private static final Pattern KEY_EXECUTION = Pattern.compile(
        "(?i)run|(run-?)?(cmds?|commands?)|execut(e|ions?)");

    public static Execution of(Object object) {
        if (object instanceof String) return new SimpleExecution((String) object);
        else if (object instanceof Iterable) {
            final List<Execution> executions = new ArrayList<>();
            for (Object o : (Iterable<?>) object) {
                final Execution execution = of(o);
                if (execution != null) executions.add(execution);
            }
            if (!executions.isEmpty()) return new AdvancedExecution(0.0, false, null, executions);
        } else if (object instanceof Map) {
            double chance = 0.0;
            boolean cancelling = false;
            String permission = null;
            final List<Execution> executions = new ArrayList<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                final String key = String.valueOf(entry.getKey());
                final Object value = entry.getValue();
                if (KEY_CHANCE.matcher(key).matches()) try {
                    chance = Double.parseDouble(String.valueOf(value));
                } catch (NumberFormatException ignored) {
                }
                else if (KEY_CANCEL.matcher(key).matches())
                    cancelling = String.valueOf(value).equalsIgnoreCase("true");
                else if (KEY_PERMISSION.matcher(key).matches()) permission = String.valueOf(value);
                else if (KEY_EXECUTION.matcher(key).matches()) {
                    final Execution execution = of(value);
                    if (execution != null) executions.add(execution);
                }
            }
            if (!executions.isEmpty()) return new AdvancedExecution(chance,
                cancelling,
                permission,
                executions);
        }
        return null;
    }

    public void run(Player player, Player agent, Function<String, String> parser, boolean console) {
        run(console ? Bukkit.getConsoleSender() : player, player, agent, parser);
    }

    public abstract boolean run(CommandSender sender, Player player, Player agent, Function<String, String> parser);

    public void run(CommandSender sender, Player player, Player agent, String cmd, Function<String, String> parser) {
        String s = parser.apply(cmd);
        if (USE_PLACEHOLDERAPI.get() && ExpensiveDeaths.getInstance().getConfig().getBoolean(
            "bonus.parse-placeholders")) {
            s = PlaceholderAPI.setPlaceholders(player, s);
            if (agent != null) s = PlaceholderAPI.setBracketPlaceholders(agent, s);
        }
        Bukkit.dispatchCommand(sender, s);
    }

    public static class SimpleExecution extends Execution {
        private final String cmd;

        public SimpleExecution(String cmd) {
            this.cmd = cmd;
        }

        @Override
        public boolean run(CommandSender sender, Player player, Player agent, Function<String, String> parser) {
            this.run(sender, player, agent, cmd, parser);
            return false;
        }
    }

    public static class AdvancedExecution extends Execution {
        private final double chance;
        private final boolean cancelling;
        private final String permission;
        private final List<Execution> executions;

        public AdvancedExecution(double chance, boolean cancelling, String permission, List<Execution> executions) {
            this.chance = chance;
            this.cancelling = cancelling;
            this.permission = permission;
            this.executions = executions;
        }

        public boolean isCancelling() {
            return cancelling;
        }

        public boolean testChance() {
            return this.chance == 0.0 || ThreadLocalRandom.current().nextDouble() <= this.chance;
        }

        public boolean meetPermission(Player player) {
            if (this.permission != null) for (String s : this.permission.split(";"))
                if (!player.hasPermission(s.trim())) return false;
            return true;
        }

        @Override
        public boolean run(CommandSender sender, Player player, Player agent, Function<String, String> parser) {
            if (!testChance() || !meetPermission(player)) return false;

            for (Execution execution : executions)
                if (execution.run(sender, player, agent, parser)) break;

            return isCancelling();
        }
    }

    public enum Type {
        DEATH_PLAYER, DEATH_CONSOLE, KILL_PLAYER, KILL_CONSOLE, RESPAWN_PLAYER, RESPAWN_CONSOLE;

        public boolean isConsole() {
            return this == DEATH_CONSOLE || this == KILL_CONSOLE || this == RESPAWN_CONSOLE;
        }
    }
}
