package info.hugoyu.mytraincontrol.trainset;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class TrainsetProfileParser {

    public static TrainsetProfile parseJSON(String filename) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader("trainset-profiles/" + filename));
        TrainsetProfile profile = gson.fromJson(reader, TrainsetProfile.class);
        return profile;
    }

}
