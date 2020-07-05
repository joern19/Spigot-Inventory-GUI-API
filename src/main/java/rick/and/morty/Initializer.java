package rick.and.morty;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author not-a-robot
 */
public class Initializer {
    
    private static Boolean initinalized = false;
    
    /**
     * This has to be done only once but is very Important to work properly.
     * 
     * It creates an ID key and registers an Inventory Click Event.
     * 
     * @param ll any Listener
     * @param pl
     */
    public static void start(Listener ll, JavaPlugin pl) {
        if (initinalized) {
            return;
        }
        initinalized = true;
        
        ClickableItem.ID_KEY = new NamespacedKey(pl, "gui_id");
        Bukkit.getPluginManager().registerEvent(InventoryClickEvent.class, ll, EventPriority.HIGH, (listener, event) -> {
            Page.onInventoryClick((InventoryClickEvent) event);
        }, pl);
        
    }
}
