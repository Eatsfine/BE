package com.eatsfine.domain.tableimage.service;

import com.eatsfine.domain.tableimage.dto.response.TableImageResDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TableImageCommandService {

    TableImageResDto.UploadTableImageDto uploadTableImage(Long storeId, List<MultipartFile> files, String email);

    TableImageResDto.DeleteTableImageDto deleteTableImage(Long storeId, List<Long> tableImageIds, String email);
}
