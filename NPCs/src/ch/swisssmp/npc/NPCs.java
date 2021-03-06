package ch.swisssmp.npc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import ch.swisssmp.npc.conversations.NPCConversation;

public class NPCs extends JavaPlugin {
	private static PluginDescriptionFile pdfFile;
	private static NPCs plugin;
	
	protected static boolean debug = false;
	
	@Override
	public void onEnable() {
		plugin = this;
		pdfFile = getDescription();
		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
		Bukkit.getPluginCommand("npc").setExecutor(new NPCCommand());
		
		Bukkit.getLogger().info(pdfFile.getName() + " has been enabled (Version: " + pdfFile.getVersion() + ")");
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		Bukkit.getScheduler().cancelTasks(this);
		NPCConversation.clear();
		PluginDescriptionFile pdfFile = getDescription();
		Bukkit.getLogger().info(pdfFile.getName() + " has been disabled (Version: " + pdfFile.getVersion() + ")");
	}
	
	public static void pack(World world) {
		NPCUnpacker.stop(world);
		NPCPacker.pack(world);
	}
	
	public static void unpack(World world) {
		NPCUnpacker.initialize(world);
	}
	
	protected static String getPrefix() {
		return "["+ChatColor.LIGHT_PURPLE+"NPCs"+ChatColor.RESET+"]";
	}
	
	public static NPCs getInstance(){
		return plugin;
	}
}
