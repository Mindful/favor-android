package com.favor.graphs;

import android.graphics.Canvas;
import com.favor.library.Logger;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.HorizontalBarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.List;

/**
 * Created by josh on 7/9/16.
 */
public class HorizontalBarChartSmartValueRenderer extends HorizontalBarChartRenderer {

    public HorizontalBarChartSmartValueRenderer(BarDataProvider chart, ChartAnimator animator,
                                      ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
    }

    @Override
    public void drawValues(Canvas c) {
        List<IBarDataSet> dataSets = mChart.getBarData().getDataSets();

        final float valueOffsetPlus = Utils.convertDpToPixel(5f);
        float posOffset = 0f;

        float max = 0;

        //Get largest value
        for (int i = 0; i < mChart.getBarData().getDataSetCount(); i++){
            for (int j = 0; j < dataSets.get(i).getEntryCount(); j++){
                float val = dataSets.get(i).getEntryForIndex(j).getY();
                max = val > max ? val : max;
            }
        }

        for (int i = 0; i < mChart.getBarData().getDataSetCount(); i++) {

            IBarDataSet dataSet = dataSets.get(i);
            if (!dataSet.isDrawValuesEnabled()){
                continue;
            }

            // apply the text-styling defined by the DataSet
            applyValueTextStyle(dataSet);
            final float halfTextHeight = Utils.calcTextHeight(mValuePaint, "10") / 2f;

            ValueFormatter formatter = dataSet.getValueFormatter();

            // get the buffer
            BarBuffer buffer = mBarBuffers[i];

            // only support single chart drawing


            for (int j = 0; j < buffer.buffer.length * mAnimator.getPhaseX(); j += 4) {

                float y = (buffer.buffer[j + 1] + buffer.buffer[j + 3]) / 2f;

                if (!mViewPortHandler.isInBoundsTop(buffer.buffer[j + 1]))
                    break;

                if (!mViewPortHandler.isInBoundsX(buffer.buffer[j]))
                    continue;

                if (!mViewPortHandler.isInBoundsBottom(buffer.buffer[j + 1]))
                    continue;

                BarEntry e = dataSet.getEntryForIndex(j / 4);
                float val = e.getY();
                String formattedValue = formatter.getFormattedValue(val, e, i, mViewPortHandler);

                // calculate the correct offset depending on the draw position of the value
                float valueTextWidth = Utils.calcTextWidth(mValuePaint, formattedValue);
                boolean drawValueAboveBar = !(val > max / 2);
                posOffset = (drawValueAboveBar ? valueOffsetPlus : -(valueTextWidth + valueOffsetPlus));
                float xPos = buffer.buffer[j + 2] + posOffset;
                float yPos = y + halfTextHeight;


                drawValue(c, formattedValue, xPos, yPos, dataSet.getValueTextColor(j / 2));
            }
        }
    }
}
