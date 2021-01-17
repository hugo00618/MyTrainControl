package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.json.LayoutJsonProvider;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public class LayoutTestBase {

    protected Layout layout;

    @BeforeEach
    public void setUp() throws IOException {
        layout = LayoutJsonProvider.parseJSON("layout.json");
    }

}
