package de.matzefratze123.exampleaddon;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.matzefratze123.heavyspleef.core.event.PlayerWinGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BaseFlag;

/*
 * This example flag sends a message to all Spleef winners
 * and gives them a cookie.
 * 
 * Every flag class must be annotated with @Flag which defines
 * the name of your flag and optional parameters such as conflicts
 * with other flags, other plugin depends, wether you want to add
 * command methods to your flag class, etc.
 */
@Flag(name = "example")
//@BukkitListener - Use this annotation if you need to listen for Bukkit events
public class ExampleFlag extends BaseFlag {

	/* Mandatory empty constructor */
	public ExampleFlag() {}
	
	/*
	 * This is the description of your flag
	 * (Currently only used internally)
	 */
	@Override
	public void getDescription(List<String> desc) {
		desc.add("Sends a message to all Spleef winners and give them a Cookie");
	}
	
	/*
	 * You may specify HeavySpleef listeners by annotating your listener method
	 * with @Subscribe and a desired event for the first parameter
	 */
	@SuppressWarnings("deprecation")
	@Subscribe
	public void onPlayerWinGame(PlayerWinGameEvent event) {
		//The i18n instance grants you access to your message files as declared by
		//locale resources
		I18N i18n = getI18N();
		
		//Baking a cookie
		ItemStack cookie = new ItemStack(Material.COOKIE);
		
		//Looping every winner of this game
		for (SpleefPlayer player : event.getWinners()) {
			//Gets the message and replaces the $[player] variable
			//with the player's display-name
			String message = i18n.getVarString("winner-message")
					.setVariable("player", player.getDisplayName())
					.toString();
			
			player.sendMessage(message);
			
			//SpleefPlayer#getBukkitPlayer() grants you access to the
			//Bukkit player object
			Player bukkitPlayer = player.getBukkitPlayer();
			
			bukkitPlayer.getInventory().addItem(cookie);
			bukkitPlayer.updateInventory();
		}
	}

}
