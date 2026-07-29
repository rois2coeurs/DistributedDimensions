package com.edouardcourty.dd.paper.command;

import com.edouardcourty.dd.common.model.Dimension;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class DistributedDimensionsCommand implements CommandExecutor {
    
    private final JavaPlugin plugin;
    
    public DistributedDimensionsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("distributeddimensions.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }
        
        boolean transferData = plugin.getConfig().getBoolean("transfer-data", true);
        boolean debug = plugin.getConfig().getBoolean("debug", false);
        
        sender.sendMessage("§b=== DistributedDimensions Info ===");
        sender.sendMessage("§7Version: §a" + plugin.getDescription().getVersion());
        sender.sendMessage("§7Multi-Dimension Mode: §aEnabled");
        sender.sendMessage("§7Data Transfer: " + (transferData ? "§aEnabled" : "§cDisabled"));
        sender.sendMessage("§7Debug Mode: " + (debug ? "§aEnabled" : "§cDisabled"));
        
        return true;
    }
}
