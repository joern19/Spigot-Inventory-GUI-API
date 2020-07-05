package rick.and.morty;

import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ClickableItem extends ClickEvents {

    private static Integer lastID = 0; //the unique id for this item...
    protected static NamespacedKey ID_KEY;
    
    private final ItemStack item;
    private Integer slot = null;
    
    /**
     * Create an Clickable Item with an ItemStack already ready for use.
     * @param item 
     */
    public ClickableItem(ItemStack item) {
        ItemMeta im = item.getItemMeta();
        im.getPersistentDataContainer().set(ID_KEY, PersistentDataType.INTEGER, ++lastID);
        item.setItemMeta(im);
        this.item = item;
    }

    /**
     * Create an ClickableItem with a custom <b>name</b>
     * @param m
     * @param name 
     */
    public ClickableItem(Material m, String name) {
        ItemStack is = new ItemStack(m);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);
        im.getPersistentDataContainer().set(ID_KEY, PersistentDataType.INTEGER, ++lastID);
        is.setItemMeta(im);

        item = is;
    }

    /**
     * Create an ClickableItem with a custom <b>name</b> and custom <b>lore</b>.
     * 
     * @param m
     * @param name
     * @param lore 
     */
    public ClickableItem(Material m, String name, String... lore) {
        ItemStack is = new ItemStack(m);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(name);
        im.setLore(Arrays.asList(lore));
        im.getPersistentDataContainer().set(ID_KEY, PersistentDataType.INTEGER, ++lastID);
        is.setItemMeta(im);

        item = is;
    }

    /**
     * Create an ClickableItem with custom <b>ItemMeta</b>
     * @param m
     * @param im 
     */
    public ClickableItem(Material m, ItemMeta im) {
        ItemStack is = new ItemStack(m);
        im.getPersistentDataContainer().set(ID_KEY, PersistentDataType.INTEGER, ++lastID);
        is.setItemMeta(im);

        item = is;
    }

    /**
     * In rare cases you want to Manage an ClickableItem manually.
     * Read the README.md for how to use it.
     * 
     * @param slot 
     */
    public final void setSlot(Integer slot) {
        this.slot = slot;
    }
    
    /**
     * Read the README.md for how to use it.
     * 
     * @return the slot number so you can temporarily chance something in the Inventory that is currently open.
     */
    public final Integer getSlot() {
        return this.slot;
    }

    public final ItemStack getItem() {
        return item;
    }
    
    /**
     * <b>You can ignore this function.</b>
     * 
     * It is used in the class Page when an Item is clicked and the corresponding ClickableItem is needed.
     * 
     * @param is
     * @return 
     */
    public boolean compareKey(ItemStack is) {
        if (is == null || !is.hasItemMeta()) {
            return false;
        }
        
        PersistentDataContainer pdc = is.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(ID_KEY, PersistentDataType.INTEGER)) {
            return false;
        }
        
        Integer id = pdc.get(ID_KEY, PersistentDataType.INTEGER);
        return this.getItem().getItemMeta().getPersistentDataContainer().get(ID_KEY, PersistentDataType.INTEGER).equals(id);
    }
}
