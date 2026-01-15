package team6.finalproject.domain.playlist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import team6.finalproject.domain.playlist.dto.CursorResponsePlaylistDto;
import team6.finalproject.domain.playlist.dto.PlaylistCreateRequest;
import team6.finalproject.domain.playlist.dto.PlaylistDto;
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

    @DeleteMapping("/{playlistId}/contents/{contentId}")
    public ResponseEntity<Void> removeContentFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long contentId,
            @AuthenticationPrincipal MoplUserDetails userDetails
    ) {
        Long userId = userDetails.getUserDto().id();

        PlaylistDto playlist = playlistService.getPlaylistById(playlistId, userId);
        if (!playlist.owner().userId().equals(String.valueOf(userId))) {
            return ResponseEntity.status(403).build();
        }

        playlistService.removeContentFromPlaylist(playlistId, contentId);
        return ResponseEntity.ok().build();
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
}