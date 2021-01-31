package fr.bbrassart.util;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.json.simple.parser.JSONParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public abstract class EditSignUtils {

    public static final int CHARACTERS_PER_LINE = 15;

    private static final JSONParser parser = new JSONParser();
    private static final BlockFace[] directions = {
            BlockFace.NORTH,
            BlockFace.NORTH_NORTH_EAST,
            BlockFace.NORTH_EAST,
            BlockFace.EAST_NORTH_EAST,
            BlockFace.EAST,
            BlockFace.EAST_SOUTH_EAST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH_SOUTH_EAST,
            BlockFace.SOUTH,
            BlockFace.SOUTH_SOUTH_WEST,
            BlockFace.SOUTH_WEST,
            BlockFace.WEST_SOUTH_WEST,
            BlockFace.WEST,
            BlockFace.WEST_NORTH_WEST,
            BlockFace.NORTH_WEST,
            BlockFace.NORTH_NORTH_WEST,
    };

    public static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public static int getVersionNumber() {
        String ver = getVersion();
        return Integer.parseInt(ver.split("_")[1]);
    }

    private final Set<UUID> inventories = new HashSet<>();

    public final BlockFace yawToFace(float yaw) {
        return directions[Math.round(yaw / 22.5f) & 0xF];
    }

    public final void clickInventory(HumanEntity p) {
        inventories.add(p.getUniqueId());
    }

    public final void closeInventory(HumanEntity p) {
        inventories.remove(p.getUniqueId());
    }

    public final boolean isInventoryOpen(HumanEntity p) {
        return inventories.contains(p.getUniqueId());
    }

    public final Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + getVersion() + "." + name);
        } catch (Exception e) {
            return null;
        }
    }

    public final Class<?> getCraftBukkitClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getVersion() + "." + name);
        } catch (Exception e) {
            return null;
        }
    }

    public final void sendPacket(Object handle, Object packet) {
        try {
            Object connection = handle.getClass().getField("playerConnection").get(handle);
            connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void update(BlockState state) {
        state.update(true, false);
    }

    public abstract void openSign(HumanEntity player, Sign sign);

    public void beforePlaceSign(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        Block block = e.getClickedBlock();

        if (!e.isCancelled() && block != null) {
            if (p.hasPermission("editsign.place-copied")) {
                if (item != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    String[] lines = getLines(item);

                    if (lines.length > 0) {
                        e.setCancelled(true);

                        BlockFace face = e.getBlockFace();
                        block = block.getRelative(face);

                        if (!block.getType().isSolid()) {
                            Material type;
                            Sign sign;
                            BlockFace direction;

                            if (face == BlockFace.DOWN) {
                                return;
                            } else if (face == BlockFace.UP) {
                                type = getMaterial("SIGN_POST", "LEGACY_SIGN_POST", "SIGN");
                                direction = yawToFace(p.getLocation().getYaw());
                            } else {
                                type = getMaterial("WALL_SIGN", "LEGACY_WALL_SIGN");
                                direction = face;
                            }

                            block.setType(type, false);
                            sign = (Sign) block.getState();
                            org.bukkit.material.Sign signData = (org.bukkit.material.Sign) sign.getData();

                            signData.setFacingDirection(direction);
                            sign.setData(signData);

                            for (int i = 0; i < lines.length; i++) {
                                sign.setLine(i, ChatColor.translateAlternateColorCodes('&', lines[i]));
                            }

                            update(sign);
                        }
                    }
                }
            }
        }
    }

    public final boolean isClonedSign(ItemStack item) {
        if (item.getType() == getMaterial("SIGN", "LEGACY_SIGN")) {
            try {
                Object tag = getRootTag(item);

                if (tag != null) {
                    Class<?> nbtTagCompound = tag.getClass();

                    // 9 is the type id of NBTTagList
                    return (boolean) nbtTagCompound.getMethod("hasKeyOfType", String.class, int.class)
                            .invoke(tag, "EditSign", 9);
                }
            } catch (Exception ignored) { }
        }

        return false;
    }

    public String[] getLines(ItemStack item) {
        String[] lines = new String[0];

        if (isClonedSign(item)) {
            lines = new String[4];
            Object tag = getRootTag(item);

            if (tag != null) {
                Class<?> nbtTagCompound = tag.getClass();

                try {
                    Object editSign = nbtTagCompound.getMethod("get", String.class)
                            .invoke(tag, "EditSign");

                    if (editSign != null) {
                        Class<?> nbtTagList = editSign.getClass();
                        Field list = nbtTagList.getDeclaredField("list");

                        list.setAccessible(true);

                        List<?> editSignList = (List<?>) list.get(editSign);

                        if (editSignList != null) {
                            for (int i = 0; i < editSignList.size(); i++) {
                                lines[i] = parser.parse(editSignList.get(i).toString()).toString();
                            }
                        }
                    }
                } catch (Exception ignored) { }
            }
        }

        return lines;
    }

    public ItemStack setLines(ItemStack item, String[] lines) {
        Object copy = getNMSCopy(item);

        if (copy != null) {
            Class<?> copyClass = copy.getClass();
            Object tag = getRootTag(item);

            try {
                if (tag != null) {
                    Class<?> nbtTagCompound = tag.getClass();
                    Class<?> nbtTagList = getNMSClass("NBTTagList");

                    if (nbtTagList != null) {
                        Object tagList = nbtTagList.getConstructor().newInstance();
                        Class<?> nbtTagString = getNMSClass("NBTTagString");
                        Class<?> nbtBase = getNMSClass("NBTBase");

                        if (nbtBase != null) {
                            Method add = nbtTagList.getMethod("add", nbtBase);

                            if (nbtTagString != null) {
                                for (String line : lines) {
                                    add.invoke(tagList, nbtTagString.getConstructor(String.class).newInstance(line));
                                }

                                nbtTagCompound.getMethod("set", String.class, nbtBase)
                                        .invoke(tag, "EditSign", tagList);
                                copyClass.getMethod("setTag", nbtTagCompound).invoke(copy, tag);

                                Class<?> craftItemStack = getCraftBukkitClass("inventory.CraftItemStack");
                                Class<?> nmsItemStack = getNMSClass("ItemStack");

                                if (craftItemStack != null && nmsItemStack != null) {
                                    item = (ItemStack) craftItemStack.getMethod("asCraftMirror", nmsItemStack)
                                            .invoke(null, copy);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) { }
        }

        return item;
    }

    public List<String> getLore(String[] lines) {
        List<String> list = new ArrayList<>();
        int size;
        String space;

        list.add(" §7§m" + StringUtils.repeat(" ", CHARACTERS_PER_LINE * 2 + 1));

        for (String line : lines) {
            size = ChatColor.stripColor(line).length();
            space = StringUtils.repeat(" ", CHARACTERS_PER_LINE - size + 2);

            list.add(space + ChatColor.translateAlternateColorCodes('&', line) + space);
        }

        list.add(" §7§m" + StringUtils.repeat(" ", CHARACTERS_PER_LINE * 2 + 1));

        return list;
    }

    public final Material getMaterial(String... names) {
        Material m = null;
        int i = 0;

        while (m == null && i < names.length) {
            m = Material.getMaterial(names[i++]);
        }

        return m;
    }

    private Object getNMSCopy(ItemStack item) {
        Class<?> craftItemStack = getCraftBukkitClass("inventory.CraftItemStack");

        try {
            if (craftItemStack != null) {
                return craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Object getRootTag(ItemStack item) {
        Object copy = getNMSCopy(item);

        try {
            if (copy != null) {
                Class<?> nmsItemStack = copy.getClass();
                return nmsItemStack.getMethod("getTag").invoke(copy);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
