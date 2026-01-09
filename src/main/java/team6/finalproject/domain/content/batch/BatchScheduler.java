package team6.finalproject.domain.content.batch;

import java.time.LocalDateTime;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchScheduler {

	private final JobLauncher jobLauncher;
	private final Job contentImportJob;

	//@Scheduled(cron = "0 0 7 * * *")
	//@Scheduled(initialDelay = 5000, fixedDelay = 3600000) //테스트용 5초뒤
	public void runContentImportJob() {
		try {
			runSingleTypeJob("MOVIE");
			runSingleTypeJob("DRAMA");
			runSingleTypeJob("SPORTS");

			log.info("전체 자동 배치 작업 완료");
		} catch (Exception e) {
			log.error("자동 배치 작업 도중 오류 발생: {}", e.getMessage());
		}
	}

	private void runSingleTypeJob(String type) throws Exception {
		JobParameters params = new JobParametersBuilder()
			.addString("datetime", LocalDateTime.now().toString() + "-" + type) // 중복 실행 방지
			.addString("contentType", type) // Processor와 Reader가 사용하는 핵심 파라미터
			.toJobParameters();

		jobLauncher.run(contentImportJob, params);
	}
}
