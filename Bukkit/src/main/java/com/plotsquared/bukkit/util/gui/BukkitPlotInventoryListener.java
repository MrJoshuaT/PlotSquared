/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *               Copyright (C) 2014 - 2022 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.util.gui;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.util.gui.PlotInventoryClickHandler;
import com.plotsquared.core.util.gui.PlotInventoryClickType;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BukkitPlotInventoryListener implements Listener {

    private static final EnumMap<ClickType, PlotInventoryClickType> CLICK_MAPPING = new EnumMap<>(ClickType.class);

    static {
        CLICK_MAPPING.put(ClickType.LEFT, PlotInventoryClickType.LEFT);
        CLICK_MAPPING.put(ClickType.RIGHT, PlotInventoryClickType.RIGHT);
        CLICK_MAPPING.put(ClickType.SHIFT_LEFT, PlotInventoryClickType.SHIFT_LEFT);
        CLICK_MAPPING.put(ClickType.SHIFT_RIGHT, PlotInventoryClickType.SHIFT_RIGHT);
        CLICK_MAPPING.put(ClickType.MIDDLE, PlotInventoryClickType.MIDDLE);
    }

    private final Map<UUID, BukkitPlotInventory> inventories;

    public BukkitPlotInventoryListener(final Map<UUID, BukkitPlotInventory> inventories) {
        this.inventories = inventories;
    }

    @EventHandler
    public void onInventoryClick(final org.bukkit.event.inventory.InventoryClickEvent event) {
        final PlotPlayer<Player> player = BukkitUtil.adapt((Player) event.getWhoClicked());

        BukkitPlotInventory currentInventory = inventories.get(player.getUUID());
        if (currentInventory == null) {
            return;
        }
        if (!Objects.equals(event.getClickedInventory(), currentInventory.nativeInventory)) {
            return;
        }

        final int slot = event.getRawSlot();
        if (slot < 0 || slot >= currentInventory.size()) {
            return;
        }
        PlotInventoryClickType clickType = CLICK_MAPPING.getOrDefault(event.getClick(), PlotInventoryClickType.OTHER);
        event.setCancelled(true);

        final PlotInventoryClickHandler clickHandler = currentInventory.clickHandlers[slot];
        if (clickHandler == null) {
            return;
        }
        final ItemStack item = event.getCurrentItem();
        if (item == null) {
            clickHandler.handle(null, clickType);
            return;
        }
        clickHandler.handle(new PlotItemStack(
                BukkitAdapter.asItemType(item.getType()),
                item.getAmount(),
                item.getItemMeta().getDisplayName(),
                item.getItemMeta().hasLore() ? item.getItemMeta().getLore().toArray(String[]::new) : new String[0]
        ), clickType);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        final PlotPlayer<Player> player = BukkitUtil.adapt((Player) event.getPlayer());
        BukkitPlotInventory currentInventory = inventories.get(player.getUUID());
        if (currentInventory == null) {
            return;
        }
        currentInventory.nativeInventory = null;
        inventories.remove(player.getUUID());
    }


}
