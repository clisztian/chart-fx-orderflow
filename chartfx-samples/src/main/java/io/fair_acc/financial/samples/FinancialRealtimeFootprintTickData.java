package io.fair_acc.financial.samples;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.renderer.spi.financial.FootprintRenderer;
import io.fair_acc.chartfx.renderer.spi.financial.css.FinancialColorSchemeConstants;
import io.fair_acc.chartfx.renderer.spi.financial.service.footprint.FootprintRendererAttributes;
import io.fair_acc.dataset.spi.DefaultDataSet;
import io.fair_acc.dataset.spi.financial.OhlcvDataSet;
import io.fair_acc.financial.samples.service.addon.AbsorptionConsolidationAddon;
import io.fair_acc.financial.samples.service.consolidate.OhlcvConsolidationAddon;
import io.fair_acc.financial.samples.service.footprint.AbsorptionClusterRendererPaintAfterEP;
import io.fair_acc.financial.samples.service.footprint.DiagonalDominantNbColumnColorGroupService;
import io.fair_acc.financial.samples.service.footprint.FootprintRenderedAPIAdapter;
import io.fair_acc.financial.samples.service.period.IntradayPeriod;

import java.util.HashMap;

import static io.fair_acc.financial.samples.service.period.IntradayPeriod.IntradayPeriodEnum.M;

/**
 * Tick FOOTPRINT realtime processing. Demonstration of re-sample data to 2M timeframe.
 * Support/Resistance range levels added.
 * YWatchValueIndicator for better visualization of y-values, auto-handling of close prices and manual settings of price levels.
 *
 * @author afischer
 */
public class FinancialRealtimeFootprintTickData extends FinancialRealtimeOrderFlowSample {
    @Override
    protected void configureApp() {
        title = "Novartis AG (NOVN) Footprint Tick Data";
        theme = FinancialColorSchemeConstants.DARK;
        resource = "REALTIME_OHLC_TICK";
        timeRange = "2022/02/09 09:02-2022/02/09 16:25";  //2022-02-09 10:34:57.022183000
        tt = "00:00-23:59"; // time template whole day session
        replayFrom = "2022/02/09 10:25";
        // price consolidation addons (extensions)
        consolidationAddons = new HashMap<>();
        consolidationAddons.put("footprintCalcAddons", new OhlcvConsolidationAddon[] {
                                                               new AbsorptionConsolidationAddon(false, 70, 3, 0.33d, 100.0d) });
        // parameter extendedCalculation ensures calculation of all necessary data for footprints features
        // parameter calculationAddonServicesType: possible add addon services for specific footprint additional features paintings
        period = new IntradayPeriod(M, 2.0, 0.0, true, "footprintCalcAddons");
    }

    protected void prepareRenderers(XYChart chart, OhlcvDataSet ohlcvDataSet, DefaultDataSet indiSet) {
        // configure footprint attributes (create defaults, and modify it by .setAttribute() methods
        FootprintRendererAttributes footprintAttrs = FootprintRendererAttributes.getDefaultValues(theme);

        // create and apply renderers
        FootprintRenderer renderer = new FootprintRenderer(
                new FootprintRenderedAPIAdapter(footprintAttrs,
                        new DiagonalDominantNbColumnColorGroupService(footprintAttrs)),
                false,
                true,
                true);

        // example of addition footprint extension point
        renderer.addPaintAfterEp(new AbsorptionClusterRendererPaintAfterEP(ohlcvDataSet, chart));
        renderer.getDatasets().addAll(ohlcvDataSet);

        chart.getRenderers().clear();
        chart.getRenderers().add(renderer);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        launch(args);
    }
}
