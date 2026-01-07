package team6.finalproject.domain.content.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import team6.finalproject.domain.content.entity.content.Content;
import team6.finalproject.domain.content.entity.tag.ContentTag;
import team6.finalproject.domain.content.entity.tag.Tag;
import team6.finalproject.domain.content.repository.ContentRepository;
import team6.finalproject.domain.content.repository.ContentTagRepository;
import team6.finalproject.domain.content.repository.TagRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContentItemWriter implements ItemWriter<ContentBatchDto> {

	private final ContentRepository contentRepository;
	private final TagRepository tagRepository;
	private final ContentTagRepository contentTagRepository;

	@Override
	@Transactional
	public void write(Chunk<? extends ContentBatchDto> dtos) {
		for (ContentBatchDto dto : dtos) {
			// 1. Contents 테이블 Upsert
			Content content = dto.getContent();
			Content savedContent = contentRepository.findByExternalIdAndSourceType(
					content.getExternalId(), content.getSourceType())
				.map(existing -> {
					existing.update(content.getTitle(), content.getType(),
						content.getDescription(), content.getThumbnailUrl());
					return existing;
				})
				.orElseGet(() -> contentRepository.save(content));

			// 2. Tags 및 Contents_Tags 처리
			List<String> genreNames = TmdbGenreMapper.toGenreNames(dto.getGenreIds());
			for (String name : genreNames) {
				// tags 테이블에 이름이 없으면 저장, 있으면 가져오기
				Tag tag = tagRepository.findByName(name)
					.orElseGet(() -> tagRepository.save(new Tag(name)));

				// contents_tags 테이블에 매핑 정보가 없으면 저장
				if (!contentTagRepository.existsByContentAndTag(savedContent, tag)) {
					contentTagRepository.save(new ContentTag(savedContent, tag));
				}
			}
		}
	}
}