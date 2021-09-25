package info.hugoyu.mytraincontrol;

import info.hugoyu.mytraincontrol.util.LayoutUtil;
import org.junit.jupiter.api.BeforeEach;

public class LayoutTestBase {

    @BeforeEach
    public void setUp() {
        LayoutUtil.registerLayout("src/tst/layout.json");
    }
}
