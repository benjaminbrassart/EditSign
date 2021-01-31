package fr.bbrassart.util;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public final class EditSignUtils13 extends EditSignUtils {

    @Override
    public void openSign(HumanEntity player, Sign sign) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);

            Field f = sign.getClass().getSuperclass().getDeclaredField("tileEntity");
            f.setAccessible(true);

            Object tileEntity = f.get(sign);
            Field editable = tileEntity.getClass().getField("isEditable");
            editable.setBoolean(tileEntity, true);

            Field owner = tileEntity.getClass().getDeclaredField("g");
            owner.setAccessible(true);
            owner.set(tileEntity, handle);

            Field editor = tileEntity.getClass().getField("signEditor");
            editor.set(tileEntity, player.getUniqueId());

            Location loc = sign.getLocation();
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();

            try {
                Class<?> packetClass = getNMSClass("PacketPlayOutOpenSignEditor");
                Class<?> blockPositionClass = getNMSClass("BlockPosition");

                if (packetClass != null && blockPositionClass != null) {
                    Constructor<?> blockPositionConstructor = blockPositionClass.getConstructor(int.class, int.class, int.class);
                    Constructor<?> packetConstructor = packetClass.getConstructor(blockPositionClass);
                    Object blockPosition = blockPositionConstructor.newInstance(x, y, z);
                    Object packet = packetConstructor.newInstance(blockPosition);

                    sendPacket(handle, packet);
                }
            } catch (Exception ex) {
                player.sendMessage("§cAn error occurred while handling the event. See console for further information.");
                ex.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
