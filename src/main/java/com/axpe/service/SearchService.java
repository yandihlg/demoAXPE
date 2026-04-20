package com.axpe.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.axpe.datamodel.SearchRequest;
import com.axpe.datamodel.SearchResult;
import com.axpe.repository.SearchResultRepository;

@Service
public class SearchService {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private SearchResultRepository searchResultRepository;

	public void sendSearchRequest(SearchRequest searchRequest) {
		kafkaTemplate.send("hotel_availability_searches", searchRequest);
	}

	public SearchResult getSearchCount(String searchId) {
		Optional<SearchResult> searchResult = searchResultRepository.findById(searchId);
		return searchResult.orElseThrow(() -> new RuntimeException("Search not found"));
	}

}
