//package com.github.tomakehurst.wiremock.verification;
//
//import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
//import com.github.tomakehurst.wiremock.stubbing.StubMapping;
//import com.google.common.base.Predicate;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentLinkedQueue;
//
//public class JournalConcurrentLinkedQueue extends PersistJournalRequestWrapper{
//
//    private final Queue<ServeEvent> serveEvents = new ConcurrentLinkedQueue<ServeEvent>();
//
//
//    public void add(ServeEvent serveEvent) {
//        serveEvents.add(serveEvent);
//    }
//
//    public void remove(ServeEvent event) {
//        serveEvents.remove(event);
//    }
//
//    public void clear() {
//        serveEvents.clear();
//    }
//
//}
