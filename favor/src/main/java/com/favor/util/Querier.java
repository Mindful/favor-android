/*
 * Copyright (C) 2015  Joshua Tanner (mindful.jt@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
