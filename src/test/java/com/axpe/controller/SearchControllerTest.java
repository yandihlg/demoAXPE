package com.axpe.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.test.context.EmbeddedKafka;

import com.axpe.datamodel.SearchRequest;
import com.axpe.datamodel.SearchResult;
import com.axpe.service.SearchService;

@EmbeddedKafka(partitions = 1, topics = { "hotel_availability_searches", "hotel_search_responses" })
public class SearchControllerTest {

	@Mock
	private SearchService searchService;

	@InjectMocks
	private SearchController searchController;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testCreateSearch() throws Exception {
		SearchRequest searchRequest = new SearchRequest("1234aBc", "29/12/2023", "31/12/2023",
				Collections.singletonList(30));
		CompletableFuture<Map<String, String>> future = new CompletableFuture<>();
		future.complete(Collections.singletonMap("searchId", "mockSearchId"));
		doNothing().when(searchService).sendSearchRequest(searchRequest);

		CompletableFuture<Map<String, String>> resultFuture = searchController.createSearch(searchRequest);
		Map<String, String> result = resultFuture.get();

		assertNotNull(result);
		assertEquals("mockSearchId", result.get("searchId"));
	}

	@Test
	void testGetCount() {
		String searchId = "mockSearchId";
		SearchResult searchResult = new SearchResult(searchId, "1234aBc", "29/12/2023", "31/12/2023",
				Collections.singletonList(30), 100);
		when(searchService.getSearchCount(searchId)).thenReturn(searchResult);

		Map<String, Object> result = searchController.getCount(searchId);

		assertNotNull(result);
		assertEquals(searchId, result.get("searchId"));
		assertEquals(100, result.get("count"));
		assertEquals(searchResult.getHotelId(), ((Map) result.get("search")).get("hotelId"));
	}

}
