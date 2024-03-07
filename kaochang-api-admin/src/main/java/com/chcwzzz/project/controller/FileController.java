package com.chcwzzz.project.controller;

import cn.hutool.core.io.FileUtil;
import com.chcwzzz.common.common.BaseResponse;
import com.chcwzzz.common.common.ErrorCode;
import com.chcwzzz.common.common.ResultUtils;
import com.chcwzzz.common.constant.FileConstant;
import com.chcwzzz.common.model.dto.file.UploadFileRequest;
import com.chcwzzz.common.model.entity.User;
import com.chcwzzz.common.model.enums.FileUploadBizEnum;
import com.chcwzzz.common.model.vo.ImageVo;
import com.chcwzzz.project.exception.BusinessException;
import com.chcwzzz.project.service.UserService;
import com.chcwzzz.project.utils.QiNiuOSSUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.UUID;

/**
 * 文件接口
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private QiNiuOSSUtils qiNiuOSSUtils;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<ImageVo> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                            UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        //获取原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        //构造唯一的文件名(不能重复)
        String uuid = UUID.randomUUID().toString();
        int index = originalFilename.lastIndexOf(".");
        String extname = originalFilename.substring(index);
        String newFileName = uuid + extname;
        String filepath = String.format("%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), newFileName);
        try {
            // 上传文件
            qiNiuOSSUtils.upload(filepath, multipartFile);
            ImageVo imageVo = new ImageVo();
            imageVo.setUrl(FileConstant.KODO_HOST + filepath);
            // 返回可访问地址
            return ResultUtils.success(imageVo);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
