package team6.finalproject.domain.common;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


@RequiredArgsConstructor
@Component
public class S3Service {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public String upload(MultipartFile file, String folderName) throws IOException {
    String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
    String key = folderName + "/" + fileName;

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getSize());
    metadata.setContentType(file.getContentType());

    amazonS3.putObject(bucket, key, file.getInputStream(), metadata);

    return amazonS3.getUrl(bucket, key).toString();
  }
}