package org.retromc.jcode.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.retromc.jcode.JCodeConfig;
import org.retromc.jcode.JCode;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class JCodeCommand implements CommandExecutor {

    private final JCode plugin;

    private final JCodeConfig config;

    public JCodeCommand(JCode plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }

        if (!sender.hasPermission("jcode.code") && !sender.isOp()) {
            sender.sendMessage("You do not have permission to execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // If no arguments are provided, show the help message
            player.sendMessage(ChatColor.YELLOW + "Please specify a service. eg. /jcode cape");
            return true;
        }

        String service = args[0].toLowerCase();

        if (!sender.hasPermission("jcode.code." + service) && !sender.isOp()) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this service.");
            return true;
        }

        String code = this.plugin.getGenerator().generateCode(player.getUniqueId(), service);

        player.sendMessage(ChatColor.GREEN + "Your code for the service '" + service + "' is: " + ChatColor.GOLD + code);
        player.sendMessage(ChatColor.YELLOW + "This code is valid for 5 minutes and can be used to access the service.");

        return true;
    }
}
