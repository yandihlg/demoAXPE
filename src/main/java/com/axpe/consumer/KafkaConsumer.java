package com.axpe.consumer;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.axpe.datamodel.SearchRequest;
import com.axpe.datamodel.SearchResult;
import com.axpe.repository.SearchResultRepository;

@Service
public class KafkaConsumer {

	@Autowired
	private SearchResultRepository searchResultRepository;

	@Autowired
	private KafkaTemplate<String, SearchResult> kafkaTemplate;

	@KafkaListener(topics = "hotel_availability_searches", groupId = "group_id")
	public void consume(SearchRequest searchRequest) {
		Optional<SearchResult> existingResult = searchResultRepository.findByHotelId(searchRequest.getHotelId());
		SearchResult searchResult;
		if (existingResult.isPresent()) {
			// Incrementar el conteo usando el metodo inmutable
			searchResult = existingResult.get().addCount();
		} else {
			String searchId = UUID.randomUUID().toString();
			searchResult = new SearchResult(searchId, searchRequest.getHotelId(), searchRequest.getCheckIn(),
					searchRequest.getCheckOut(), searchRequest.getAges(), 1);
		}

		searchResultRepository.save(searchResult);
		kafkaTemplate.send("hotel_search_responses", searchResult);
	}

}
