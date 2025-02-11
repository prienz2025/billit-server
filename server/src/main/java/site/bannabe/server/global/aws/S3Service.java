package site.bannabe.server.global.aws;

import io.awspring.cloud.s3.S3Operations;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@Service
@RequiredArgsConstructor
public class S3Service {

  private static final String PROFILE_IMAGE_DIRECTORY = "profile-images/";

  private final S3Operations s3Operations;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucketName;

  public String getPreSignedUrl(String objectKey) {
    objectKey = PROFILE_IMAGE_DIRECTORY + objectKey;
    return s3Operations.createSignedPutURL(bucketName, objectKey, Duration.ofMinutes(5L)).toString();
  }

  public void removeProfileImage(String imageUrl) {
    String objectKey = extractObjectKey(imageUrl);
    s3Operations.deleteObject(bucketName, objectKey);
  }

  private String extractObjectKey(String imageUrl) {
    try {
      URI uri = new URI(imageUrl);
      String path = uri.getPath();
      if (Objects.isNull(path) || !path.contains(PROFILE_IMAGE_DIRECTORY)) {
        throw new BannabeServiceException(ErrorCode.INVALID_S3_URL_FORMAT);
      }
      return path.startsWith("/") ? path.substring(1) : path;
    } catch (URISyntaxException e) {
      throw new BannabeServiceException(ErrorCode.INVALID_S3_URL_FORMAT);
    }
  }

}