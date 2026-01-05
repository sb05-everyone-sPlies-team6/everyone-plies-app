package team6.finalproject.domain.content.entity.content;

public enum ContentType {
	MOVIE("영화"),
	DRAMA("드라마"),
	SPORTS("스포츠");

	private final String description;
	ContentType(String description) { this.description = description; }
}