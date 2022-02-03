package ru.tulavcube.Spectate.commands;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import ru.tulavcube.Spectate.Spectate;

import java.util.ArrayList;
import java.util.List;

public class spec implements TabExecutor {
    Spectate plugin;

    public spec(Spectate plg) {
        plugin = plg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (plugin.worldWhitelist.contains(((Player) sender).getLocation().getWorld().getName())) {
                if (!((Player) sender).getScoreboardTags().contains("ru.tulavcube.Spectate:spectating")
                        && ((Player) sender).getGameMode() != GameMode.SPECTATOR)
                    Spectate.turnIntoShadow((Player) sender);
                else
                    Spectate.leaveShadow((Player) sender);

            } else sender.sendMessage(ChatColor.RED + "You can`t use this command here");
        } else sender.sendMessage(ChatColor.RED + "Sorry, this command is only available for players");
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return new ArrayList<>();
    }

}