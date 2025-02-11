package site.bannabe.server.global.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // 400 BAD_REQUEST
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
  UNSUPPORTED_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 Content-Type입니다."),
  MISSING_REQUIRED_FIELDS(HttpStatus.BAD_REQUEST, "필수 요청 데이터가 누락되었습니다."),
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 데이터가 올바르지 않습니다."),
  INVALID_LOGIN_CREDENTIALS(HttpStatus.BAD_REQUEST, "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요."),
  INVALID_S3_URL_FORMAT(HttpStatus.BAD_REQUEST, "S3 URL 형식이 올바르지 않습니다."),
  RENTAL_ITEM_NOT_RENTED(HttpStatus.BAD_REQUEST, "대여되지 않은 물품입니다."),

  // 401 UNAUTHORIZED
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "사용자 인증이 필요합니다."),
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "RefreshToken이 유효하지 않습니다."),
  TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),

  // 403 FORBIDDEN
  FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
  RENTAL_ITEM_OVERDUE(HttpStatus.FORBIDDEN, "연체된 물품은 반납할 수 없습니다. 결제를 진행해주세요."),

  // 404 NOT_FOUND
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원정보가 존재하지 않습니다."),
  REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "RefreshToken이 존재하지 않습니다."),
  AUTH_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "인증 코드가 존재하지 않습니다."),
  ORDER_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 정보가 존재하지 않습니다."),
  BOOKMARK_NOT_EXIST(HttpStatus.NOT_FOUND, "북마크 정보가 일치하지 않습니다."),
  RENTAL_STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "대여소 정보가 존재하지 않습니다."),
  RENTAL_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "대여물품 정보가 존재하지 않습니다."),
  RENTAL_STATION_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "대여소 대여물품 정보가 존재하지 않습니다."),
  RENTAL_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "대여내역이 존재하지 않습니다."),
  USER_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 토큰이 존재하지 않습니다."),

  // 409 CONFLICT
  NEW_PASSWORD_MISMATCH(HttpStatus.CONFLICT, "새 비밀번호가 일치하지 않습니다."),
  PASSWORD_MISMATCH(HttpStatus.CONFLICT, "비밀번호가 일치하지 않습니다."),
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다."),
  DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용중인 닉네임입니다."),
  DUPLICATE_PASSWORD(HttpStatus.CONFLICT, "동일한 비밀번호로 변경할 수 없습니다."),
  AUTH_CODE_MISMATCH(HttpStatus.CONFLICT, "인증 코드가 일치하지 않습니다."),
  AUTH_CODE_ALREADY_VERIFIED(HttpStatus.CONFLICT, "이미 인증된 코드입니다."),
  AUTH_CODE_NOT_VERIFIED(HttpStatus.CONFLICT, "인증 코드가 검증되지 않았습니다."),
  ALREADY_BOOKMARKED(HttpStatus.CONFLICT, "이미 즐겨찾기한 대여 스테이션입니다."),
  AMOUNT_MISMATCH(HttpStatus.CONFLICT, "금액이 일치하지 않습니다."),
  LOCK_CONFLICT(HttpStatus.CONFLICT, "현재 요청이 많아 처리가 지연되고 있습니다. 다시 시도해주세요."),
  ORDER_INFO_EXISTS(HttpStatus.CONFLICT, "해당 대여물품에 대한 주문 정보가 이미 존재합니다."),
  RENTAL_ITEM_STOCK_EMPTY(HttpStatus.CONFLICT, "대여물품 재고가 부족합니다."),
  RENTAL_ITEM_ALREADY_RENTED(HttpStatus.CONFLICT, "해당 물품은 이미 대여 중 입니다."),
  PROFILE_IMAGE_ALREADY_DEFAULT(HttpStatus.CONFLICT, "이미 기본 프로필 이미지입니다."),

  // 500 INTERNAL_SERVER_ERROR
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다. 관리자에게 문의하세요."),
  JSON_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "객체를 JSON으로 변환하는 중 오류가 발생했습니다."),
  JSON_DESERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JSON을 객체로 변환하는 중 오류가 발생했습니다."),
  MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메일 전송에 실패했습니다."),
  LOCK_ACQUISITION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 작업을 처리할 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;

}