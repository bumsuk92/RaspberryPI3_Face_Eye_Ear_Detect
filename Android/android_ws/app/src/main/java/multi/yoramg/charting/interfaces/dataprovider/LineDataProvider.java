package multi.yoramg.charting.interfaces.dataprovider;

import multi.yoramg.charting.components.YAxis;
import multi.yoramg.charting.data.LineData;

public interface LineDataProvider extends BarLineScatterCandleBubbleDataProvider {

    LineData getLineData();

    YAxis getAxis(YAxis.AxisDependency dependency);
}
