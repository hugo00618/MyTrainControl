package info.hugoyu.mytraincontrol.layout;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Output for alloc() & free()
 */
@AllArgsConstructor
@Getter
public class BlockSectionResult {
    private int consumedDist;                   // distance alloc'ed/freed
    private int remainingDist;                  // remaining distance to alloc/free
    private boolean isEntireSectionConsumed;    // has the entire section been alloc'ed/free'd
}
