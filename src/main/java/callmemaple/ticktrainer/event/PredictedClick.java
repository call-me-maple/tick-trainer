package callmemaple.ticktrainer.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Comparator;

@Getter
@AllArgsConstructor
public class PredictedClick
{
    private final Click click;
    private final int predictedTick;
    private final long timestamp;
}
