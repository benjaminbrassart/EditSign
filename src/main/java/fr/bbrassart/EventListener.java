package fr.bbrassart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EventListener implements Listener {

    private final EditSign pl = EditSign.getInstance();

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        Player p = e.getPlayer();

        if (e.getItem() == null || e.getItem().getType() != pl.getUtils().getMaterial("WOOD_AXE", "WOODEN_AXE", "LEGACY_WOOD_AXE")) {
            if (block != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (!p.isSneaking()) {
                    if (p.hasPermission("editsign.edit")) {
                        if (block.getState() instanceof Sign) {
                            Sign sign = (Sign) block.getState();

                            for (int i = 0; i < 4; i++) {
                                sign.setLine(i, sign.getLine(i).replace('§', '&'));
                            }

                            pl.getUtils().update(sign);

                            // you have to wait at least 2 ticks for the sign actually to update
                            Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(pl, () -> pl.getUtils().openSign(p, sign), 2);

                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void beforePlace(PlayerInteractEvent e) {
        pl.getUtils().beforePlaceSign(e);
    }

    @EventHandler
    public void onEdit(SignChangeEvent e) {
        if (e.getPlayer().hasPermission("signedit.color")) {
            for (int i = 0; i < 4; i++) {
                e.setLine(i, ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(e.getLine(i))));
            }

            pl.getUtils().update(e.getBlock().getState());
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent e) {
        pl.getUtils().closeInventory(e.getPlayer());
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent e) {
        pl.getUtils().clickInventory(e.getWhoClicked());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        pl.getUtils().closeInventory(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMiddleClick(InventoryCreativeEvent e) {
        if (e.getClick() == ClickType.CREATIVE && e.getSlotType() == InventoryType.SlotType.QUICKBAR) {
            ItemStack item = e.getCursor();

            Material oak = pl.getUtils().getMaterial("OAK_SIGN");
            Material spruce = pl.getUtils().getMaterial("SPRUCE_SIGN");
            Material birch = pl.getUtils().getMaterial("BIRCH_SIGN");
            Material jungle = pl.getUtils().getMaterial("JUNGLE_SIGN");
            Material acacia = pl.getUtils().getMaterial("ACACIA_SIGN");
            Material darkOak = pl.getUtils().getMaterial("DARK_OAK_SIGN");
            Material crimson = pl.getUtils().getMaterial("CRIMSON_SIGN");
            Material warped = pl.getUtils().getMaterial("WARPED_SIGN");

            if (item.getAmount() == 1 && (
                    item.getType() == pl.getUtils().getMaterial("SIGN", "LEGACY_SIGN") ||
                            Arrays.asList(oak, spruce, birch, jungle, acacia, darkOak, crimson, warped)
                                    .contains(item.getType())
            )) {
                Player p = (Player) e.getWhoClicked();

                if (!pl.getUtils().isInventoryOpen(p)) {
                    ItemMeta meta = item.getItemMeta();

                    if (meta != null) {
                        List<String> lore = meta.getLore();

                        if (lore != null && lore.size() > 0) {
                            String nbtLine = lore.get(lore.size() - 1);

                            if (Objects.equals(nbtLine, "(+NBT)")) {
                                Block targetBlock = p.getTargetBlock(null, 7);

                                if (targetBlock.getState() instanceof Sign) {
                                    Sign sign = (Sign) targetBlock.getState();
                                    String[] lines = sign.getLines();

                                    meta.setLore(pl.getUtils().getLore(lines));
                                    meta.setDisplayName("§fSign (custom)");
                                    item.setItemMeta(meta);

                                    item = pl.getUtils().setLines(item, lines);
                                    e.setCursor(item);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
