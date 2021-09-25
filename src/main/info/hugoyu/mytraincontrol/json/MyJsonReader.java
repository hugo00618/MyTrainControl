package info.hugoyu.mytraincontrol.json;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;

public class MyJsonReader {

    private static Gson gson;

    public static <T> T parseJSON(String filePath, Class<T> type) throws IOException {
        if (gson == null) {
            gson = new Gson();
        }

        JsonReader reader = new JsonReader(new FileReader(filePath));
        T layout = gson.fromJson(reader, type);
        reader.close();

        return layout;
    }

}
