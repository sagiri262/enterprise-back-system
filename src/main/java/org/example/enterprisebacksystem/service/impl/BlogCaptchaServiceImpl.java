package org.example.enterprisebacksystem.service.impl;

import org.example.enterprisebacksystem.dto.blog.BlogCaptchaResp;
import org.example.enterprisebacksystem.service.BlogCaptchaService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlogCaptchaServiceImpl implements BlogCaptchaService {
    private static final long EXPIRE_SECONDS = 300;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, CaptchaItem> store = new ConcurrentHashMap<>();

    @Override
    public BlogCaptchaResp create() {
        cleanup();
        int left = random.nextInt(8) + 2;
        int right = random.nextInt(8) + 2;
        String challenge = left + " + " + right + " = ?";
        String id = UUID.randomUUID().toString();
        store.put(id, new CaptchaItem(String.valueOf(left + right), Instant.now().plusSeconds(EXPIRE_SECONDS)));
        return new BlogCaptchaResp(id, "请输入图片中的计算结果", renderImage(challenge));
    }

    @Override
    public boolean verify(String id, String answer) {
        cleanup();
        CaptchaItem item = store.remove(id);
        if (item == null || item.expireAt().isBefore(Instant.now())) {
            return false;
        }
        return item.answer().equals(answer == null ? "" : answer.trim());
    }

    private void cleanup() {
        Instant now = Instant.now();
        store.entrySet().removeIf(entry -> entry.getValue().expireAt().isBefore(now));
    }

    private String renderImage(String question) {
        int width = 168;
        int height = 56;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(245, 249, 255));
            graphics.fillRect(0, 0, width, height);

            for (int i = 0; i < 6; i += 1) {
                graphics.setColor(new Color(88 + random.nextInt(80), 122 + random.nextInt(70), 190 + random.nextInt(45), 110));
                graphics.setStroke(new BasicStroke(1.2f + random.nextFloat()));
                int x1 = random.nextInt(width);
                int y1 = random.nextInt(height);
                int x2 = random.nextInt(width);
                int y2 = random.nextInt(height);
                graphics.drawLine(x1, y1, x2, y2);
            }

            for (int i = 0; i < 90; i += 1) {
                graphics.setColor(new Color(80 + random.nextInt(90), 105 + random.nextInt(90), 170 + random.nextInt(60), 80));
                graphics.fillRect(random.nextInt(width), random.nextInt(height), 1, 1);
            }

            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
            FontMetrics metrics = graphics.getFontMetrics();
            int textWidth = metrics.stringWidth(question);
            int textX = Math.max(14, (width - textWidth) / 2);
            int textY = (height - metrics.getHeight()) / 2 + metrics.getAscent() + 1;
            for (int i = 0; i < question.length(); i += 1) {
                String value = String.valueOf(question.charAt(i));
                graphics.setColor(new Color(39 + random.nextInt(45), 73 + random.nextInt(45), 145 + random.nextInt(55)));
                int rotation = random.nextInt(9) - 4;
                graphics.rotate(Math.toRadians(rotation), textX, textY);
                graphics.drawString(value, textX, textY + random.nextInt(5) - 2);
                graphics.rotate(Math.toRadians(-rotation), textX, textY);
                textX += metrics.charWidth(question.charAt(i)) + 2;
            }
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("验证码图片生成失败", ex);
        }
    }

    private record CaptchaItem(String answer, Instant expireAt) {
    }
}
