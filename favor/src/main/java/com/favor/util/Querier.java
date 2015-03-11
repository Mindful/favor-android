package com.favor.util;

import com.favor.library.Core;
import com.favor.library.Logger;
import com.favor.library.Processor;
import com.favor.ui.GraphableResult;

/**
 * Created by josh on 2/2/15.
 */
public class Querier {
    public static enum AnalyticType {None, Charcount, Messagecount, ResponseTime}
    public static final AnalyticType DEFAULT_ANALYTIC = AnalyticType.Charcount;


    public static String analyticName(AnalyticType t){
        switch(t){
            case Charcount:
                return "Total Character Count";
            case Messagecount:
                return "Total Message Count";
            case ResponseTime:
                return "Conversational Response Time";
            default:
                return "No Metric";
        }
    }


    public static GraphableResult launchQuery(QueryDetails input){
        switch(input.getAnalyticType()){
            case Charcount:
                Logger.info("Charcount");
                return new GraphableResult(input,
                        Processor.batchCharCount(Core.getCurrentAccount(), input.getContacts(), input.getStartDate(), input.getEndDate(), true),
                        Processor.batchCharCount(Core.getCurrentAccount(), input.getContacts(), input.getStartDate(), input.getEndDate(), false));
            case Messagecount:
                Logger.info("Messagecount");
                return new GraphableResult(input,
                        Processor.batchMessageCount(Core.getCurrentAccount(), input.getContacts(), input.getStartDate(), input.getEndDate(), true),
                        Processor.batchMessageCount(Core.getCurrentAccount(), input.getContacts(), input.getStartDate(), input.getEndDate(), false));
            case ResponseTime:
                Logger.info("Responsetime");
                return new GraphableResult(input,
                        Processor.batchCharCount(Core.getCurrentAccount(), input.getContacts(), input.getStartDate(), input.getEndDate(), true),
                        Processor.batchCharCount(Core.getCurrentAccount(), input.getContacts(), input.getStartDate(), input.getEndDate(), false));
            default:
                Logger.info("Default result type - empty");
                return new GraphableResult(new QueryDetails(), new long[]{}, new long[]{});
        }
    }
}
