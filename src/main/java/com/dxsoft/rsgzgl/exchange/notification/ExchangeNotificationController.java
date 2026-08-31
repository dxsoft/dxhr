package com.dxsoft.rsgzgl.exchange.notification;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange-notifications")
class ExchangeNotificationController {

    private final ExchangeNotificationService exchangeNotificationService;

    ExchangeNotificationController(ExchangeNotificationService exchangeNotificationService) {
        this.exchangeNotificationService = exchangeNotificationService;
    }

    @GetMapping
    PageResponse<ExchangeNotificationRecord> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return exchangeNotificationService.list(status, PageRequest.of(page, size));
    }

    @GetMapping("/unread-count")
    UnreadCountResponse unreadCount() {
        return new UnreadCountResponse(exchangeNotificationService.unreadCount());
    }

    @PostMapping("/{id}/read")
    MarkReadResponse markRead(@PathVariable long id) {
        return new MarkReadResponse(exchangeNotificationService.markRead(id));
    }

    @PostMapping("/read-all")
    MarkAllReadResponse markAllRead() {
        return new MarkAllReadResponse(exchangeNotificationService.markAllRead());
    }

    record UnreadCountResponse(long count) {
    }

    record MarkReadResponse(boolean updated) {
    }

    record MarkAllReadResponse(int updated) {
    }
}
