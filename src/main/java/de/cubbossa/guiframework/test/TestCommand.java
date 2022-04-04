package de.cubbossa.guiframework.test;

import com.destroystokyo.paper.MaterialTags;
import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.chat.ComponentMenu;
import de.cubbossa.guiframework.chat.TextMenu;
import de.cubbossa.guiframework.inventory.BottomInventoryMenu;
import de.cubbossa.guiframework.inventory.MenuPresets;
import de.cubbossa.guiframework.inventory.implementations.InventoryMenu;
import de.cubbossa.guiframework.inventory.implementations.ListMenu;
import de.cubbossa.guiframework.scoreboard.CustomScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class TestCommand implements CommandExecutor {

    /*
    Testcases
    1) Scoreboards
        - show
        - hide
        - stacked
        - animations
        - thread safety
    2) Chat Menus
        - pagination
        - itemstacks
        - string component mixed
    3) Top Inventory Menus
        - setting items and clickhandlers
        - default click handlers
        - pagination
        - presets
            - pagination
            - backhandler
            - static filling
            - dynamic filling
        - sub and parent menus
        - animations
        - shared view in top menus
        - view modes


     */

    CustomScoreboard.Animation scoreboardAnimation = null;
    CustomScoreboard scoreboard = new CustomScoreboard("test_scoreboard_1", Component.text("Nur ein Scoreboard"), 10);
    CustomScoreboard higherBoard = new CustomScoreboard("test_scoreboard_2", Component.text("Noch eins"), 5);

    InventoryMenu exampleMenu = new InventoryMenu(4, Component.text("Example Inventory"));
    BottomInventoryMenu<ClickType> bottomMenu = new BottomInventoryMenu<>(1);
    ListMenu listMenu = new ListMenu(4, Component.text("Weapons"));
    InventoryMenu craftingMenu = new InventoryMenu(InventoryType.WORKBENCH, Component.text("Cursed Crafting:"));

    public TestCommand() {
        exampleMenu.loadPreset(MenuPresets.fill(MenuPresets.FILLER_LIGHT));
        exampleMenu.loadPreset(MenuPresets.fillRow(MenuPresets.FILLER_DARK, 3));
        exampleMenu.loadPreset(MenuPresets.paginationRow(3, 0, 1, false, ClickType.LEFT));
        int i = 0;
        for (Material material : MaterialTags.BEDS.getValues()) {
            exampleMenu.setButton(exampleMenu.buttonBuilder()
                            .withItemStack(material)
                            .withSound(Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 0f, 2f, .5f, 2f)
                            .withClickHandler(clickContext -> clickContext.getPlayer().getInventory().addItem(new ItemStack(material)), ClickType.RIGHT),
                    i++);
            if (i / 9 % 4 == 3) {
                i += 9;
            }
        }
        exampleMenu.setItem(new ItemStack(Material.STONE), -70, 60);
        exampleMenu.updateTitle(Component.text("Seite 2"), 1);

        bottomMenu.loadPreset(MenuPresets.fillRow(MenuPresets.FILLER_DARK, 1));
        bottomMenu.loadPreset(MenuPresets.paginationRow(exampleMenu, 1, 0, 1, false, ClickType.LEFT, ClickType.RIGHT));

        for (Material material : MaterialTags.ENCHANTABLE.getValues()) {
            listMenu.addListEntry(listMenu.buttonBuilder().withItemStack(material));
        }
        for (Material material : MaterialTags.TERRACOTTA.getValues()) {
            listMenu.addListEntry(listMenu.buttonBuilder().withItemStack(material));
        }

        Material[] planks = {Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS};
        craftingMenu.setItem(new ItemStack(Material.OAK_PLANKS), 1, 4, 5, 7, 8, 9);
        craftingMenu.playAnimation(1, 20, animationContext -> new ItemStack(planks[animationContext.getTicks() % planks.length]));
        craftingMenu.setItem(new ItemStack(Material.SPRUCE_STAIRS), 0);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;

        TextMenu online = new TextMenu("Spieler online:");
        Bukkit.getOnlinePlayers().forEach(p -> online.addSub(new ComponentMenu(p.displayName())));
        scoreboard.setLine(3, online);
        scoreboard.setLine(7, online.asComponent());

        TextMenu inventory = new TextMenu("Your Inventory:");
        Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).forEach(stack -> inventory.addSub(new TextMenu(stack.getAmount() + "x " + stack.getType().toString())));

        switch (strings[0]) {
            case "1.1":
                scoreboard.show(player);
                break;
            case "1.2":
                scoreboard.hide(player);
                break;
            case "1.3":
                higherBoard.show(player);
                break;
            case "1.4":
                higherBoard.hide(player);
                break;
            case "1.5":
                scoreboardAnimation = scoreboard.playAnimation(3, 200, 2, () -> {
                    float val = (System.currentTimeMillis() % 1000) / 1000f;
                    return Component.text("Rainbow", TextColor.color(1, val, 1 - val));
                });
                break;
            case "1.6":
                scoreboardAnimation.stop();
                break;
            case "1.7":
                Bukkit.getScheduler().runTaskAsynchronously(GUIHandler.getInstance().getPlugin(), () -> scoreboard.setLine(9, Component.text("Asynchron")));
                break;

            case "2.1":
                inventory.send(player);
                break;
            case "2.2":
                player.sendMessage(inventory);
                break;
            case "2.3":
                inventory.send(player, Integer.parseInt(strings[1]), 3);
                break;

            case "3.1":
                exampleMenu.open(player);
                break;
            case "3.2":
                exampleMenu.playAnimation(0, 10, animationContext ->
                        new ItemStack(Material.values()[(int) (System.currentTimeMillis() % Material.values().length)]));
                break;

            case "4.1":
                bottomMenu.open(player);
                break;
            case "5.1":
                listMenu.open(player);
                break;
            case "5.2":
                craftingMenu.open(player);
        }
        return false;
    }
}
