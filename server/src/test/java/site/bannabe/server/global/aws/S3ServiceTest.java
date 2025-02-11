package site.bannabe.server.global.aws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.awspring.cloud.s3.S3Operations;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

  @InjectMocks
  private S3Service s3Service;
  @Mock
  private S3Operations s3Operations;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(s3Service, "bucketName", "bucket-name");
  }

  @Test
  @DisplayName("PreSignedUrl 정상 생성")
  void getPreSignedUrl() throws MalformedURLException {
    //given
    String objectKey = "objectKey";
    String formattedObjectKey = "profile-images/" + objectKey;
    URL url = new URL("http://localhost:8080/" + formattedObjectKey);
    given(s3Operations.createSignedPutURL(anyString(), eq(formattedObjectKey), any(Duration.class))).willReturn(url);

    //when
    String preSignedUrl = s3Service.getPreSignedUrl(objectKey);

    //then
    assertThat(preSignedUrl).isEqualTo(url.toString());
  }

  @Test
  @DisplayName("removeProfileImage 정상 작동")
  void removeProfileImage() {
    //given
    String imageUrl = "https://bucket-name.s3.ap-northeast-2.amazonaws.com/profile-images/objectKey";
    String objectKey = "profile-images/objectKey";
    String bucketName = "bucket-name";
    //when
    s3Service.removeProfileImage(imageUrl);

    //then
    verify(s3Operations).deleteObject(bucketName, objectKey);
  }

  @Test
  @DisplayName("URL 파싱 실패 또는 예상 형식이 아닐 때 예외 발생")
  void urlParsingError() {
    //given
    String imageUrl = "isNotUrl";

    //when then
    assertThatExceptionOfType(BannabeServiceException.class)
        .isThrownBy(() -> s3Service.removeProfileImage(imageUrl))
        .withMessage(ErrorCode.INVALID_S3_URL_FORMAT.getMessage());

    verify(s3Operations, never()).deleteObject(anyString(), anyString());
  }

}