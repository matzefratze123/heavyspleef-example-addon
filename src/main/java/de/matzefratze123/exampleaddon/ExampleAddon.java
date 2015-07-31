package de.matzefratze123.exampleaddon;

import de.matzefratze123.heavyspleef.addon.AddOnProperties;
import de.matzefratze123.heavyspleef.addon.java.BasicAddOn;

/*
 * This is the main class of your add-on that hooks into the add-on API
 * and receives callbacks for the following to methods
 */
public class ExampleAddon extends BasicAddOn {
	
	/*
	 * This method gets called once your add-on is being enabled
	 */
	@Override
	public void enable() {
		//If you do not want to declare command classes in the add-on yml you can
		//register commands programmatically at any time you want to
		/* getCommandManager().registerSpleefCommand(ExampleCommand.class, this); */
		
		//Same for flag classes
		/* getFlagRegistry().registerFlag(ExampleFlag.class, this); */
		
		//And another example for extensions
		/* getExtensionRegistry().registerExtension(ExampleExtension.class, this); */
		
		//These are the properties as defined in the addon.yml
		AddOnProperties properties = getProperties();
		
		getLogger().info("ExampleAddon is now enabled! [v" + properties.getVersion() + "]");
	}
	
	/*
	 * This method gets called once your add-on is being disabled.
	 * In this case you should release any resources your add-on occupies
	 */
	@Override
	public void disable() {
		getLogger().info("ExampleAddon is now disabled!");
	}

}
