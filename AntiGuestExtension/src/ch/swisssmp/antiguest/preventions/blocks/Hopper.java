package ch.swisssmp.antiguest.preventions.blocks;

import org.bukkit.block.Block;

public class Hopper extends BlockInteractPrevention {

    @Override
    protected boolean isMatch(Block block) {
        return block.getState() instanceof org.bukkit.block.Hopper;
    }

    @Override
    protected String getSubPermission() {
        return "jukebox";
    }
}
