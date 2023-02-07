package com.udacity.webcrawler;

import com.google.inject.Inject;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

public class WebCrawlerRecursiveAction extends RecursiveAction {
    List<String> startingUrls;
    Clock clock;
    int popularWordCount;
    Duration timeout;
    private final PageParserFactory parserFactory;
    private final int maxDepth;
    private final List<Pattern> ignoredUrls;

    @Inject
    public WebCrawlerRecursiveAction(
            PageParserFactory parserFactory,
            @MaxDepth int maxDepth,
            @IgnoredUrls List<Pattern> ignoredUrls,
            List<String> startingUrls,
            Clock clock,
            Duration timeout,
            int popularWordCount) {

        this.startingUrls = startingUrls;
        this.clock = clock;
        this.timeout = timeout;
        this.popularWordCount = popularWordCount;
        this.maxDepth = maxDepth;
        this.ignoredUrls = ignoredUrls;
        this.parserFactory = parserFactory;
    }

    @Override
    protected void compute() {


    }

    private void crawlInternal (
            String url,
            Instant deadline,
            int maxDepth,
            Map<String, Integer> counts,
            Set<String> visitedUrls) {
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
        for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            if (counts.containsKey(e.getKey())) {
                counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
            } else {
                counts.put(e.getKey(), e.getValue());
            }
        }
        for (String link : result.getLinks()) {
            crawlInternal(link, deadline, maxDepth - 1, counts, visitedUrls);
        }
    }
}
