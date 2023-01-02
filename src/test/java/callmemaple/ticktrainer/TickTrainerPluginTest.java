package callmemaple.ticktrainer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TickTrainerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TickTrainerPlugin.class);
		RuneLite.main(args);
	}
}