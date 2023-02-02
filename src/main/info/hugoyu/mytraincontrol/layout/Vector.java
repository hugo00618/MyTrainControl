package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.json.layout.VectorJson;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class Vector {
    private final long id0, id1;

    public Vector(VectorJson vectorJson) {
        this(vectorJson.getId0(), vectorJson.getId1());
    }

    public Vector reversed() {
        return new Vector(id1, id0);
    }

}
