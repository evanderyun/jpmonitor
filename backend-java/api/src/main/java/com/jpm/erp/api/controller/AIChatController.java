package com.jpm.erp.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/chat")
@PreAuthorize("isAuthenticated()")
public class AIChatController {

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        String reply = generateResponse(message);
        Map<String, String> response = new HashMap<>();
        response.put("reply", reply);
        return ResponseEntity.ok(response);
    }

    private String generateResponse(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("fleet") || lower.contains("unit") || lower.contains("alat")) {
            return "Data Fleet Management tersedia di modul Fleet Management. Anda bisa melihat status seluruh unit, jam operasional, dan jadwal maintenance.";
        }
        if (lower.contains("inventory") || lower.contains("sparepart") || lower.contains("stok")) {
            return "Inventory & Spareparts tersedia di modul Inventory. Anda bisa melihat stok, melakukan transaksi, dan memantau item low stock.";
        }
        if (lower.contains("production") || lower.contains("produksi") || lower.contains("coal") || lower.contains("batubara")) {
            return "Production Control menampilkan data produksi coal, overburden, dan strip ratio di Dashboard.";
        }
        if (lower.contains("employee") || lower.contains("karyawan") || lower.contains("hr")) {
            return "Modul Employee & HR mengelola data karyawan, timesheet, dan hour meter.";
        }
        if (lower.contains("supplier") || lower.contains("vendor")) {
            return "Modul Suppliers & Vendors mengelola data supplier dan riwayat transaksi.";
        }
        if (lower.contains("finance") || lower.contains("keuangan") || lower.contains("debt") || lower.contains("hutang")) {
            return "Modul Finance & Debt mengelola data keuangan dan hutang perusahaan.";
        }
        if (lower.contains("hse") || lower.contains("safety") || lower.contains("keselamatan")) {
            return "Modul HSE & Safety mencatat incident dan investigation.";
        }
        if (lower.contains("help") || lower.contains("bantuan") || lower.contains("bisa apa")) {
            return "Saya bisa membantu navigasi modul ERP. Tanyakan tentang Fleet, Inventory, Production, Employee, Supplier, Finance, atau HSE.";
        }
        return "Terima kasih. Saya sedang dalam mode pengembangan. Coba tanyakan tentang Fleet, Inventory, Production, atau modul lainnya.";
    }
}
