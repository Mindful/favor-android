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
