package com.axpe.controller;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.axpe.datamodel.SearchRequest;
import com.axpe.datamodel.SearchResult;
import com.axpe.service.SearchService;

@RestController
@RequestMapping("/search")
public class SearchController {

	@Autowired
	private SearchService searchService;

	private final ConcurrentHashMap<String, CompletableFuture<Map<String, String>>> futures = new ConcurrentHashMap<>();

	@PostMapping
	public CompletableFuture<Map<String, String>> createSearch(@RequestBody SearchRequest searchRequest) {

		String futureKey = searchRequest.getHotelId() + searchRequest.getCheckIn() + searchRequest.getCheckOut();
		CompletableFuture<Map<String, String>> future = new CompletableFuture<>();
		futures.put(futureKey, future);
		searchService.sendSearchRequest(searchRequest);
		return future;

	}

	@KafkaListener(topics = "hotel_search_responses", groupId = "group_id")
	public void handleSearchResult(SearchResult searchResult) {
		String futureKey = searchResult.getHotelId() + searchResult.getCheckIn() + searchResult.getCheckOut();
		CompletableFuture<Map<String, String>> future = futures.remove(futureKey);
		if (future != null) {
			future.complete(Collections.singletonMap("searchId", searchResult.getSearchId()));
		}
	}

	@GetMapping("/count")
    public Map<String, Object> getCount(@RequestParam String searchId) {
        SearchResult searchResult = searchService.getSearchCount(searchId);
        return Map.of(
            "searchId", searchResult.getSearchId(),
            "search", Map.of(
                "hotelId", searchResult.getHotelId(),
                "checkIn", searchResult.getCheckIn(),
                "checkOut", searchResult.getCheckOut(),
                "ages", searchResult.getAges()
            ),
            "count", searchResult.getCount()
        );
    }
}
