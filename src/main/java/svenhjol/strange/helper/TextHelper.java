package svenhjol.strange.helper;

/**
 * TODO: Merge me into Charmony!
 */
public class TextHelper {
    public static String uncapitalize(String string) {
        if (string != null && !string.isEmpty()) {
            var str = string.substring(0, 1).toLowerCase();
            return str + string.substring(1);
        } else {
            return string;
        }
    }
}
