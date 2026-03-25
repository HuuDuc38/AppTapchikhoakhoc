package com.example.apptapchikhoakhoc.utils;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ContentModerationHelper — Kiểm duyệt nội dung bài viết bằng Claude AI.
 *
 * Kiểm tra:
 *  • Bạo lực / thù ghét / kích động
 *  • Vi phạm đạo đức / thuần phong mỹ tục
 *  • Đạo văn / vi phạm bản quyền rõ ràng
 *  • Tin giả nghiêm trọng / vi phạm pháp luật Việt Nam
 */
public class ContentModerationHelper {

    // ── Thay bằng API key thực của bạn ──────────────────────────
    private static final String API_KEY        = "YOUR_ANTHROPIC_API_KEY";
    private static final String API_URL        = "https://api.anthropic.com/v1/messages";
    private static final String MODEL          = "claude-haiku-4-5-20251001";
    private static final int    CONNECT_TIMEOUT = 10_000;
    private static final int    READ_TIMEOUT    = 20_000;
    private static final int    MAX_CONTENT_LEN = 2000;

    // ════════════════════════════════════════════════════════════
    //  PUBLIC INTERFACE
    // ════════════════════════════════════════════════════════════

    public interface ModerationCallback {
        /** Nội dung sạch — cho phép đăng */
        void onApproved();
        /** Nội dung vi phạm — kèm lý do cụ thể để hiển thị cho user */
        void onRejected(String reason);
        /** Lỗi mạng/API — mặc định cho phép đăng để không chặn user */
        void onError(String errorMessage);
    }

    /**
     * Kiểm duyệt bài viết bất đồng bộ. Callback luôn trả về trên main thread.
     */
    public static void moderate(String title, String content, ModerationCallback callback) {
        String plain = stripHtml(content);
        if (plain.length() > MAX_CONTENT_LEN) {
            plain = plain.substring(0, MAX_CONTENT_LEN) + "...";
        }
        final String finalContent = plain;

        new Thread(() -> {
            try {
                String response = callApi(title, finalContent);
                handleResponse(response, callback);
            } catch (Exception e) {
                postMain(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    // ════════════════════════════════════════════════════════════
    //  API CALL
    // ════════════════════════════════════════════════════════════

    private static String callApi(String title, String content) throws Exception {
        String prompt = buildPrompt(title, content);
        String body   = "{"
                + "\"model\":\"" + MODEL + "\","
                + "\"max_tokens\":400,"
                + "\"messages\":[{\"role\":\"user\",\"content\":" + jsonString(prompt) + "}]"
                + "}";

        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type",      "application/json");
        conn.setRequestProperty("x-api-key",         API_KEY);
        conn.setRequestProperty("anthropic-version", "2023-06-01");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();
        java.io.InputStream is = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
        BufferedReader reader  = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb       = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    private static String buildPrompt(String title, String content) {
        return "Bạn là hệ thống kiểm duyệt nội dung tạp chí khoa học Việt Nam.\n"
                + "Phân tích bài viết và trả lời ĐÚNG format JSON sau, KHÔNG thêm gì khác.\n\n"
                + "TIÊU ĐỀ: " + title + "\n"
                + "NỘI DUNG: " + content + "\n\n"
                + "Kiểm tra các vi phạm:\n"
                + "1. Bạo lực, kích động thù địch, phân biệt đối xử\n"
                + "2. Nội dung khiêu dâm, vi phạm thuần phong mỹ tục\n"
                + "3. Đạo văn rõ ràng (sao chép nguyên văn không ghi nguồn)\n"
                + "4. Tin giả nghiêm trọng gây hoang mang xã hội\n"
                + "5. Vi phạm pháp luật Việt Nam\n"
                + "6. Quảng cáo trá hình, nội dung thương mại ẩn\n\n"
                + "Trả lời ĐÚNG JSON:\n"
                + "{\"approved\":true/false,\"violation_type\":\"loại vi phạm hoặc null\","
                + "\"reason\":\"lý do chi tiết bằng tiếng Việt hoặc chuỗi rỗng nếu ok\","
                + "\"severity\":\"low/medium/high hoặc null\"}";
    }

    // ════════════════════════════════════════════════════════════
    //  PARSE RESPONSE
    // ════════════════════════════════════════════════════════════

    private static void handleResponse(String raw, ModerationCallback callback) {
        try {
            // Lấy phần text từ Anthropic response JSON
            String text = extractTextContent(raw);
            if (text == null || text.isEmpty()) {
                postMain(() -> callback.onError("Phản hồi API rỗng"));
                return;
            }

            // Parse approved
            boolean approved = extractBool(text, "approved", true);

            if (approved) {
                postMain(callback::onApproved);
            } else {
                String reason        = extractString(text, "reason");
                String violationType = extractString(text, "violation_type");
                String severity      = extractString(text, "severity");

                String displayMsg = buildRejectionMessage(violationType, reason, severity);
                postMain(() -> callback.onRejected(displayMsg));
            }
        } catch (Exception e) {
            // Parse thất bại → mặc định approve
            postMain(() -> callback.onError("Lỗi phân tích: " + e.getMessage()));
        }
    }

    private static String buildRejectionMessage(String type, String reason, String severity) {
        StringBuilder sb = new StringBuilder();
        if (type != null && !type.isEmpty() && !type.equals("null")) {
            sb.append("⚠️ Vi phạm: ").append(type).append("\n\n");
        }
        if (reason != null && !reason.isEmpty()) {
            sb.append(reason);
        } else {
            sb.append("Nội dung không phù hợp với tiêu chuẩn cộng đồng.");
        }
        if ("high".equals(severity)) {
            sb.append("\n\n🚫 Mức độ: Nghiêm trọng");
        } else if ("medium".equals(severity)) {
            sb.append("\n\n⚠️ Mức độ: Trung bình");
        }
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════════
    //  JSON HELPERS (không dùng thư viện để giảm dependency)
    // ════════════════════════════════════════════════════════════

    /** Lấy text content từ Anthropic response */
    private static String extractTextContent(String response) {
        int start = response.indexOf("\"text\":\"");
        if (start == -1) return null;
        start += 8;
        StringBuilder sb  = new StringBuilder();
        boolean escape     = false;
        for (int i = start; i < response.length(); i++) {
            char c = response.charAt(i);
            if (escape) {
                switch (c) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    default: sb.append(c);
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean extractBool(String json, String key, boolean defaultVal) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) return defaultVal;
        String sub = json.substring(idx + search.length()).trim();
        if (sub.startsWith("true"))  return true;
        if (sub.startsWith("false")) return false;
        return defaultVal;
    }

    private static String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        String val = json.substring(start, end);
        return val.equals("null") ? null : val;
    }

    private static String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").trim();
    }

    private static String jsonString(String s) {
        if (s == null) return "\"\"";
        return "\"" + s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    private static void postMain(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }
}