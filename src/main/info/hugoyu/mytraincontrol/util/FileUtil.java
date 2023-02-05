package info.hugoyu.mytraincontrol.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class FileUtil {

    private static final Gson gson;

    static {
        gson = new Gson();
    }

    public static void writeJson(String filePath, Object object) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(object, writer);
        } catch (IOException e) {
            throw new RuntimeException(String.format("failed to write file: %s", filePath), e);
        }
    }

    public static <T> T readJson(String filePath, Class<T> type) {
        try (JsonReader reader = new JsonReader(new FileReader(filePath))) {
            T layout = gson.fromJson(reader, type);
            reader.close();

            return layout;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error parsing file %s to %s", filePath, type), e);
        }
    }

}
