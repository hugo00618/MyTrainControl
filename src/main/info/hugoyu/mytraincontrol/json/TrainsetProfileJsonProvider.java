package info.hugoyu.mytraincontrol.json;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import info.hugoyu.mytraincontrol.trainset.TrainsetProfile;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class TrainsetProfileJsonProvider {

    public static TrainsetProfile parseJSON(String filename) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader("trainset-profiles/" + filename));
        TrainsetProfile profile = gson.fromJson(reader, TrainsetProfile.class);
        profile.postDeserialization();
        return profile;
    }

}
