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

    public String getUTC5MinuteEpochBlock() {
        long epochMinutes = System.currentTimeMillis() / 60000; // ms → minutes
        long block = epochMinutes / 5; // group into 5-minute chunks
        return String.valueOf(block);
    }

    public String generateSHA256(String message) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.reset();
        sha256.update(message.getBytes());
        byte[] digest = sha256.digest();
        return String.format("%0" + (digest.length << 1) + "x", new Object[] { new BigInteger(1, digest) });
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

        if(args.length == 0) {
            // If no arguments are provided, show the help message
            player.sendMessage(ChatColor.YELLOW + "Please specify a service. eg. /jcode capes");
            return true;
        }

        String service = args[0].toLowerCase();

        if(!sender.hasPermission("jcode.code." + service) && !sender.isOp()) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this service.");
            return true;
        }

        try {
            String codeString = player.getUniqueId().toString() + ":" + config.getConfigString("settings.key.value") + ":" + getUTC5MinuteEpochBlock() + ":" + service;
            String code = generateSHA256(codeString).substring(0, 6);

            player.sendMessage(ChatColor.GREEN + "Your code for the service '" + service + "' is: " + ChatColor.GOLD + code);
            player.sendMessage(ChatColor.YELLOW + "This code is valid for 5 minutes and can be used to access the service.");
        } catch (NoSuchAlgorithmException e) {
            player.sendMessage(ChatColor.RED + "An error occurred while generating your code. Please try again later.");
            throw new RuntimeException(e);
        }

        return true;
    }
}
