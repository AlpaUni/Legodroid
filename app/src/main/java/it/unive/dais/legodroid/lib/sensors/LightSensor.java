package it.unive.dais.legodroid.lib.sensors;

import java.io.IOException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.Const;

public class LightSensor extends AbstractSensor {
    public LightSensor(EV3.Api api, EV3.InputPort port) {
        super(api, port);
    }

    public Future<Integer> getReflected() throws IOException {
        Future<short[]> f = api.getPercentValue(port, Const.EV3_COLOR, Const.COL_REFLECT, 1);
        return api.execAsync(() -> (int) f.get()[0]);
    }

    public int getAmbient() {
        return 0;
    }

    public Color getColor() {
        return Color.BLACK;
    }

    public int getRawR() {
        return 0;
    }

    public int getRawG() {
        return 0;
    }

    public int getRawB() {
        return 0;
    }

    public enum Color {
        TRANSPARENT,
        BLACK,
        BLUE,
        GREEN,
        YELLOW,
        RED,
        WHITE,
        BROWN
    }

}
