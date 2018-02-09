package ch.swisssmp.event.remotelisteners;

import org.bukkit.event.Event;

import ch.swisssmp.adventuredungeons.event.DungeonEvent;
import ch.swisssmp.adventuredungeons.world.Dungeon;
import ch.swisssmp.event.remotelisteners.filter.DungeonFilter;
import ch.swisssmp.event.remotelisteners.filter.WorldFilter;
import ch.swisssmp.utils.ConfigurationSection;

public class DungeonEventListener extends BasicEventListener implements DungeonFilter,WorldFilter{

	public DungeonEventListener(ConfigurationSection dataSection) {
		super(dataSection);
	}

	@Override
	public void trigger(Event event) {
		if(!(event instanceof DungeonEvent)) return;
		DungeonEvent dungeonEndEvent = (DungeonEvent) event;
		if(!checkDungeon(this.dataSection, dungeonEndEvent)) return;
		if(!checkWorld(this.dataSection, dungeonEndEvent.getInstance().getWorld())) return;
		super.trigger(event);
	}
	
	@Override
	protected String insertArguments(String command, Event event){
		command = super.insertArguments(command, event);
		DungeonEvent dungeonEvent = (DungeonEvent) event;
		command = command.replace("{Dungeon-ID}", String.valueOf(dungeonEvent.getDungeonId()));
		if(command.contains("{Dungeon}")){
			Dungeon dungeon = Dungeon.get(dungeonEvent.getDungeonId());
			command = command.replace("{Dungeon}", dungeon.name);
		}
		command = command.replace("{Instance-ID}", String.valueOf(dungeonEvent.getInstanceId()));
		command = command.replace("{World}", dungeonEvent.getInstance().getWorld().getName());
		return command;
	}
	
}
