

package dev.tbm00.spigot.market64.command;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import dev.tbm00.spigot.market64.StaticUtil;
import dev.tbm00.spigot.market64.Market64;
import dev.tbm00.spigot.market64.StallHandler;
import dev.tbm00.spigot.market64.data.Stall;
import dev.tbm00.spigot.market64.gui.MainGui;
import dev.tbm00.spigot.market64.gui.StallGui;

public class StallCmd implements TabExecutor {
    private final Market64 javaPlugin;
    private final StallHandler stallHandler;

    public StallCmd(Market64 javaPlugin, StallHandler stallHandler) {
        this.javaPlugin = javaPlugin;
        this.stallHandler = stallHandler;
    }

    /**
     * Handles the /stall command.
     * 
     * @param sender the command sender
     * @param consoleCommand the command being executed
     * @param alias the alias used for the command
     * @param args the arguments passed to the command
     * @return true if the command was handled successfully, false otherwise
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            StaticUtil.sendMessage(sender, "&cThis command cannot be run through the console!");
            return true;
        } else if (!StaticUtil.hasPermission(sender, StaticUtil.PLAYER_PERM)) {
            StaticUtil.sendMessage(sender, "&cNo permission!");
            return true;
        }

        if (args.length==0) return handleMainGuiCmd(sender);

        String subCmd = args[0].toLowerCase();
        switch (subCmd) {
            case "help":
                return handleHelpCmd(sender);
            case "rent":
                return handleRentCmd(sender, args);
            case "renew":
                return handleRenewCmd(sender, args);
            case "abandon":
                return handleAbandonCmd(sender, args);
            default:
                return handleSearchCmd(sender, args);
        }
    }
    
    /**
     * Handles the sub command for the help menu.
     * 
     * @param sender the command sender
     * @return true after displaying help menu
     */
    private boolean handleHelpCmd(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + "--- " + ChatColor.AQUA + "Stall Commands" + ChatColor.DARK_AQUA + " ---\n"
            + ChatColor.WHITE + "/stalls" + ChatColor.GRAY + " Open the main Stall GUI\n"
            + ChatColor.WHITE + "/stall <id>" + ChatColor.GRAY + " Open the stall's GUI\n"
            + ChatColor.WHITE + "/stall rent <id>" + ChatColor.GRAY + " Rent a stall for a week, renews automatically if you have enough money in your pocket\n"
            + ChatColor.WHITE + "/stall renew" + ChatColor.GRAY + " Renew your stall early\n"
            + ChatColor.WHITE + "/stall abandon" + ChatColor.GRAY + " Abandon your stall\n"
        );
        return true;
    }

    private boolean handleRentCmd(CommandSender sender, String[] args) {
        Player player;
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            StaticUtil.sendMessage(sender, "&cCommand must be ran by a player!");
            return true;
        }

        if (args.length<2) {
            StaticUtil.sendMessage(sender, "&cYou must provide a stall ID to rent!");
            return false;
        } 

        int id;
        try {
            id = Integer.valueOf(args[1]);
        } catch (Exception e) {
            StaticUtil.sendMessage(sender, "&cCould not parse ID '"+args[1]+"'!");
            return true;
        }

        Stall stall = stallHandler.getStall(id);
        if (stallHandler.fillStall(stall, player)) {
            StaticUtil.sendMessage(sender, "&aRented stall "+id+"! &eYour stall will automatically renew after "+stall.getRentalTimeDays()+" days ("+stall.getEvictionDate()+"), as long as you have $" + StaticUtil.formatInt(stall.getRenewalPrice()) +" in your pocket.");
        } else {
            StaticUtil.sendMessage(sender, "&aFailed to rent stall "+id+"!");
        }
        return true;
    }

    private boolean handleRenewCmd(CommandSender sender, String[] args) {
        Player player;
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            StaticUtil.sendMessage(sender, "&cCommand must be ran by a player!");
            return true;
        }

        Stall stall = null;
        Integer id=null;
        if (args.length<2) {
            for (Stall s : stallHandler.getStalls()) {
                if (s==null) continue;
                if (s.isRented() && s.getRenterUuid().equals(player.getUniqueId())) {
                    id = s.getId();
                    stall = s;
                    break;
                }
            }
            if (id==null) {
                StaticUtil.sendMessage(player, "&cCould not find your stall, do you really have one?");
                return true;
            }
        } else {
            try {
                id = Integer.valueOf(args[1]);
                stall = stallHandler.getStall(id);
            } catch (Exception e) {
                StaticUtil.sendMessage(player, "&cCould not parse ID '"+args[1]+"'!");
                return true;
            }
        }

        if (stall==null) {
            StaticUtil.sendMessage(player, "&cCould not find your stall, do you really have one?");
            return true;
        }

        if (!stall.getRenterUuid().equals(player.getUniqueId())) {
            StaticUtil.sendMessage(player, "&cStall "+ id +"is not yours!");
            return true;
        }

        if (stallHandler.renewStall(stall, false)) {
            StaticUtil.sendMessage(player, "&aRenewed stall "+stall.getId()+"! &eYour stall will automatically renew on "+stall.getEvictionDate()+", as long as you have $" + StaticUtil.formatInt(stall.getRenewalPrice()) +" in your pocket.");
        } else {
            StaticUtil.sendMessage(player, "&aFailed to renew stall!");
        }
        return true;
    }

    private boolean handleAbandonCmd(CommandSender sender, String[] args) {
        Player player;
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            StaticUtil.sendMessage(sender, "&cCommand must be ran by a player!");
            return true;
        }

        Stall stall=null;
        Integer id=null;
        if (args.length<2) {
            for (Stall s : stallHandler.getStalls()) {
                if (s==null) continue;
                if (s.isRented() && s.getRenterUuid().equals(player.getUniqueId())) {
                    id = s.getId();
                    stall = s;
                    break;
                }
            }
            if (id==null) {
                StaticUtil.sendMessage(player, "&cCould not find your stall, do you really have one?");
                return true;
            }
        } else {
            try {
                id = Integer.valueOf(args[1]);
                stall = stallHandler.getStall(id);
            } catch (Exception e) {
                StaticUtil.sendMessage(player, "&cCould not parse ID '"+args[1]+"'!");
                return true;
            }
        }

        if (stall==null) {
            StaticUtil.sendMessage(player, "&cCould not find your stall, do you really have one?");
            return true;
        }

        if (!stall.getRenterUuid().equals(player.getUniqueId())) {
            StaticUtil.sendMessage(player, "&cStall "+ id +"is not yours!");
            return true;
        }

        if (stallHandler.clearStall(stall, "player abandoned", false)) {
            StaticUtil.sendMessage(sender, "&aAbandoned stall "+id+"!");
        } else {
            StaticUtil.sendMessage(sender, "&aFailed to abandon stall "+id+"!");
        }
        return true;
    }

    private boolean handleMainGuiCmd(CommandSender sender) {
        Player player;
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            StaticUtil.sendMessage(sender, "&cCommand must be ran by a player!");
            return true;
        }

        new MainGui(javaPlugin, stallHandler, stallHandler.getStalls(), player);
        return true;
    }

    private boolean handleSearchCmd(CommandSender sender, String[] args) {
        Player player;
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            StaticUtil.sendMessage(sender, "&cCommand must be ran by a player!");
            return true;
        }

        Stall stall=null;
        Integer id=null;
        try {
            id = Integer.valueOf(args[0]);
            stall = stallHandler.getStall(id);
        } catch (Exception e) {
            StaticUtil.sendMessage(player, "&cCould not parse ID '"+args[0]+"'!");
            return true;
        }

        new StallGui(javaPlugin, stallHandler, stall, player);
        return true;
        
    }

    /**
     * Handles tab completion for the /stall command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.clear();
            String[] subCmds = new String[]{"help","rent","renew","abandon","<id>"};
            for (String n : subCmds) {
                if (n!=null && n.startsWith(args[0])) 
                    list.add(n);
            }
        } else if (args.length == 2) {
            if (args[0].equals("abandon") || args[0].equals("renew")) {
                for (Stall stall : stallHandler.getStalls()) {
                    if (stall==null || !stall.isRented()) continue;
                    if (sender instanceof Player && sender.getName().equalsIgnoreCase(stall.getRenterName()))
                        list.add(String.valueOf(stall.getId()));
                }
            } else if (args[0].equals("rent")) {
                for (Stall stall : stallHandler.getStalls()) {
                    if (stall==null || stall.isRented()) continue;
                    if (sender instanceof Player) list.add(String.valueOf(stall.getId()));
                }
            }

        }
        return list;
    }
}