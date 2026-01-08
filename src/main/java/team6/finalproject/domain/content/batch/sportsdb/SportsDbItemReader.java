package team6.finalproject.domain.content.batch.sportsdb;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import team6.finalproject.domain.content.api.SportsDbFeignClient;
import team6.finalproject.domain.content.api.SportsDbEventResponse;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class SportsDbItemReader implements ItemReader<SportsDbEventResponse.EventDto> {

	private final SportsDbFeignClient sportsDbFeignClient;
	@Value("${sportsdb.api.key}")
	private String apiKey;

	private List<SportsDbEventResponse.EventDto> resultsBuffer = new ArrayList<>();

	// 리그 대신 주요 인기 팀 ID (리버풀, 아스널, 맨시티 등)를 순회하여 최신 데이터를 가져옵니다.
	private final List<String> teamIds = List.of("133602", "133604", "133613");
	private int currentIndex = 0;

	@Override
	public SportsDbEventResponse.EventDto read() {
		if (resultsBuffer.isEmpty()) {
			if (currentIndex >= teamIds.size()) return null;
			fetchLastEvents(teamIds.get(currentIndex++));
		}
		return resultsBuffer.isEmpty() ? null : resultsBuffer.remove(0);
	}

	private void fetchLastEvents(String teamId) {
		try {
			SportsDbEventResponse response = sportsDbFeignClient.getLastEvents(apiKey, teamId);
			if (response != null && response.getEvents() != null) {
				this.resultsBuffer.addAll(response.getEvents());
			}
		} catch (Exception e) {
			log.error("팀 ID {} 데이터 호출 실패: {}", teamId, e.getMessage());
		}
	}
}