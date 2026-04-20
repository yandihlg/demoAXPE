package com.axpe.datamodel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "search_results")
@Access(AccessType.FIELD)
public final class SearchResult {
	@Id
	@NotBlank(message = "Search ID is mandatory")
	private final String searchId;

	@NotBlank(message = "Hotel ID is mandatory")
	private final String hotelId;

	@NotBlank(message = "Check-in date is mandatory")
	private final String checkIn;

	@NotBlank(message = "Check-out date is mandatory")
	private final String checkOut;

	@ElementCollection(fetch = FetchType.EAGER)
	@NotNull(message = "Ages list cannot be null")
	@Size(min = 1, message = "Ages list must have at least one age")
	private final List<@NotNull Integer> ages;

	private final int count;

	public SearchResult(@NotBlank(message = "Search ID is mandatory") String searchId,
			@NotBlank(message = "Hotel ID is mandatory") String hotelId,
			@NotBlank(message = "Check-in date is mandatory") String checkIn,
			@NotBlank(message = "Check-out date is mandatory") String checkOut,
			@NotNull(message = "Ages list cannot be null") @Size(min = 1, message = "Ages list must have at least one age") List<@NotNull Integer> ages,
			int count) {
		this.searchId = searchId;
		this.hotelId = hotelId;
		this.checkIn = checkIn;
		this.checkOut = checkOut;
		this.ages = Collections.unmodifiableList(ages);
		this.count = count;
	}

	protected SearchResult() {
		this.searchId = null;
		this.hotelId = null;
		this.checkIn = null;
		this.checkOut = null;
		this.ages = null;
		this.count = 0;
	}

	public String getSearchId() {
		return searchId;
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

	public int getCount() {
		return count;
	}

	public SearchResult addCount() {
		return new SearchResult(this.searchId, this.hotelId, this.checkIn, this.checkOut, this.ages, this.count + 1);
	}

	@Override
    public String toString() {
        return "SearchResult{" +
                "searchId='" + searchId + '\'' +
                ", hotelId='" + hotelId + '\'' +
                ", checkIn='" + checkIn + '\'' +
                ", checkOut='" + checkOut + '\'' +
                ", ages=" + ages +
                ", count=" + count +
                '}';
    }

	 @Override
	    public boolean equals(Object o) {
	        if (this == o) 
	        	return true;
	        if (o == null || getClass() != o.getClass()) 
	        	return false;
	        SearchResult that = (SearchResult) o;
	        return count == that.count &&
	                Objects.equals(searchId, that.searchId) &&
	                Objects.equals(hotelId, that.hotelId) &&
	                Objects.equals(checkIn, that.checkIn) &&
	                Objects.equals(checkOut, that.checkOut) &&
	                Objects.equals(ages, that.ages);
	    }

	    @Override
	    public int hashCode() {
	        return Objects.hash(searchId, hotelId, checkIn, checkOut, ages, count);
	    }

}
