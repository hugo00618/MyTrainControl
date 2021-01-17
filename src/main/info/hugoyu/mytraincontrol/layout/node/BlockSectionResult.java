package info.hugoyu.mytraincontrol.layout.node;

import lombok.Builder;
import lombok.Getter;

/**
 * Output for alloc() & free()
 */
@Builder
@Getter
public class BlockSectionResult {
    private int remainingDist; // remaining distance to alloc/free
    private boolean isSectionComplete; // if there is no remaining distance to alloc/free for the caller
}
