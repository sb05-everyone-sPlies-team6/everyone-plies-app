package team6.finalproject.domain.content.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.repository.ContentRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ContentItemWriter implements ItemWriter<Content> {

	private final ContentRepository contentRepository;

	@Override
	public void write(Chunk<? extends Content> contents) {
		for (Content newContent : contents) {
			// 중복 체크: external_id와 source_type으로 기존 데이터 조회
			Optional<Content> existingContent = contentRepository.findByExternalIdAndSourceType(
				newContent.getExternalId(), newContent.getSourceType());

			if (existingContent.isPresent()) {
				// 이미 존재하면 정보 업데이트 (Dirty Checking 활용)
				Content content = existingContent.get();
				content.update(
					newContent.getTitle(),
					newContent.getType(),
					newContent.getDescription(),
					newContent.getThumbnailUrl()
				);
			} else {
				// 존재하지 않으면 새롭게 저장
				contentRepository.save(newContent);
			}
		}
	}
}