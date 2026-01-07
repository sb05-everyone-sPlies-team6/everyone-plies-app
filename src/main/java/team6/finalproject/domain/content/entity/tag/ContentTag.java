package team6.finalproject.domain.content.entity.tag;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team6.finalproject.domain.content.entity.content.Content;

@Entity
@Table(name = "contents_tags") @Getter
@NoArgsConstructor
public class ContentTag {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long mappingId;
	@ManyToOne
	@JoinColumn(name = "content_id")
	private Content content;
	@ManyToOne @JoinColumn(name = "tag_id")
	private Tag tag;
	public ContentTag(Content content, Tag tag) { this.content = content; this.tag = tag; }
}