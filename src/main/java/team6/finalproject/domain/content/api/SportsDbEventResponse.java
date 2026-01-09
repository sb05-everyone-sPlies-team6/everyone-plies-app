package team6.finalproject.domain.content.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SportsDbEventResponse {
	@JsonProperty("results")
	private List<EventDto> events;

	@Getter
	@NoArgsConstructor
	public static class EventDto {
		@JsonProperty("idEvent")
		private String idEvent;
		@JsonProperty("strEvent")
		private String strEvent;     //타이틀
		@JsonProperty("strFilename")
		private String strFilename;  //디스크립션
		@JsonProperty("strThumb")
		private String strThumb;     //썸네일
		@JsonProperty("strSport")
		private String strSport;     //종목
		@JsonProperty("strVenue")
		private String strVenue;     //경기장
		@JsonProperty("strLeague")
		private String strLeague;    //리그명
	}
}
