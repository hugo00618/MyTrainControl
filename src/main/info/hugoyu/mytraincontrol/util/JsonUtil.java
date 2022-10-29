package info.hugoyu.mytraincontrol.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class JsonUtil {

    private static final Gson gson;

    static {
        gson = new Gson();
    }

    public static <T> T parseJSON(String filePath, Class<T> type) throws IOException {
        JsonReader reader = new JsonReader(new FileReader(filePath));
        T layout = gson.fromJson(reader, type);
        reader.close();

        return layout;
    }

    public static void writeJSON(String filePath, Object object) throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(object, writer);
        }
    }

}
