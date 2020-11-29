package ml.karmaconfigs.modpackupdater.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class Text {

    private static String text;
    private static String formatted;

    /**
     * Initialize the text class
     *
     * @param _text the text
     */
    public Text(@NotNull final String _text) {
        text = _text;
    }

    /**
     * Initialize the text class
     *
     * @param error the error
     */
    public Text(@NotNull final Throwable error) {
        Throwable prefix = new Throwable(error);
        String pr = prefix.fillInStackTrace().toString();
        List<String> lines = new ArrayList<>();
        for (StackTraceElement element : error.getStackTrace()) {
            String format = "                                 " + element;
            lines.add(format
                    .replace("[", "{open}")
                    .replace("]", "{close}")
                    .replace(",", "{comma}"));
        }

        text = pr + "<br>" + lines.toString()
                .replace("[", "")
                .replace("]", "")
                .replace(",", "<br>")
                .replace("{open}", "[")
                .replace("{close}", "]")
                .replace("{comma}", ",");
    }

    /**
     * Get the text or formatted text
     * if specified
     *
     * @param _formatted if should return the formatted
     *                  text
     * @return the text or formatted text
     */
    @NotNull
    public final String getText(final boolean _formatted) {
        return _formatted && formatted != null && !formatted.isEmpty() ? formatted : text;
    }

    /**
     * Format the text (Just uses html)
     *
     * @param color the color to use
     * @param size the size of the text
     */
    public final void format(@NotNull final Color color, final int size) {
        String hex = String.format( "#%02X%02X%02X",
                (int)( color.getRed() * 255 ),
                (int)( color.getGreen() * 255 ),
                (int)( color.getBlue() * 255 ) );

        formatted = "<span style=\"color: " + hex + /*"; font-size: " + size + */"\">" + text + "</span>";
    }

    public interface util {

        /**
         * Create text instance without using
         * new Text(java.lang.String); and
         * with the color and size pre-set
         *
         * @param text the text
         * @param color the color of the text
         * @param size the size of the text
         * @return a Text instance
         */
        @NotNull
        static Text create(@NotNull final String text, @NotNull final Color color, final int size) {
            Text text_util = new Text(text);
            text_util.format(color, size);

            return text_util;
        }
    }
}
