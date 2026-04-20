package com.axpe.consumer;

import com.axpe.datamodel.SearchRequest;
import com.axpe.datamodel.SearchResult;
import com.axpe.repository.SearchResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;

@EmbeddedKafka(partitions = 1, topics = { "hotel_availability_searches", "hotel_search_responses" })
public class KafkaConsumerTest {

	@Mock
    private SearchResultRepository searchResultRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConsume_existingSearchResult() {
        SearchRequest searchRequest = new SearchRequest("1234aBc", "29/12/2023", "31/12/2023", Collections.singletonList(30));
        SearchResult existingResult = new SearchResult("testSearchId", "1234aBc", "29/12/2023", "31/12/2023", Collections.singletonList(30), 1);
        when(searchResultRepository.findByHotelId("1234aBc")).thenReturn(Optional.of(existingResult));
        when(searchResultRepository.save(any(SearchResult.class))).thenReturn(null);
        doNothing().when(kafkaTemplate).send(anyString(), any(SearchResult.class));

        kafkaConsumer.consume(searchRequest);

        verify(searchResultRepository, times(1)).findByHotelId("1234aBc");
        verify(searchResultRepository, times(1)).save(any(SearchResult.class));
        verify(kafkaTemplate, times(1)).send(anyString(), any(SearchResult.class));
    }

    @Test
    public void testConsume_newSearchResult() {
        SearchRequest searchRequest = new SearchRequest("1234aBc", "29/12/2023", "31/12/2023", Collections.singletonList(30));
        when(searchResultRepository.findByHotelId("1234aBc")).thenReturn(Optional.empty());
        when(searchResultRepository.save(any(SearchResult.class))).thenReturn(null);
        doNothing().when(kafkaTemplate).send(anyString(), any(SearchResult.class));

        kafkaConsumer.consume(searchRequest);

        verify(searchResultRepository, times(1)).findByHotelId("1234aBc");
        verify(searchResultRepository, times(1)).save(any(SearchResult.class));
        verify(kafkaTemplate, times(1)).send(anyString(), any(SearchResult.class));
    }
}
