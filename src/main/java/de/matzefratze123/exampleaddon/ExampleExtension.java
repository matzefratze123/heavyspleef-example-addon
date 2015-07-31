package de.matzefratze123.exampleaddon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.PlayerPostActionHandler.PostActionCallback;
import de.matzefratze123.heavyspleef.core.event.GameStateChangeEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.extension.Extension;
import de.matzefratze123.heavyspleef.core.extension.GameExtension;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.GameManager;
import de.matzefratze123.heavyspleef.core.game.GameState;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.lib.dom4j.Element;

/*
 * Every extension must be annotated with @Extension, declaring
 * an internal name and wether there are command methods declared
 * inside the class or not
 */
@SuppressWarnings("deprecation")
@Extension(name = "example-extension", hasCommands = true)
public class ExampleExtension extends GameExtension {

	/* For a reference to data values see 
	 * http://minecraft.gamepedia.com/Data_values#Wool.2C_Stained_Clay.2C_Stained_Glass_and_Carpet */
	private static final byte RED_CLAY = (byte) 14;
	private static final byte LIME_CLAY = (byte) 5;
	
	/*
	 * Defining a command for adding the extension to a game
	 */
	@Command(name = "addgameindicator", usage = "/spleef addgameindicator <game>", minArgs = 1,
			permission = "heavyspleef.admin.addgameindicator", description = "Adds an indicator block to a game")
	@PlayerOnly
	public static void onExampleExtensionAddCommand(CommandContext context, HeavySpleef heavySpleef, ExampleAddon addon) 
			throws CommandException {
		//As the sender can only be a player, get a SpleefPlayer instance by calling
		//HeavySpleef#getSpleefPlayer(Object)
		final SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		String gameName = context.getString(0);
		//Get an instance of the GameManager
		GameManager manager = heavySpleef.getGameManager();
		
		//This throws a CommandException if the game does not exist with the specified message
		CommandValidate.isTrue(manager.hasGame(gameName), addon.getI18n().getString(Messages.Command.GAME_DOESNT_EXIST));
		
		//Get the game
		Game game = manager.getGame(gameName);
		//Handle a block click after executing this command
		applyPostAction(heavySpleef, player, game, false);
		player.sendMessage(ChatColor.GRAY + "Click on a block to add it as an indicator.");
	}
	
	/* 
	 * Defining a command removing an indiciator block from a game
	 */
	@Command(name = "removegameindicator", usage = "/spleef removegameindicator",
			permission = "heavyspleef.admin.removegameindicator", description = "Removes an indicator block")
	@PlayerOnly
	public static void onExampleExtensionRemoveCommand(CommandContext context, HeavySpleef heavySpleef, ExampleAddon addon) {
		//As the sender can only be a player, get a SpleefPlayer instance by calling
		//HeavySpleef#getSpleefPlayer(Object)
		final SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		
		//Handle a block click after executing this command
		applyPostAction(heavySpleef, player, null, true);
		player.sendMessage(ChatColor.GRAY + "Click on an existing indicator block to remove it.");
	}
	
	/*
	 * Method called when a player executed a remove/add command
	 */
	private static void applyPostAction(final HeavySpleef heavySpleef, SpleefPlayer player, final Game game, final boolean remove) {
		//Wait until the player clicked a block (this block will be added)
		heavySpleef.getPostActionHandler().addPostAction(player, PlayerInteractEvent.class, new PostActionCallback<PlayerInteractEvent>() {

			/*
			 * This is called one time when the player interacts
			 */
			public void onPostAction(PlayerInteractEvent event, SpleefPlayer player, Object cookie) {
				//Check if the player clicked a block
				Block block = event.getClickedBlock();
				if (block == null) {
					player.sendMessage(ChatColor.RED + "You must click on a block to continue!");
					return;
				}
				
				if (remove) {
					//Remove any extensions found at this location
					boolean removed = false;
					
					for (Game game : heavySpleef.getGameManager().getGames()) {
						for (ExampleExtension ext : game.getExtensionsByType(ExampleExtension.class)) {
							if (!ext.location.equals(block.getLocation())) {
								continue;
							}
							
							game.removeExtension(ext);
							removed = true;
						}
					}
					
					if (removed) {
						player.sendMessage(ChatColor.GRAY + "Game indicator removed!");
					} else {
						player.sendMessage(ChatColor.RED + "No game indicators found where you clicked");
					}
				} else {
					//Add a new extension to the game
					ExampleExtension extension = new ExampleExtension(block.getLocation());
					//Actually add the extension to the game
					game.addExtension(extension);
					
					//Update and set the block
					extension.updateBlock(game.getGameState());
					player.sendMessage(ChatColor.GRAY + "Indicator added!");
				}
			}
			
		});
	}
	
	/* In this example we are storing a location whose block is either
	 * red stained clay for game running, or green stained clay for joinable */
	private Location location;
	
	/* Mandatory empty constructor for construction when deserializing/unmarshalling */
	@SuppressWarnings("unused")
	private ExampleExtension() {}
	
	/* A constructor taking the location attribute as a parameter */
	public ExampleExtension(Location where) {
		this.location = where;
	}
	
	/*
	 * This methods stores all attributes of your extension in 
	 * a XML element (dom4j).
	 */
	public void marshal(Element element) {
		//Create a new child element for the location
		Element locationElement = element.addElement("location");
		
		//Store the world, x, y and z in separate child elements of locationElement
		locationElement.addElement("world").setText(location.getWorld().getName());
		locationElement.addElement("x").setText(String.valueOf(location.getBlockX()));
		locationElement.addElement("y").setText(String.valueOf(location.getBlockY()));
		locationElement.addElement("z").setText(String.valueOf(location.getBlockZ()));
	}

	/*
	 * This methods restores all attributes of your extension from
	 * a XML element previously created by #marshal(Element)
	 */
	public void unmarshal(Element element) {
		//Gets our previously stored location element
		Element locationElement = element.element("location");
		
		//Restore all values that uniquely identify the location
		World world = Bukkit.getWorld(locationElement.elementText("world"));
		int x = Integer.parseInt(locationElement.elementText("x"));
		int y = Integer.parseInt(locationElement.elementText("y"));
		int z = Integer.parseInt(locationElement.elementText("z"));
		
		//Finally create a new location and assign it to our field
		this.location = new Location(world, x, y, z);
	}
	
	/*
	 * Listening for game state changes as we want to set the block
	 * to a specified color indicating the game state
	 * 
	 * This listens only for events of the game this extension has been
	 * applied to.
	 */
	@Subscribe
	public void onGameStateChange(GameStateChangeEvent event) {
		//Getting the new game state
		GameState state = event.getNewState();
		
		//Calling a method to actually change the block
		updateBlock(state);
	}
	
	public void updateBlock(GameState newState) {
		Block block = location.getBlock();
		block.setType(Material.STAINED_CLAY);
		
		if (newState == GameState.INGAME || newState == GameState.DISABLED) {
			block.setData(RED_CLAY);
		} else {
			block.setData(LIME_CLAY);
		}
	}

}
