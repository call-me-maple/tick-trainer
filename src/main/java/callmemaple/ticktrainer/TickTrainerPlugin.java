package callmemaple.ticktrainer;

import callmemaple.ticktrainer.method.DoubleRollManager;
import callmemaple.ticktrainer.method.DoubleRollOverlay;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuAction;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

@Slf4j
@PluginDescriptor(
	name = "tick trainer"
)
public class TickTrainerPlugin extends Plugin
{
	@Inject private OverlayManager overlayManager;
	@Inject private EventBus eventBus;
	@Inject private DoubleRollOverlay overlay;
	@Inject private TickTrainerConfig config;

	private final Collection<Object> eventHandlers = new LinkedList<>();

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		Collections.addAll(eventHandlers,
				injector.getInstance(TickTracker.class),
				injector.getInstance(DoubleRollManager.class),
				injector.getInstance(ResourceTickManager.class),
				injector.getInstance(ClickManager.class),
				injector.getInstance(PlayerManager.class));
		eventHandlers.forEach(eventBus::register);
		log.info("tick trainer started!");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		eventHandlers.forEach(eventBus::unregister);
		eventHandlers.clear();
		log.info("tick trainer stopped!");
	}

	@Subscribe
	public void onOverlayMenuClicked(OverlayMenuClicked overlayMenuClicked)
	{
		OverlayMenuEntry overlayMenuEntry = overlayMenuClicked.getEntry();
		if (overlayMenuEntry.getMenuAction() == MenuAction.RUNELITE_OVERLAY
				&& overlayMenuClicked.getEntry().getOption().equals("Open")
				&& overlayMenuClicked.getOverlay() == overlay)
		{
			log.info("open more info");
		}
	}

	@Provides
	TickTrainerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TickTrainerConfig.class);
	}
}
