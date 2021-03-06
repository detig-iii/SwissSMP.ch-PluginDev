package ch.swisssmp.dungeongenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;

public class ObstructedGenerationPart extends GenerationPart{

	private final int partSizeXZ;
	private final int partSizeY;
	
	private boolean debug = false; //for debugging set to true
	
	public ObstructedGenerationPart(BlockVector gridPosition, DungeonFloor floor, int partSizeXZ, int partSizeY) {
		super(null, gridPosition, floor);
		this.partSizeXZ = partSizeXZ;
		this.partSizeY = partSizeY;
	}
	/**
	 * Generate nothing
	 */
	@Override
	public void generate(World world, BlockVector position){
		if(!this.debug) return;
		for(Block block : BlockUtil.getBox(world.getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ()), this.partSizeXZ, this.partSizeY)){
			if(block.getType()!=Material.AIR && block.getType()!=Material.GLASS) continue;
			block.setType(Material.REDSTONE_BLOCK);
		}
	}
	/**
	 * There is no original
	 */
	@Override
	public GeneratorPart getOriginal(){
		return null;
	}
	/**
	 * Signature is always empty
	 */
	@Override
	public String getSignature(Direction direction){
		return "";
	}
	
	@Override
	public String getInfoString(){
		return "Blockiert";
	}
	
	/**
	 * Nothing to reset in empty part
	 */
	@Override
	public void reset(World world, BlockVector position){
		if(!this.debug) return;
		for(Block block : BlockUtil.getBox(world.getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ()), this.partSizeXZ, this.partSizeY)){
			if(block.getType()!=Material.REDSTONE_BLOCK) continue;
			block.setType(Material.AIR);
		}
	}
	
	@Override
	public boolean typeMatch(PartType...partTypes){
		return false;
	}
	
	@Override
	protected Collection<PartType> getPartTypes(){
		return new ArrayList<PartType>();
	}
	
	protected void findNeighbours(HashMap<BlockVector,GenerationPart> generationData){
		this.setNeighbour(generationData.get(Direction.UP.moveVector(this.getGridPosition())), Direction.UP);
		this.setNeighbour(generationData.get(Direction.DOWN.moveVector(this.getGridPosition())), Direction.DOWN);
		this.setNeighbour(generationData.get(Direction.NORTH.moveVector(this.getGridPosition())), Direction.NORTH);
		this.setNeighbour(generationData.get(Direction.EAST.moveVector(this.getGridPosition())), Direction.EAST);
		this.setNeighbour(generationData.get(Direction.SOUTH.moveVector(this.getGridPosition())), Direction.SOUTH);
		this.setNeighbour(generationData.get(Direction.WEST.moveVector(this.getGridPosition())), Direction.WEST);
	}
}
