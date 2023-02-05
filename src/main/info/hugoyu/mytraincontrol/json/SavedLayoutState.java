package info.hugoyu.mytraincontrol.json;

import info.hugoyu.mytraincontrol.layout.Vector;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class SavedLayoutState {

    Map<Integer, Vector> occupationState;

}
