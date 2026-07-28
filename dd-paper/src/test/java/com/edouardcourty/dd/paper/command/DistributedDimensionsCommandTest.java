package com.edouardcourty.dd.paper.command;

import com.edouardcourty.dd.common.model.Dimension;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DistributedDimensionsCommandTest {

    @Test
    void testCommandOutput() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        FileConfiguration config = mock(FileConfiguration.class);
        PluginDescriptionFile description = mock(PluginDescriptionFile.class);
        
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getDescription()).thenReturn(description);
        when(description.getVersion()).thenReturn("1.0.0");
        
        when(config.getBoolean("transfer-data", true)).thenReturn(true);
        when(config.getBoolean("debug", false)).thenReturn(false);
        
        DistributedDimensionsCommand commandExecutor = new DistributedDimensionsCommand(plugin, Dimension.NETHER);
        
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("distributeddimensions.admin")).thenReturn(true);
        Command command = mock(Command.class);
        
        boolean result = commandExecutor.onCommand(sender, command, "ddinfo", new String[0]);
        
        assertTrue(result);
        verify(sender).sendMessage("§b=== DistributedDimensions Info ===");
        verify(sender).sendMessage("§7Version: §a1.0.0");
        verify(sender).sendMessage("§7Current Dimension: §eNETHER");
        verify(sender).sendMessage("§7Data Transfer: §aEnabled");
        verify(sender).sendMessage("§7Debug Mode: §cDisabled");
    }
    
    @Test
    void testCommandNoPermission() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        DistributedDimensionsCommand commandExecutor = new DistributedDimensionsCommand(plugin, Dimension.OVERWORLD);
        
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("distributeddimensions.admin")).thenReturn(false);
        Command command = mock(Command.class);
        
        boolean result = commandExecutor.onCommand(sender, command, "ddinfo", new String[0]);
        
        assertTrue(result);
        verify(sender).hasPermission("distributeddimensions.admin");
        verify(sender).sendMessage("§cYou do not have permission to use this command.");
        verifyNoMoreInteractions(sender);
    }
}
