package ch.swisssmp.weaver;

import ch.swisssmp.city.SigilRingInfo;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.List;

public class EventListener implements Listener {


    @EventHandler
    private void onBannerPut(InventoryClickEvent event){
        Inventory inventory = event.getClickedInventory();
        if(!(inventory instanceof PlayerInventory)) return;
        if(!event.getSlotType().equals(InventoryType.SlotType.ARMOR)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack banner = event.getCursor();
        if(!CityBanner.isBanner(banner, player)) return;
        event.setCancelled(true);

        ItemStack helmet = ((PlayerInventory) inventory).getHelmet();
        ((PlayerInventory) inventory).setHelmet(banner);

        event.setCurrentItem(helmet);
    }

    @EventHandler
    private void onBannerRegister(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack ring = event.getItem();
        SigilRingInfo ringInfo = SigilRingInfo.get(ring);
        if(ringInfo == null) return;
        Player player = event.getPlayer();
        if(!ringInfo.getOwner().getUniqueId().equals(player.getUniqueId())) return;
        if(!player.hasPermission("weaver.setbanner")) return;

        if(!(event.getClickedBlock() instanceof Banner)) return;
        Banner banner = (Banner) event.getClickedBlock();
        List<Pattern> patterns = banner.getPatterns();
        DyeColor baseColor = banner.getBaseColor();

        ItemStack bannerStack = new ItemStack(Material.BLACK_BANNER);
        BannerMeta bannerStackMeta = (BannerMeta) bannerStack.getItemMeta();
        bannerStackMeta.setBaseColor(baseColor); //use MaterialData, but Materialdata is subject to removal. BlockData replaces it ...
        bannerStackMeta.setPatterns(patterns);

        bannerStack.setItemMeta(bannerStackMeta);
        CityBanner.registerBanner(bannerStack, ringInfo.getCity());

    }
}
