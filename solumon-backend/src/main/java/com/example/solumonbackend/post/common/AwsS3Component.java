package com.example.solumonbackend.post.common;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.solumonbackend.global.exception.ErrorCode;
import com.example.solumonbackend.post.model.AwsS3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Component {

  private final AmazonS3 amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public AwsS3 upload(MultipartFile multipartFile, String dirName) throws IOException {
    // MultipartFile -> File 객체로 변환
    if (multipartFile.isEmpty()) {
      return null;
    }
    File file = convertMultipartFileToFile(multipartFile)
        .orElseThrow(() -> new IllegalArgumentException(ErrorCode.IMAGE_CAN_NOT_SAVE.getDescription()
            + " : MultipartFile -> File convert fail"));
    log.debug("MultipartFile -> File 변환 완료");

    return upload(file, dirName);
  }

  private AwsS3 upload(File file, String dirName) {
    String key = randomFileName(file, dirName); // 객체(이미지) 이름
    String path = putS3(file, key); // 해당 객체의 절대 경로
    removeFile(file); // 로컬 파일 삭제

    return AwsS3.builder()
        .key(key)
        .path(path)
        .build();
  }

  private String randomFileName(File file, String dirName) {
    return dirName + "/" + UUID.randomUUID() + file.getName();
  }

  private String putS3(File uploadFile, String fileName) {
    // 파일명(키 값)을 통해 버킷에 업로드
    amazonS3.putObject(new PutObjectRequest(bucket, fileName, uploadFile)
        .withCannedAcl(CannedAccessControlList.PublicRead));
    log.debug("S3 버킷에 저장 완료 : {}", fileName);

    return getS3(bucket, fileName);
  }

  private String getS3(String bucket, String fileName) {
    // 버킷명과 파일명으로 구성되는 url get
    return amazonS3.getUrl(bucket, fileName).toString();
  }

  private void removeFile(File file) {
    file.delete();
  }

  public Optional<File> convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
    // 현재 작업 디렉토리와 원래 파일 이름을 결합해 새로운 file 객체 생성
    File file = new File(System.getProperty("user.dir") + "/" + multipartFile.getOriginalFilename());
    log.debug("file 객체 생성");
    multipartFile.transferTo(file);

    return Optional.of(file);
  }

  public void remove(AwsS3 awsS3) {
    // 파일명(키 값)을 통해 s3에서 삭제
    if (!amazonS3.doesObjectExist(bucket, awsS3.getKey())) {
      throw new AmazonS3Exception("이미지 삭제에 실패했습니다 : Object " + awsS3.getKey() + " does not exist");
    }
    amazonS3.deleteObject(bucket, awsS3.getKey());
    log.debug("S3에서 이미지 삭제 완료 : {}", awsS3.getKey());
  }

  public void removeAll(List<AwsS3> awsS3List) {
    awsS3List.stream()
        .filter(Objects::nonNull)
        .forEach(this::remove);
  }

}
