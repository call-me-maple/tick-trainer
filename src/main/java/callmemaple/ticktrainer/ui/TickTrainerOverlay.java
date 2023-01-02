package callmemaple.ticktrainer.ui;

import callmemaple.ticktrainer.CycleState;
import callmemaple.ticktrainer.CycleStatus;
import callmemaple.ticktrainer.Error;
import callmemaple.ticktrainer.TickTrainerConfig;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static callmemaple.ticktrainer.CycleStatus.*;

public class TickTrainerOverlay extends OverlayPanel
{
    private final TickTrainerConfig config;

    @Inject
    private CycleState cycleState;

    @Inject
    private TickTrainerOverlay(TickTrainerConfig config)
    {
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        switch (config.displayMode())
        {
            case VERBOSE:
                return renderVerbose(graphics);
            case MINIMAL:
                return renderMinimal(graphics);
            case VISUAL:
                return renderVisual(graphics);
            default:
                return panelComponent.render(graphics);
        }
    }

    private Dimension renderVisual(Graphics2D graphics)
    {
        return panelComponent.render(graphics);
    }

    private Dimension renderMinimal(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        List<CycleStatus> displayStatuses = new ArrayList<>(Arrays.asList(config.errorsOnly() ? new CycleStatus[]{ERROR} : values()));
        if (!displayStatuses.contains(cycleState.getStatus()))
        {
            return panelComponent.render(graphics);
        }

        switch (cycleState.getStatus())
        {
            case ON_CYCLE:
                panelComponent.getChildren().add((LineComponent.builder())
                        .left("on cycle tick: " + cycleState.getTickCount())
                        .leftColor(config.onCycleColor())
                        .build());
                break;
            case IDLE:
                panelComponent.getChildren().add((LineComponent.builder())
                        .left("Idle...")
                        .leftColor(config.idleColor())
                        .build());
                break;
            case WAITING_FOR_ANIMATION:
                panelComponent.getChildren().add((LineComponent.builder())
                        .left("Waiting for animation")
                        .build());
                break;
            case ERROR:
                for (Error error: cycleState.getErrors())
                {
                    panelComponent.getChildren().add((LineComponent.builder())
                            .left("error: " + error.getMessage(config.displayMode()))
                            .leftColor(config.errorColor())
                            .build());
                }
                break;
            case LOCKED_OUT:
                Color lockedOutColor = cycleState.getLockedOutTimer() >= 1 ? config.errorColor() : Color.PINK;
                panelComponent.getChildren().add((LineComponent.builder())
                        .left("Locked out for " + (int) Math.ceil(cycleState.getLockedOutTimer()))
                        .leftColor(lockedOutColor)
                        .build());
                break;
        }

        return panelComponent.render(graphics);
    }

    private Dimension renderVerbose(Graphics2D graphics)
    {
        return panelComponent.render(graphics);

    }
}