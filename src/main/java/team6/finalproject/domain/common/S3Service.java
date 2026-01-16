package team6.finalproject.domain.common;

import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;


@RequiredArgsConstructor
@Component
public class S3Service {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public String upload(MultipartFile file, String folderName) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }

    String original = (file.getOriginalFilename() == null) ? "image" : file.getOriginalFilename();
    String imageName = UUID.randomUUID().toString() + "_" + original;

    String key = folderName + "/" + imageName;

    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .contentType(file.getContentType())
          .contentLength(file.getSize())
          .build();

      PutObjectResponse response = s3Client.putObject(
          putObjectRequest,
          RequestBody.fromInputStream(file.getInputStream(), file.getSize())
      );

      if (!response.sdkHttpResponse().isSuccessful()) {
        throw new RuntimeException("Upload failed");
      }

      return s3Client.utilities()
          .getUrl(b -> b.bucket(bucket).key(key))
          .toExternalForm();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}