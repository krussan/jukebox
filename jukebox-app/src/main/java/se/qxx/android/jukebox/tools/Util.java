package se.qxx.android.jukebox.tools;

import java.util.function.Supplier;

public class Util {
    public static <T> T nullSafeGetter(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (NullPointerException n) {
            return null;
        }
    }

}
