package com.favor.util;

import com.favor.library.Core;
import com.favor.library.Processor;
import com.favor.ui.GraphableResult;

/**
 * Created by josh on 2/2/15.
 */
public class Querier {
    public static enum AnalyticType {None, Charcount, Messagecount, ResponseTime}


    public static GraphableResult launchQuery(QueryDetails input){
        switch(input.getAnalyticType()){
            case Charcount:
                return new GraphableResult(input,
                        Processor.batchCharCount(Core.getCurrentAccount(), input.getContacts(), input.getStartDate(), input.getEndDate(), true),
                        Processor.batchCharCount(Core.getCurrentAccount(), input.getContacts(), input.getStartDate(), input.getEndDate(), true));
            case Messagecount:
                return new GraphableResult(input,
                        Processor.batchMessageCount(Core.getCurrentAccount(), input.getContacts(), input.getStartDate(), input.getEndDate(), true),
                        Processor.batchMessageCount(Core.getCurrentAccount(), input.getContacts(), input.getStartDate(), input.getEndDate(), true));
            case ResponseTime:
                return new GraphableResult(input,
                        Processor.batchCharCount(Core.getCurrentAccount(), input.getContacts(), input.getStartDate(), input.getEndDate(), true),
                        Processor.batchCharCount(Core.getCurrentAccount(), input.getContacts(), input.getStartDate(), input.getEndDate(), true));
            default:
                return new GraphableResult(new QueryDetails(), new long[]{}, new long[]{});
        }
    }
}
