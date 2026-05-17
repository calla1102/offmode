package com.offmode.global.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.offmode.global.exception.BusinessException;
import com.offmode.global.status.ErrorStatus;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class ImageUploadServiceTest {

  @TempDir Path tempDir;

  @Test
  void uploadVerificationImageStoresValidPngLocally() throws Exception {
    ImageUploadService service = createService();
    MockMultipartFile file = new MockMultipartFile("photo", "mission.png", "image/png", pngBytes());

    String url = service.uploadVerificationImage(file);

    assertThat(url).startsWith("/uploads/").endsWith(".png");
    assertThat(Files.list(tempDir)).hasSize(1);
  }

  @Test
  void uploadVerificationImageRejectsUnsupportedExtension() {
    ImageUploadService service = createService();
    MockMultipartFile file =
        new MockMultipartFile("photo", "mission.txt", "text/plain", "not-image".getBytes());

    assertThatThrownBy(() -> service.uploadVerificationImage(file))
        .isInstanceOfSatisfying(
            BusinessException.class,
            e -> assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.FILE_INVALID_TYPE));
  }

  @Test
  void uploadVerificationImageRejectsNonImageBytes() {
    ImageUploadService service = createService();
    MockMultipartFile file =
        new MockMultipartFile("photo", "mission.png", "image/png", "not-image".getBytes());

    assertThatThrownBy(() -> service.uploadVerificationImage(file))
        .isInstanceOfSatisfying(
            BusinessException.class,
            e -> assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.FILE_INVALID_IMAGE));
  }

  private ImageUploadService createService() {
    ImageUploadService service = new ImageUploadService(Optional.empty());
    ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());
    ReflectionTestUtils.setField(service, "r2Bucket", "");
    ReflectionTestUtils.setField(service, "r2PublicUrl", "");
    return service;
  }

  private byte[] pngBytes() throws Exception {
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }
}
