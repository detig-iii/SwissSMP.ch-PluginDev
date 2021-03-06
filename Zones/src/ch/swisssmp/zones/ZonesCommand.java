package ch.swisssmp.zones;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ZonesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = ZonesPlugin.getPrefix()+" ";
        if(!(sender instanceof Player)){
            sender.sendMessage(prefix+"Kann nur ingame verwendet werden!");
            return true;
        }

        Player player = (Player) sender;

        if(args.length>0){
            String typeString = args[0];
            ZoneType type = ZoneType.findByName(typeString).orElse(null);
            if(type!=null){
                ZonesView.open(player, type);
                return true;
            }
        }

        ZoneTypesView.open(player);
        return true;
    }
}
