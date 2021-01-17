package info.hugoyu.mytraincontrol.json;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import info.hugoyu.mytraincontrol.layout.Layout;

import java.io.FileReader;
import java.io.IOException;

public class LayoutJsonProvider {

    public static Layout parseJSON(String filename) throws IOException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader("layout-profiles/" + filename));
        Layout layout = gson.fromJson(reader, Layout.class);
        reader.close();
        layout.postDeserialization();
        return layout;
    }

}
