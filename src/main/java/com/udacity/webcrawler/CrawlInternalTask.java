package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

public class CrawlInternalTask extends RecursiveAction {
    List<Pattern> ignoredUrls;
    PageParserFactory parserFactory;
    String url;
    Instant deadline;
    int maxDepth;
    Map<String, Integer> counts;
    Set<String> visitedUrls;
    List<String> startingUrls;
    Clock clock;
    int popularWordCount;

    public CrawlInternalTask(String url, Instant deadline, int maxDepth, List<Pattern> ignoredUrls, Map<String, Integer> counts, Set<String> visitedUrls,
                             PageParserFactory parserFactory, int popularWordCount, Clock clock) {
        this.parserFactory = parserFactory;
        this.popularWordCount = popularWordCount;
        this.maxDepth = maxDepth;
        this.ignoredUrls = ignoredUrls;
        this.url = url;
        this.clock = clock;
        this.visitedUrls = visitedUrls;
        this.counts = counts;
        this.deadline = deadline;
    }


    @Override
    protected void compute() {
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return;
        }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }
        if (visitedUrls.contains(url)) {
            return;
        }
        visitedUrls.add(url);
        PageParser.Result result = parserFactory.get(url).parse();
        for (ConcurrentMap.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            if (counts.containsKey(e.getKey())) {
                counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
            } else {
                counts.put(e.getKey(), e.getValue());
            }
        }
        List<CrawlInternalTask> tasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            tasks.add(new CrawlInternalTask(link, deadline, maxDepth - 1, ignoredUrls, counts, visitedUrls, parserFactory, popularWordCount, clock));
        }
        invokeAll(tasks);

    }
}


