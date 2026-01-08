package team6.finalproject.domain.content.batch;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import team6.finalproject.domain.content.entity.content.Content;

@Getter
@AllArgsConstructor
public class ContentBatchDto {
	private Content content;
	private List<String> tagNames;
}