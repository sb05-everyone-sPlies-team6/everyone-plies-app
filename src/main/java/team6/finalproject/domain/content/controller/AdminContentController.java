package team6.finalproject.domain.content.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import team6.finalproject.domain.content.dto.ContentCreateRequest;
import team6.finalproject.domain.content.dto.ContentUpdateRequest;
import team6.finalproject.domain.content.service.AdminContentService;

@RestController
@RequestMapping("/api/admin/contents")
@RequiredArgsConstructor
public class AdminContentController {

	private final AdminContentService adminContentService;

	@PostMapping
	public ResponseEntity<Long> create(@RequestBody @Valid ContentCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.createContent(request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody @Valid ContentUpdateRequest request) {
		adminContentService.updateContent(id, request);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		adminContentService.deleteContent(id);
		return ResponseEntity.noContent().build();
	}
}