package com.github.intangir.DragonQuest;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class DragonQuest extends JavaPlugin implements Listener
{
    public Logger log;
    public PluginDescriptionFile pdfFile;
    public boolean allowDragon;
    public boolean allowEggPlace;
    
	public void onEnable()
	{
		log = this.getLogger();
		pdfFile = this.getDescription();
		allowDragon = false;
		allowEggPlace = true;

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
		if(b.getType() == Material.OBSIDIAN)
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
							player.sendMessage(ChatColor.YELLOW + "Something ominous emerges... you sense this item contains terrible power.");
							player.sendMessage(ChatColor.YELLOW + "You are filled with dread as the realization of what you have done dawns on you...");
							player.sendMessage(ChatColor.YELLOW + "Surely this world is no place for such a foul artifact...");
							Bukkit.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "An ancient seal has been broken.");
							Bukkit.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "A terrible artifact is released on this unsuspecting world...");
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

	// disable ender crystal damage
	@EventHandler(ignoreCancelled=true)
	public void onEnderCrystalDamage(EntityDamageEvent e)
	{
		if(e.getEntityType() == EntityType.ENDER_CRYSTAL)
		{
			e.setCancelled(true);
		}
	}

	// checks to make sure no dragons exist
	public boolean checkNoDragon(World world)
	{
		List<LivingEntity> ents = world.getLivingEntities();
    
	    for(LivingEntity ent : ents)
	    {
	        if(ent instanceof EnderDragon)
	        {
	        	return false;
	        }
	    }
	    return true;
    }

	
	// handle block placements for eggs, and endercrystal substitute
	@EventHandler(ignoreCancelled=true)
	public void onBlockPlace(BlockPlaceEvent e)
	{
		final Block b = e.getBlock();
		final World world = b.getWorld();
		if(b.getType() == Material.DRAGON_EGG)
		{
			if(world.getName().equals("world"))
			{
				e.getPlayer().sendMessage(ChatColor.YELLOW + "Surely this world is no place for such a foul artifact...");
			}
			else if(world.getName().equals("world_the_end"))
			{
				if(!allowEggPlace || !checkNoDragon(world))
				{
					e.getPlayer().sendMessage(ChatColor.YELLOW + "You dare not taunt the dragon by revealing a dragon egg.");
                    e.setCancelled(true);
					return;
                }
				
				log.info("Dragon Egg placed in end by " + e.getPlayer().getName());
				allowEggPlace = false;
				
				long[] beatTimings = {60, 40, 35, 30, 25, 20, 20, 20, 18, 15, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10};
				Runnable beat = new Runnable() { 
					public void run() {
						world.playEffect(b.getLocation(), Effect.ENDER_SIGNAL, 0);
						world.playEffect(b.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
						world.playSound(b.getLocation(), Sound.IRONGOLEM_WALK, 100, 0);
					}
				};
				
				long timing = 0;
				for(long inc: beatTimings)
				{
					timing += inc;
					Bukkit.getScheduler().scheduleSyncDelayedTask(this, beat, timing);
				}

				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						if(b.getType() == Material.DRAGON_EGG)
						{
							b.setType(Material.AIR);
							world.createExplosion(b.getLocation(), 0);
							world.strikeLightning(b.getLocation());
							allowDragon = true;
							world.spawnEntity(b.getLocation(), EntityType.ENDER_DRAGON);
						}
						else
						{
							allowEggPlace = true;
						}
					}
				}, timing);
			}
		}
		
		// temporarily need this as a substitute for placing ender crystals, so i can manually repair already broken ones
		if(b.getType() == Material.SPONGE)
		{
			e.setCancelled(true);
			world.spawnEntity(b.getLocation().add(0.5, -1, 0.5), EntityType.ENDER_CRYSTAL);
			
			// temporarily kill dragons too
			List<LivingEntity> ents = world.getLivingEntities();
            
            for(LivingEntity ent : ents){
                if(ent instanceof EnderDragon){
                	ent.damage(1000000);
                }
            }

		}
	}
	
	// enable ender dragon
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCreatureSpawn(CreatureSpawnEvent e)
	{
		if(e.getEntityType() == EntityType.ENDER_DRAGON && allowDragon)
		{
			e.setCancelled(false);
			allowDragon = false;
			log.info("Dragon Spawned!");
		}
	}
	
	// disable ender portal creation, and add dragon drops
	@EventHandler(ignoreCancelled=true)
	public void onEntityPortalCreate(EntityCreatePortalEvent e)
	{
		if(e.getPortalType() == PortalType.ENDER)
		{
			log.info("Dragon Killed!");
			World world = e.getEntity().getWorld();
			Location loc = e.getEntity().getLocation();
			world.dropItemNaturally(loc, new ItemStack(Material.NETHER_STAR));
			//world.dropItemNaturally(loc, new ItemStack(Material.EMERALD, 4));
			//world.dropItemNaturally(loc, new ItemStack(Material.GOLD_NUGGET, 20));
			allowEggPlace = true;

			e.setCancelled(true);
		}
	}
}

