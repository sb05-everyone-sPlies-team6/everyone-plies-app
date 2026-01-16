package team6.finalproject.domain.playlist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team6.finalproject.domain.playlist.dto.CursorResponsePlaylistDto;
import team6.finalproject.domain.playlist.dto.PlaylistCreateRequest;
import team6.finalproject.domain.playlist.dto.PlaylistDto;
import team6.finalproject.domain.playlist.dto.PlaylistUpdateRequest;
import team6.finalproject.domain.playlist.service.PlaylistService;
import team6.finalproject.global.security.MoplUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping
    public ResponseEntity<CursorResponsePlaylistDto<PlaylistDto>> getPlaylists(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESCENDING") String sortDirection,
            @RequestParam(required = false) String cursor,
            @AuthenticationPrincipal MoplUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserDto().id() : null;

        return ResponseEntity.ok(
                playlistService.getPlaylists(userId, limit, sortBy, sortDirection, cursor)
        );
    }

    @PostMapping
    public ResponseEntity<PlaylistDto> createPlaylist(
            @RequestBody PlaylistCreateRequest request,
            @AuthenticationPrincipal MoplUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                playlistService.createPlaylist(userDetails.getUserDto().id(), request)
        );
    }

    @PostMapping("/{playlistId}/contents/{contentId}")
    public ResponseEntity<Void> addContentToPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long contentId
    ) {
        playlistService.addContentToPlaylist(playlistId, contentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal MoplUserDetails userDetails
    ) {
        playlistService.deletePlaylist(
                playlistId,
                userDetails.getUserDto().id()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{playlistId}/contents/{contentId}")
    public ResponseEntity<Void> removeContent(
            @PathVariable Long playlistId,
            @PathVariable Long contentId,
            @AuthenticationPrincipal MoplUserDetails userDetails
    ) {
        playlistService.removeContentFromPlaylist(
                playlistId,
                contentId,
                userDetails.getUserDto().id()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDto> getPlaylist(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal MoplUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserDto().id() : null;
        PlaylistDto playlistDto = playlistService.getPlaylistById(playlistId, userId);
        return ResponseEntity.ok(playlistDto);
    }


    @PostMapping("/{playlistId}/subscription")
    public ResponseEntity<Void> subscribe(
            @PathVariable String playlistId,
            @AuthenticationPrincipal MoplUserDetails userDetails
    ) {
        Long playlistIdLong = Long.valueOf(playlistId);
        Long userId = Long.valueOf(userDetails.getUserDto().id());
        playlistService.subscribePlaylist(playlistIdLong, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}/subscription")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable String playlistId,
            @AuthenticationPrincipal MoplUserDetails userDetails
    ) {
        Long playlistIdLong = Long.valueOf(playlistId);
        Long userId = Long.valueOf(userDetails.getUserDto().id());
        playlistService.unsubscribePlaylist(playlistIdLong, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{playlistId}")
    public ResponseEntity<Void> updatePlaylist(
            @PathVariable String playlistId,
            @RequestBody PlaylistUpdateRequest request,
            @AuthenticationPrincipal MoplUserDetails userDetails
    ) {
        playlistService.updatePlaylist(
                Long.valueOf(playlistId),
                userDetails.getUserDto().id(),
                request
        );
        return ResponseEntity.noContent().build();
    }
}