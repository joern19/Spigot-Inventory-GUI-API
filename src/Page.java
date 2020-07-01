package minecraftserver.gui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import minecraftserver.adminTools.Admins;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import utilities.BasicFunctions;

/**
 *
 * @author joern
 */
public class Page {

    private static boolean eventRegisterd = false;
    private static final InstanceManager instanceManager = new InstanceManager();

    private final String name;
    private final ClickableItem[] items;
    
    private LinkedList<ClickableItem> tmpItems = new LinkedList<>(); //thinking about removing this
    private Map<UUID, Object[]> information = new HashMap<>(); // and this.

    public Object[] getInformationFor(UUID playerUUID) {
        return information.get(playerUUID);
    }

    /**
     * This has to be done only once but is very Important to work properly.
     *
     * @param listener
     * @param plugin
     */
    public static synchronized void addEventListener(Listener listener, JavaPlugin plugin) {
        if (eventRegisterd) {
            return;
        }
        eventRegisterd = true;
        Bukkit.getPluginManager().registerEvent(InventoryClickEvent.class, listener, EventPriority.HIGH, (ll, event) -> {
            onInventoryClick((InventoryClickEvent) event);
        }, plugin);
    }

    private ClickableItem getItemById(ItemStack is) {
        for (ClickableItem i : items) {
            if (i.compareKey(is)) {
                return i;
            }
        }
        for (ClickableItem i : tmpItems) {
            if (i.compareKey(is)) {
                return i;
            }
        }
        return null;
    }

    /**
     * Do not forget to add it to the Instance Manager with new Page(name,
     * items).addToInstanceManager This is Important because if you do not do it
     * the ClickEvent can not access the Page.
     *
     * <b>Did you already registered the Event Manager with
     * "Page.addEventListener();" ?</b>
     *
     * @param name It is the Title of the Inventory. The name must be unique
     * across all pages.
     * @param items the ClickableItems that should be present in the Inventory
     */
    public Page(String name, ClickableItem... items) {
        if (instanceManager.existsName(name)) {
            throw new IllegalArgumentException(
                    "The Page with the name \"" + name + "\" already exists. "
                    + "It is used to Identify the Inventory. Please choose a different name");
        }
        this.name = "§r" + name; //The §r infront of the Title is to prevent a Player to create an Inventory with the same name as an Page. 
        //It could couse trouble since the Name is used for identification.
        this.items = items;
    }

    /**
     * This is Important because if you do not add the Page to the
     * InstanceManager, the ClickableItem's will not work.
     */
    public void addToInstanceManager() {
        instanceManager.add(this);
    }

    /**
     * This function is useful if you want to disable all click events from a
     * specific Page.
     */
    public void removeFromInstanceManager() {
        instanceManager.remove(this);
    }

    /**
     * Usually this function is not important please ready my README.md for how
     * to use this.
     *
     * @param ci
     * @return
     */
    public boolean addClickListener(ClickableItem ci) {
        return tmpItems.add(ci);
    }

    /**
     * Usually this function is not important please ready my README.md for how
     * to use this.
     *
     * @param ci
     * @return
     */
    public boolean removeClickListener(ClickableItem ci) {
        return tmpItems.remove(ci);
    }

    public static InstanceManager getInstanceManager() {
        return instanceManager;
    }

    private static void onInventoryClick(InventoryClickEvent e) {
        if (!((e.getWhoClicked() instanceof Player) && Admins.isAdmin(e.getWhoClicked().getUniqueId()))) {
            return;
        }
        Page p;
        Optional<Page> pOptional = instanceManager.getByInventoryName(e.getView().getTitle());
        if (pOptional.isPresent()) {
            p = pOptional.get();
        } else {
            return;
        }

        ClickableItem ci = p.getItemById(e.getCurrentItem());
        if (ci == null) {
            return;
        }
        Object[] infos = p.information.get(e.getWhoClicked().getUniqueId());
        if (infos == null) {
            infos = new Object[]{};
        }

        e.setCancelled(true);
        ci.click((Player) e.getWhoClicked(), e.isShiftClick(), infos);
        ci.click((Player) e.getWhoClicked(), e.isShiftClick());
        if (e.isLeftClick()) {
            ci.leftClick((Player) e.getWhoClicked(), e.isShiftClick(), infos);
            ci.leftClick((Player) e.getWhoClicked(), e.isShiftClick());
        }
        if (e.isRightClick()) {
            ci.rightClick((Player) e.getWhoClicked(), e.isShiftClick(), infos);
            ci.rightClick((Player) e.getWhoClicked(), e.isShiftClick());
        }
    }

    /**
     * Do not forget to check if the Player is allowed to access the Inventory.
     *
     * @param p the Player who wants to see the Inventory.
     */
    public void open(Player p) {
        open(p, BasicFunctions.getNeededRows(items.length));
    }

    /**
     * Do not forget to check if the Player is allowed to access the Inventory.
     *
     * @param p the Player who wants to see the Inventory.
     * @param rows number of Rows. There must be enough to fit all Items. One
     * row has 9 slots.
     */
    public void open(Player p, Integer rows) {
        Inventory inv = Bukkit.createInventory(null, 9 * rows, name);
        p.openInventory(inv);

        int slotCounter = 0;
        for (ClickableItem ci : items) {
            ci.setSlot(slotCounter);
            inv.setItem(slotCounter, ci.getItem());
            ci.onLoad(p);
            slotCounter += 1;
        }
    }

    /**
     * Do not forget to check if the Player is allowed to access the Inventory.
     *
     * @param p the Player who wants to see the Inventory.
     * @param information information that was selected in the inventory before.
     */
    public void open(Player p, Object... information) {
        this.information.put(p.getUniqueId(), information);
        Inventory inv = Bukkit.createInventory(null, 9 * BasicFunctions.getNeededRows(items.length), name);
        p.openInventory(inv);

        int slotCounter = 0;
        for (ClickableItem ci : items) {
            ci.setSlot(slotCounter);
            inv.setItem(slotCounter, ci.getItem());
            ci.onLoad(p);
            ci.onLoad(p, information);
            slotCounter += 1;
        }
    }

    public static class InstanceManager {

        private InstanceManager() {
        }

        private final LinkedList<Page> INSTANCES = new LinkedList<>();

        /**
         * Because the Name of the Page is the same as the Inventory Name, in
         * front of the Page name is a §r. Here the §r is automatically added.
         *
         * @param name The same Name you saved the Page with.
         * @return Returns whether or not the name exists.
         */
        public boolean existsName(String name) {
            return INSTANCES.stream().anyMatch((p) -> ((p.name).equals("§r" + name)));
        }

        /**
         * Remove a Page from the InstanceManager. This is usually done over the
         * Page with removeFromInstanceManager();
         *
         * @param p The Page you want to remove from the Instance Manager.
         * @return if the Page was successfully removed.
         */
        public boolean remove(Page p) {
            return INSTANCES.remove(p);
        }

        /**
         * Add a Page to the InstanceManager for easier access. This is usually
         * done over the Page with addToInstanceManager();
         *
         * @param p the Page to add. Please check if it is already present.
         */
        public void add(Page p) {
            INSTANCES.add(p);
        }

        /**
         * Do not confuse with getByName.
         *
         * Because the Name of the Page is the same as the Inventory Name, in
         * front of the Page name is a §r. Here the §r is NOT added.
         *
         * @param invName The Title of the Inventory.
         * @return Optional can be empty if no Page was found with the Provided
         * Name.
         */
        public Optional<Page> getByInventoryName(String invName) {
            for (Page p : INSTANCES) {
                if (p.name.equals(invName)) {
                    return Optional.of(p);
                }
            }
            return Optional.empty();
        }

        /**
         * Do not confuse with getByInventoryName.
         *
         * Because the Name of the Page is the same as the Inventory Name, in
         * front of the Page name is a §r. Here the §r is automatically added.
         *
         * @param name The same Name you saved the Page with.
         * @return Optional can be empty if no Page was found with the Provided
         * Name.
         */
        public Optional<Page> getByName(String name) {
            for (Page p : INSTANCES) {
                if ((p.name).equals("§r" + name)) {
                    return Optional.of(p);
                }
            }
            return Optional.empty();
        }

        public LinkedList<Page> getInstances() {
            return INSTANCES;
        }

        /**
         * Log all Instances.
         */
        public void debug() {
            Bukkit.getLogger().log(Level.FINE, "List of all registerd Instances in the Page Instance Manager in the following format: 'page.name - page.toString()'");
            INSTANCES.forEach((p) -> {
                Bukkit.getLogger().log(Level.FINE, "{0} - {1}", new Object[]{p.name, p.toString()});
                System.out.println(p.name + p.toString());
            });
        }

    }
}
