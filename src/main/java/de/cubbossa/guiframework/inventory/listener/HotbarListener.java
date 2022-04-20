package de.cubbossa.guiframework.inventory.listener;

import de.cubbossa.guiframework.GUIHandler;
import de.cubbossa.guiframework.inventory.Action;
import de.cubbossa.guiframework.inventory.HotbarMenu;
import de.cubbossa.guiframework.inventory.context.ClickContext;
import de.cubbossa.guiframework.inventory.context.TargetContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;

public class HotbarListener implements Listener {

	private final Set<HotbarMenu> menus = new HashSet<>();

	public HotbarListener() {
		Bukkit.getPluginManager().registerEvents(this, GUIHandler.getInstance().getPlugin());
	}

	public void register(HotbarMenu menu) {
		menus.add(menu);
	}

	public void unregister(HotbarMenu menu) {
		menus.remove(menu);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		menus.forEach(menu -> {
			if (!menu.isThisInventory(player.getInventory(), player)) {
				return;
			}
			ClickContext clickContext = new ClickContext(player, player.getInventory().getHeldItemSlot(), true);
			ItemStack stack = event.getItemDrop().getItemStack();
			event.setCancelled(menu.handleInteract(Action.Hotbar.DROP, clickContext));
			if (clickContext.isCancelled()) {
				Bukkit.getScheduler().runTaskLater(GUIHandler.getInstance().getPlugin(), () -> {
					player.getInventory().removeItem(stack);
				}, 1);
			}
		});
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		menus.forEach(menu -> {
			if (!menu.isThisInventory(player.getInventory(), player)) {
				return;
			}
			switch (event.getAction()) {
				case LEFT_CLICK_AIR -> event.setCancelled(menu.handleInteract(Action.Hotbar.LEFT_CLICK_AIR, new ClickContext(player, player.getInventory().getHeldItemSlot(), true)));
				case RIGHT_CLICK_AIR -> event.setCancelled(menu.handleInteract(Action.Hotbar.RIGHT_CLICK_AIR, new ClickContext(player, player.getInventory().getHeldItemSlot(), true)));
				case LEFT_CLICK_BLOCK -> event.setCancelled(menu.handleInteract(Action.Hotbar.LEFT_CLICK_BLOCK, new TargetContext<>(player, player.getInventory().getHeldItemSlot(), true, event.getClickedBlock())));
				case RIGHT_CLICK_BLOCK -> event.setCancelled(menu.handleInteract(Action.Hotbar.RIGHT_CLICK_BLOCK, new TargetContext<>(player, player.getInventory().getHeldItemSlot(), true, event.getClickedBlock())));
			}
		});
	}
}
