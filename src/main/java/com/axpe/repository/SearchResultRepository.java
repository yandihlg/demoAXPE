package com.axpe.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.axpe.datamodel.SearchResult;

@Repository
public interface SearchResultRepository extends JpaRepository<SearchResult, String> {

	// TODO entiendo que la busqueda solo se hace por el id del hotel sino habria
	// que renombrar el metodo a findByHotelIdAndCheckInAndCheckOut y pasarle clos
	// parametros de busqueda necesarios
	Optional<SearchResult> findByHotelId(String hotelId);

}
