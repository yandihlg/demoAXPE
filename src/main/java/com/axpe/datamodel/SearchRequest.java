package com.axpe.datamodel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SearchRequest {

	@NotBlank(message = "Hotel ID is mandatory")
	private final String hotelId;

	@NotNull(message = "Check-in date is mandatory")
	private final String checkIn;

	@NotNull(message = "Check-out date is mandatory")
	private final String checkOut;

	@NotNull(message = "Ages list cannot be null")
	@Size(min = 1, message = "Ages list must have at least one age")
	private final List<@NotNull Integer> ages;

	public SearchRequest(@JsonProperty("hotelId") @NotBlank(message = "Hotel ID is mandatory") String hotelId,
			@JsonProperty("checkIn") @NotNull(message = "Check-in date is mandatory") String checkIn,
			@JsonProperty("checkOut") @NotNull(message = "Check-out date is mandatory") String checkOut,
			@JsonProperty("ages") @NotNull(message = "Ages list cannot be null") @Size(min = 1, message = "Ages list must have at least one age") List<@NotNull Integer> ages) {
		this.hotelId = hotelId;
		this.checkIn = checkIn;
		this.checkOut = checkOut;
		this.ages = Collections.unmodifiableList(ages);
	}

	public String getHotelId() {
		return hotelId;
	}

	public String getCheckIn() {
		return checkIn;
	}

	public String getCheckOut() {
		return checkOut;
	}

	public List<Integer> getAges() {
		return ages;
	}

	@Override
	public String toString() {
		return "SearchRequest{" + "hotelId='" + hotelId + '\'' + ", checkIn=" + checkIn + ", checkOut=" + checkOut
				+ ", ages=" + ages + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SearchRequest that = (SearchRequest) o;
		return Objects.equals(hotelId, that.hotelId) && Objects.equals(checkIn, that.checkIn)
				&& Objects.equals(checkOut, that.checkOut) && Objects.equals(ages, that.ages);
	}

	@Override
	public int hashCode() {
		return Objects.hash(hotelId, checkIn, checkOut, ages);
	}
}
