package team6.finalproject.domain.content.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "sportsDbClient", url = "${sportsdb.api.url}")
public interface SportsDbFeignClient {
	// 예정된 경기 목록 조회
	@GetMapping("/{key}/eventslast.php")
	SportsDbEventResponse getLastEvents(
		@PathVariable("key") String key,
		@RequestParam("id") String id // 팀 ID 혹은 리그 ID
	);
}
