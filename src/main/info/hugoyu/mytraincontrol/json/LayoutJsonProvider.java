package info.hugoyu.mytraincontrol.json;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import info.hugoyu.mytraincontrol.layout.Layout;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class LayoutJsonProvider {

    public static Layout parseJSON(String filename) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader("layout-profiles/" + filename));
        Layout layout = gson.fromJson(reader, Layout.class);
        layout.postDeserialization();
        return layout;
    }

}
