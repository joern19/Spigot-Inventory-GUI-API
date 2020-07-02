# Spigot-Inventory-GUI-API
Create an Inventory GUI simply and fast.

# Installation
### Add the Depedency to your pom.xml in your maven Project
```xml
<dependency>
  <groupId>rick.and.morty</groupId>
  <artifactId>Spigot-Inventory-GUI-API</artifactId>
  <version>1.2-SNAPSHOT</version>
</dependency>
```
Also do not forget to build your jar with dependencies.
### Run the Initializer onLoad
Add the following line to your onEnable function.

It will Register an Event which will be responsible when you click an clickable Item\
and it will create an NamespacedKey. But you do not have to worry about that.
```
Initializer.start(<your_listener>, this);
```
\
\
\
_
# Examples
### 1. Simple Example
```java
ClickableItem ci = new ClickableItem(Material.IRON_INGOT, "Add Iron to your Inventory.") {
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
 
Additionally we override the Function ```click(Player p, Boolean shift);``` which is called when the Item is clicked ignoring if it is a left or right click. The function just gives us an Iron Ingot and when we press shift while we press the Item, it will give us 64 Iron Ingots.

The Last thing we do, is we add the ClickableItem to a new Page called "my page" and add it to the Instance Manager so it can be accessed by the function which we registered in the Installation.\
If we do not add the Page to the Instance Manager, nothing will happen when we click the Item.

Finally we can open the Page from everywhere with\
```Page.getInstanceManager().getByName("my page").get().open(player);```\
Because the Function getByName(String name); in the Instance Manager returns an Optional<Page> we need to get() the Page from it and then can show it to the Player with the Function open(Player p);

### 2. Update Info Onload
Sometimes we need to set Information right after opening the Page, for example: We want to allow a Player to change a Gamerule and show the current state of it.
```java
new ClickableItem(Material.SUNFLOWER, "loading...") {
  @Override
  public void click(Player p, Boolean shift) {
    World w = p.getWorld();
    Boolean currentState = w.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE);
    w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, !currentState);
    onLoad(p);
  }

  @Override
  public void onLoad(Player p) {
    ItemMeta itemMeta = this.getItem().getItemMeta();
    Boolean currentState = p.getWorld().getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE);
    String newTitle = "DO_DAYLIGHT_CYCLE is " + (currentState ? "on" : "off");
    itemMeta.setDisplayName(newTitle);
    p.getOpenInventory().getItem(this.getSlot()).setItemMeta(itemMeta);            
  }
}
```
The Title "loading..." should be not visible because we instantly replace it in the onLoad function.

When the Item is loaded in the Inventory, we get The ItemMeta of the ClickableItem which contains an Important ID so we need to copy it. We than replace the Title with the "DO_DAYLIGHT_CYCLE is " and then weather it is enabled or not. We than update the ItemStack in the Inventory of the Player with the new MetaData but the real ClickableItem doesnt get changed!

If a Player now hovers over the Item he would see the new Title and when he clicks it, the Gamerule get flipped and the onLoad Function is called so the Item gets updated with the new Title.

### 3. Information pass through
If we have multiple Layers of Pages where we can select for example a Player and after clicking him we can do something with that Player on a new Page. Now the new Page needs to know which Player was selected, so we do not need a Page for every single Player which would be very laggy.

We can actually open a Page with custom Information provided. The Information can than be optionally accessed by the the rightClick, leftClick and click function.

First we create a Page which contains all Player options (the 2. layer).
```java
ClickableItem teleport = new ClickableItem(Material.ENDER_PEARL, "Teleport you to him/her") {
  @Override
  public void click(Player p, Boolean shift, Object[] infos) {
    if (infos.length >= 1 && infos[0] instanceof Player) {
      Player clicked = (Player) infos[0];
      p.teleport(clicked);
    } else {
      p.sendMessage("Error please try again.");
    }
  }
};
        
new Page("Player options", teleport).addToInstanceManager();

```
You can see the third argument of the function click which is an Object array. We could pass any Information in it but here we will pass the Player selected in the first Inventory(wich is not implemented yet). So in the if statement we validate the Information and check if we can work with it. If everything is ok, we get the Player who was selected in the first Inventory: "clicked". After that we Teleport ourselves to that Player.

Now lets see how we Implement the first Layer. But first we need three helper Functions to resize the Inventory when it is already open and to get the Player heads. This is necessary to fit all Player heads perfectly but you could also just open the Inventory on its maximum size. I will not explain these functions, because they do not have very much to do with this API.
```java
private static Containers<?> getContainerType(int slots) {
  int neededRows = slots / 9;
    if (slots % 9 != 0) {
      neededRows = ((int) slots / 9) + 1;
    }
    switch (neededRows) {
      case 1:
        return Containers.GENERIC_9X1;
      case 2:
        return Containers.GENERIC_9X2;
      case 3:
        return Containers.GENERIC_9X3;
      case 4:
        return Containers.GENERIC_9X4;
      case 5:
        return Containers.GENERIC_9X5;
      case 6:
        return Containers.GENERIC_9X6;
      default:
        System.err.println("Failed to get Containers Type for " + neededRows + " rows.");
        return null;
        }
    }

public static void risizeToFitAllItems(Player p, int numberOfItems) {
  EntityPlayer ep = ((CraftPlayer) p).getHandle();
  PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(
    ep.activeContainer.windowId, 
    getContainerType(numberOfItems), 
    new ChatMessage(p.getOpenInventory().getTitle())
  );
  ep.playerConnection.sendPacket(packet);
  ep.updateInventory(ep.activeContainer);
}

public ItemStack getHead(Player p) { //I recommend to build a chache with Heads of 
  ItemStack playerhead = new ItemStack(Material.PLAYER_HEAD);//every Player currently on the server.
  SkullMeta playerheadmeta = (SkullMeta) playerhead.getItemMeta();
  playerheadmeta.setOwningPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()));
  playerheadmeta.setDisplayName(p.getName());
  playerhead.setItemMeta(playerheadmeta);
  return playerhead;
}
```
The Helper Functions done, we can finally start with the first layer.
```java
ClickableItem ci = new ClickableItem(Material.CLOCK, "loading...") {
  @Override
  public void onLoad(Player p) {
    risizeToFitAllItems(p, Bukkit.getOnlinePlayers().size());

    p.getOpenInventory().getItem(this.getSlot()).setType(Material.VOID_AIR);
    int counter = this.getSlot();
    for (Player all : Bukkit.getOnlinePlayers()) {
      ClickableItem ci = new ClickableItem(getHead(all)) {
      @Override
        public void click(Player p, Boolean shift) {
          Player clicked = Bukkit.getPlayer(getItem().getItemMeta().getDisplayName());
          Page.getInstanceManager().getByName("Player options").get().open(p, clicked);
        }
      };
      ci.setSlot(counter);
      p.getOpenInventory().setItem(counter, ci.getItem());

      Page page = Page.getInstanceManager().getByName("Player options").get();
      page.addClickListener(ci);

      counter += 1;
    }
  }
};

new Page("My GUI", ci).addToInstanceManager();
```



\
\
\
_
## License
[GNU General Public License v3.0](https://choosealicense.com/licenses/gpl-3.0/)
