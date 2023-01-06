package callmemaple.ticktrainer;

import callmemaple.ticktrainer.ui.TickTrainerOverlay;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
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
	@Inject private TickTrainerOverlay overlay;
	@Inject private TickTrainerConfig config;

	private final Collection<Object> eventHandlers = new LinkedList<>();

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		Collections.addAll(eventHandlers,
				injector.getInstance(TickManager.class),
				injector.getInstance(TickingCycleManager.class),
				injector.getInstance(SkillingCycleManager.class),
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

	@Provides
	TickTrainerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TickTrainerConfig.class);
	}
}
