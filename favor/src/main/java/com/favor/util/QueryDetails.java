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

import com.favor.library.Contact;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by josh on 1/31/15.
 */
public class QueryDetails implements Serializable {

    public static final long DEFAULT_DATE = -1;

    @Override
    public String toString() {
        return "QueryDetails{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", analyticType=" + analyticType +
                ", contacts=" + contacts +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryDetails that = (QueryDetails) o;

        if (endDate != that.endDate) return false;
        if (startDate != that.startDate) return false;
        if (analyticType != that.analyticType) return false;
        if (contacts != null ? !contacts.equals(that.contacts) : that.contacts != null) return false;

        return true;
    }

    public long getStartDate() { return startDate; }
    public long getEndDate() {
        return endDate;
    }
    public Querier.AnalyticType getAnalyticType() {
        return analyticType;
    }
    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }


    public void setAnalyticType(Querier.AnalyticType analyticType) {
        this.analyticType = analyticType;
    }


    public void setContacts(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }

    public void resetStartDate(){
        startDate = DEFAULT_DATE;
    }

    public void resetEndDate(){
        endDate = DEFAULT_DATE;
    }

    private long startDate;
    private long endDate;
    private Querier.AnalyticType analyticType;
    private ArrayList<Contact> contacts;


    public QueryDetails(QueryDetails right){
        this.startDate = right.startDate;
        this.endDate = right.endDate;
        this.analyticType = right.analyticType;
        this.contacts = (ArrayList<Contact>) right.contacts.clone();
    }

    public QueryDetails(){
        startDate = -1;
        endDate = -1;
        analyticType = Querier.DEFAULT_ANALYTIC;
        contacts = new ArrayList<Contact>();
    }

    public QueryDetails(long startDate, long endDate,  ArrayList<Contact> contacts, Querier.AnalyticType analyticType) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.analyticType = analyticType;
        this.contacts = contacts;
    }

    @Override
    public Object clone() {
        return new QueryDetails(this);
    }


}
