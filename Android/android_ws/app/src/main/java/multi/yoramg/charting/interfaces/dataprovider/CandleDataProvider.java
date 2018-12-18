package multi.yoramg.charting.interfaces.dataprovider;

import multi.yoramg.charting.data.CandleData;

public interface CandleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    CandleData getCandleData();
}
