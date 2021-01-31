package fr.bbrassart;

import fr.bbrassart.util.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Supplier;

public class EditSign extends JavaPlugin implements Listener {

    private static EditSign instance;

    public static EditSign getInstance() {
        return instance;
    }

    private static final Map<Integer, Supplier<? extends EditSignUtils>> versions = new TreeMap<>(Comparator.reverseOrder());

    static {
        registerVersion(8, EditSignUtils8::new);
        registerVersion(12, EditSignUtils12::new);
        registerVersion(13, EditSignUtils13::new);
        registerVersion(14, EditSignUtils14::new);
        registerVersion(15, EditSignUtils15::new);
    }

    public static void registerVersion(int version, Supplier<? extends EditSignUtils> supplier) {
        versions.put(version, supplier);
    }

    public static Supplier<? extends EditSignUtils> getMostSuitableUtils(int serverVersion) {
        List<Map.Entry<Integer, Supplier<? extends EditSignUtils>>> versions = new ArrayList<>(EditSign.versions.entrySet());

        Map.Entry<Integer, Supplier<? extends EditSignUtils>> it;
        Supplier<? extends EditSignUtils> supplier = null;
        int i = 0;

        while (supplier == null && i < versions.size()) {
            it = versions.get(i);

            if (it.getKey() <= serverVersion) {
                supplier = it.getValue();
            }

            i++;
        }

        return supplier;
    }

    private EditSignUtils utils;

    @Override
    public void onEnable() {
        instance = this;

        reload();

        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    public void reload() {
        setUtils(getMostSuitableUtils(EditSignUtils.getVersionNumber()).get());
    }

    public EditSignUtils getUtils() {
        return utils;
    }

    public void setUtils(EditSignUtils utils) {
        this.utils = utils;
    }
}
