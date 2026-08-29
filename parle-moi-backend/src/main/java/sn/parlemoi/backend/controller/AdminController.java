package sn.parlemoi.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.parlemoi.backend.dto.admin.ConversationAdminResponse;
import sn.parlemoi.backend.dto.admin.StatsResponse;
import sn.parlemoi.backend.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return adminService.consulterStats();
    }

    @GetMapping("/conversations")
    public List<ConversationAdminResponse> conversations() {
        return adminService.listerToutesConversations();
    }
}