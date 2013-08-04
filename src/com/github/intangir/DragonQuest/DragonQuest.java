package com.github.intangir.DragonQuest;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class DragonQuest extends JavaPlugin implements Listener
{
    public Logger log;
    public PluginDescriptionFile pdfFile;

	public void onEnable()
	{
		log = this.getLogger();
		pdfFile = this.getDescription();

		Bukkit.getPluginManager().registerEvents(this, this);
		
		log.info("v" + pdfFile.getVersion() + " enabled!");
	}
	
	public void onDisable()
	{
		log.info("v" + pdfFile.getVersion() + " disabled.");
	}

    // trigger egg spawn
	@EventHandler(ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent e)
	{
		Block b = e.getBlock();
		
		if(b.getWorld().getName().equals("world") && b.getType() == Material.OBSIDIAN)
		{
			BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
			for (BlockFace face : faces)
			{
				if(b.getRelative(face).getType() == Material.PORTAL)
				{
					final Block portal = b.getRelative(face);
					final Player player = e.getPlayer();
					portal.getWorld().strikeLightning(portal.getLocation());
					
					log.info("Dragon Egg revealed by " + player.getName());
					
					player.sendMessage(ChatColor.RED + "The portal has become unstable!!");
					
					Runnable strike = new Runnable() { public void run() { portal.getWorld().strikeLightning(portal.getLocation()); } };
					long[] strikeTimings = {1, 5, 15, 30, 35, 38, 50, 52, 60};
					
					for(long timing: strikeTimings)
					{
						Bukkit.getScheduler().scheduleSyncDelayedTask(this, strike, timing);
					}
					
					Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						public void run() { 
							portal.setType(Material.DRAGON_EGG);
							player.sendMessage(ChatColor.YELLOW + "A dragon egg swirling with nether energies emerges... this item could be useful.");
							Bukkit.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "An ancient seal has been broken.");
						}
					}, 60);
				}
			}
		}
	}
	
    // disable punting the egg around
	@EventHandler(ignoreCancelled=true)
	public void onPlayerInteract(PlayerInteractEvent e)
	{
		if(e.getClickedBlock().getType() == Material.DRAGON_EGG)
		{
			e.setCancelled(true);
		}
	}

}

