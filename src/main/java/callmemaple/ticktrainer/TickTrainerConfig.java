package callmemaple.ticktrainer;

import callmemaple.ticktrainer.ui.DisplayMode;
import net.runelite.client.config.*;

import java.awt.Color;

import static callmemaple.ticktrainer.ui.DisplayMode.MINIMAL;

@ConfigGroup("ticktrainer")
public interface TickTrainerConfig extends Config
{
	@ConfigItem(
		keyName = "displayMode",
		name = "Display Mode",
		description = "How much info should the overlay show?"
	)
	default DisplayMode displayMode()
	{
		return MINIMAL;
	}

	@Range(
			min = 1,
			max = 300
	)
	@ConfigItem(
			keyName = "errorTimeout",
			name = "Error Timeout",
			description = "How long in ticks should an error stay (1-300 ticks)"
	)
	default int errorTimeout()
	{
		return 10;
	}

	@ConfigItem(
			keyName = "errorsOnly",
			name = "Errors Only",
			description = "Will only display info for the error status"
	)
	default boolean errorsOnly()
	{
		return false;
	}

	@ConfigSection(
			name = "Status Colors",
			description = "Change colors of the statuses",
			position = 5,
			closedByDefault = true
	)
	String StatusColors = "Status Colors";

	@ConfigItem(
			keyName = "onCycleColor",
			name = "On Cycle",
			description = "The color used when on cycle",
			section = StatusColors
	)
	default Color onCycleColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
			keyName = "idleColor",
			name = "Idle",
			description = "The color used when on cycle",
			section = StatusColors
	)
	default Color idleColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			keyName = "errorColor",
			name = "Error",
			description = "The color used when on cycle",
			section = StatusColors
	)
	default Color errorColor()
	{
		return Color.RED;
	}
}
