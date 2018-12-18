package multi.yoramg.charting.interfaces.dataprovider;

import multi.yoramg.charting.components.YAxis.AxisDependency;
import multi.yoramg.charting.data.BarLineScatterCandleBubbleData;
import multi.yoramg.charting.utils.Transformer;

public interface BarLineScatterCandleBubbleDataProvider extends ChartInterface {

    Transformer getTransformer(AxisDependency axis);
    boolean isInverted(AxisDependency axis);
    
    float getLowestVisibleX();
    float getHighestVisibleX();

    BarLineScatterCandleBubbleData getData();
}
