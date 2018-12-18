package multi.yoramg.charting.interfaces.dataprovider;

import multi.yoramg.charting.data.ScatterData;

public interface ScatterDataProvider extends BarLineScatterCandleBubbleDataProvider {

    ScatterData getScatterData();
}
