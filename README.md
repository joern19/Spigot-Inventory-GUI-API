# Spigot-Inventory-GUI-API
Create an Inventory GUI simply and fast.

# Installation
### Register Event Manager
Add the following to the onEnable function.\
It will Register an Event which will be responsible when you click an clickable Item.
```
Page.addEventListener(your_listener, this);
```

# Examples
### Simple Example
```
ClickableItem ci = new ClickableItem(Material.IRON_BARS, "Add Iron to your Inventory.") {
  @Override
  public void click(Player p, Boolean shift) {
    int amount = shift ? 1 : 64;
    ItemStack itemStack = new ItemStack(Material.IRON_INGOT, amount);
    p.getInventory().addItem(itemStack);
  }
}

new Page("my page", ci).addToInstanceManager();
```

The first thing we did here, was to create an ClickableItem.\
This ClickableItem has the Title "Add Iron to your Inventory." and looks like an Iron Ingot.

<insert Picture>
 
Additinally we override the Function ```click(Player p, Boolean shift);``` which is called when the Item is clicked ignoring if it is a left or right click. The function just gives us an Iron Ingot and when we press shift while we press the Item, it will give us 64 Iron Ingots.

The Last thing we do, is we add the ClickableItem to a new Page called "my page" and add it to the Instance Manager so it can be accessed by the Function wich we registerd in the Installtion.\
If we do not add the Page to the Instance Manager, nothing will happen when we click the Item.

Finally we can open the Page from everywhere with\
```Page.getInstanceManager().getByName("my page").get().open(player);```\
Because the Function getByName(String name); in the Instance Manager returns an Optional<Page> we need to get() the Page from it and then can show it to the Player with the Function open(Player p);

### Update Info Onload
Sometimes we need to set Information right after opening the Page, for example: We want to allow a Player to change a Gamerule and show the current state of it.
```
new ClickableItem(Material.SUNFLOWERE, "loading...") {
  @Override
  void click(Player p, Boolean shift) {
    World w = p.getWorld();
    Boolean currentState = (Boolean) w.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE);
    w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, !currentState);
    onLoad(p);
  }

  @Override
  void onLoad(Player p) {
    ItemMeta itemMeta = this.getItem().getItemMeta();
    Boolean currentState = p.getWorld().getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE);
    String newTitle = "DO_DAYLIGHT_CYCLE is " + (currentState ? "on" : "off");
    itemMeta.setDisplayName(newTitle);
    p.getOpenInventory().getItem(this.getSlot()).setItemMeta(itemMeta);            
  }
}
```
The Title "loading..." should be not visible because we instantly replace it in the onLoad function.\
When the Item is loaded in the Inventory, we get The ItemMeta of the ClickableItem which contains an Important ID so we need to copy it. We than replace the Title and 


#
#
#

## License
[GNU General Public License v3.0](https://choosealicense.com/licenses/gpl-3.0/)
