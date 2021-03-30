package net.fabricmc.loader.launch.knot;

public class FaucetKnotHelper {

    public static Knot createKnot() {
        return new Knot(null);
    }

    public static void initKnot(Knot knot) {
        knot.init(new String[0]);
    }

}
