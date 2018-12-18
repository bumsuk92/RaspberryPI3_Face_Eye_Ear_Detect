package multi.yoramg.charting.interfaces.dataprovider;

import multi.yoramg.charting.data.BubbleData;

public interface BubbleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    BubbleData getBubbleData();
}
