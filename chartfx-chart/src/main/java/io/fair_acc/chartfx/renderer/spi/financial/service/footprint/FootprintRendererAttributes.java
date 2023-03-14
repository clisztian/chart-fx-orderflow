package io.fair_acc.chartfx.renderer.spi.financial.service.footprint;

import io.fair_acc.chartfx.renderer.spi.financial.css.FinancialColorSchemeConstants;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import io.fair_acc.dataset.spi.financial.api.attrs.AttributeKey;
import io.fair_acc.dataset.spi.financial.api.attrs.AttributeModel;

public class FootprintRendererAttributes extends AttributeModel {
    /** Column coloring group feature active, default true */
    public static final AttributeKey<Boolean> COLUMN_COLORING_FEATURE_ACTIVE = AttributeKey.create(Boolean.class, "COLUMN_COLORING_FEATURE_ACTIVE");

    /** Draw pullback column, default true */
    public static final AttributeKey<Boolean> DRAW_PULLBACK_COLUMN = AttributeKey.create(Boolean.class, "DRAW_PULLBACK_COLUMN");

    /** Draw rectangle of POC of each bar, default true */
    public static final AttributeKey<Boolean> DRAW_POC_RECTANGLE_OF_EACH_BAR = AttributeKey.create(Boolean.class, "DRAW_POC_RECTANGLE_OF_EACH_BAR");

    /**
     * Column color group settings:
     * 1st column bid [0, 1, 2, 3] groups
     * 2nd column ask [0, 1, 2, 3] groups
     */
    public static final AttributeKey<Color[][]> COLUMN_COLOR_GROUP_SETTINGS = AttributeKey.create(Color[][].class, "COLUMN_COLOR_GROUP_SETTINGS");

    /**
     * Column color group thresholds:
     * three thresholds for calculation of column color group choosing process
     */
    public static final AttributeKey<Double[]> COLUMN_COLOR_GROUP_THRESHOLDS = AttributeKey.create(Double[].class, "COLUMN_COLOR_GROUP_THRESHOLDS");

    /** Bolding/Plain font bid/ask under defined threshold, 0 means disabled, default 30 */
    public static final AttributeKey<Double> BID_ASK_BOLD_THRESHOLD = AttributeKey.create(Double.class, "BID_ASK_BOLD_THRESHOLD");

    /**
     * Bid/Ask volume fonts:
     * 0 - plain normal font, number is less BID_ASK_BOLD_THRESHOLD
     * 1 - bold normal font, number is higher or equal than BID_ASK_BOLD_THRESHOLD
     * 2 - bold big font, the number is last bar and last price
     */
    public static final AttributeKey<Font[]> BID_ASK_VOLUME_FONTS = AttributeKey.create(Font[].class, "BID_ASK_VOLUME_FONTS");

    /**
      * Configure Footprint by default values. Good practise is create these defaults and apply your changes to this instance by direct call setAttribute method.
      *
      * @param scheme the coloring scheme
      * @return define default values
      */
    public static FootprintRendererAttributes getDefaultValues(String scheme) {
        FootprintRendererAttributes model = new FootprintRendererAttributes();

        model.setAttribute(COLUMN_COLORING_FEATURE_ACTIVE, true);

        model.setAttribute(DRAW_POC_RECTANGLE_OF_EACH_BAR, true);

        model.setAttribute(DRAW_PULLBACK_COLUMN, true);

        model.setAttribute(COLUMN_COLOR_GROUP_THRESHOLDS, new Double[] { 40.0d, 100.0d, 150.0d });

        model.setAttribute(BID_ASK_BOLD_THRESHOLD, 30.0d);

        model.setAttribute(BID_ASK_VOLUME_FONTS, new Font[] {
                                                         Font.font("Segoe UI", FontWeight.NORMAL, 13), // plain normal font, number is less BID_ASK_BOLD_THRESHOLD
                                                         Font.font("Segoe UI", FontWeight.BOLD, 12), // bold normal font, number is higher or equal than BID_ASK_BOLD_THRESHOLD
                                                         Font.font("Segoe UI", FontWeight.BOLD, 15) // bold big font, the number is last bar and last price
                                                 });

        Color[][] columnColorGroupSettings;
        switch (scheme) {
        case FinancialColorSchemeConstants.SAND:
        case FinancialColorSchemeConstants.CLASSIC:
        case FinancialColorSchemeConstants.CLEARLOOK:
            columnColorGroupSettings = new Color[][] {
                {
                        Color.rgb(194, 54, 92), // RANGE 0 BID COLOR, color: light blue
                        Color.rgb(210, 88, 103), // RANGE 1 BID COLOR, color: white
                        Color.rgb(201, 102, 127), // RANGE 2 BID COLOR, color: pink
                        Color.rgb(255, 102, 102) // RANGE 3 BID COLOR, color: red
                },
                {
                        Color.rgb(152, 191, 238), // RANGE 0 ASK COLOR, color: light blue
                        Color.rgb(125, 172, 255), // RANGE 1 ASK COLOR, color: white
                        Color.rgb(100, 157, 255), // RANGE 2 ASK COLOR, color: light green
                        Color.rgb(52, 125, 250) // RANGE 3 ASK COLOR, color: green
                }
            };
            break;
        default:
            columnColorGroupSettings = new Color[][] {
                {
                        Color.rgb(194, 54, 92), // RANGE 0 BID COLOR, color: light blue
                        Color.rgb(210, 88, 103), // RANGE 1 BID COLOR, color: white
                        Color.rgb(201, 102, 127), // RANGE 2 BID COLOR, color: pink
                        Color.rgb(255, 102, 102) // RANGE 3 BID COLOR, color: red
                },
                {
                        Color.rgb(152, 191, 238), // RANGE 0 ASK COLOR, color: light blue
                        Color.rgb(125, 172, 255), // RANGE 1 ASK COLOR, color: white
                        Color.rgb(100, 157, 255), // RANGE 2 ASK COLOR, color: light green
                        Color.rgb(52, 125, 250) // RANGE 3 ASK COLOR, color: green
                }
            };
            break;
        }
        model.setAttribute(COLUMN_COLOR_GROUP_SETTINGS, columnColorGroupSettings);

        return model;
    }
}
