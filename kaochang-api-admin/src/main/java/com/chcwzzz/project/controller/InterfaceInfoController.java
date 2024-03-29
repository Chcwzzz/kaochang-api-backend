package com.chcwzzz.project.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chcwzzz.common.common.*;
import com.chcwzzz.common.model.entity.UserInterfaceInfo;
import com.chcwzzz.common.model.vo.UserInterfaceInfoVO;
import com.chcwzzz.project.annotation.AuthCheck;
import com.chcwzzz.common.constant.InterfaceStatusConstant;
import com.chcwzzz.common.constant.UserConstant;
import com.chcwzzz.project.exception.BusinessException;
import com.chcwzzz.project.exception.ThrowUtils;
import com.chcwzzz.common.model.dto.Interfaceinfo.InterfaceInfoAddRequest;
import com.chcwzzz.common.model.dto.Interfaceinfo.InterfaceInfoQueryRequest;
import com.chcwzzz.common.model.dto.Interfaceinfo.InterfaceInfoUpdateRequest;
import com.chcwzzz.common.model.dto.Interfaceinfo.InterfaceInvokeRequest;
import com.chcwzzz.common.model.entity.InterfaceInfo;
import com.chcwzzz.common.model.entity.User;
import com.chcwzzz.common.model.vo.InterfaceInfoVO;
import com.chcwzzz.project.service.InterfaceInfoService;
import com.chcwzzz.project.service.UserInterfaceInfoService;
import com.chcwzzz.project.service.UserService;
import com.chcwzzz.sdk.client.KaochangClient;
import com.chcwzzz.sdk.model.DevRequest;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子接口
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private KaochangClient kaochangClient;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        //前端可能传递空的字段，后端需要校验，把不符合规则的字段去除掉
        if (CollUtil.isNotEmpty(interfaceInfoAddRequest.getRequestParams())) {
            List<RequestParams> requestParams = interfaceInfoAddRequest.getRequestParams()
                    .stream().filter(requestParam -> {
                        String paramName = requestParam.getParamName();
                        String description = requestParam.getDescription();
                        String type = requestParam.getType();
                        String required = requestParam.getRequired();
                        return !StringUtils.isAnyBlank(paramName, description, type, required);
                    }).collect(Collectors.toList());
            interfaceInfo.setRequestParams(GSON.toJson(requestParams));
        }
        if (CollUtil.isNotEmpty(interfaceInfoAddRequest.getResponseParams())) {
            List<ResponseParams> responseParams = interfaceInfoAddRequest.getResponseParams()
                    .stream().filter(requestParam -> {
                        String paramName = requestParam.getParamName();
                        String description = requestParam.getDescription();
                        String type = requestParam.getType();
                        return !StringUtils.isAnyBlank(paramName, description, type);
                    }).collect(Collectors.toList());
            interfaceInfo.setResponseParams(GSON.toJson(responseParams));
        }
        //校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());

        boolean result = interfaceInfoService.save(interfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param interfaceInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        //前端可能传递空的字段，后端需要校验，把不符合规则的字段去除掉
        if (CollUtil.isNotEmpty(interfaceInfoUpdateRequest.getRequestParams())) {
            List<RequestParams> requestParams = interfaceInfoUpdateRequest.getRequestParams()
                    .stream().filter(requestParam -> {
                        String paramName = requestParam.getParamName();
                        String description = requestParam.getDescription();
                        String type = requestParam.getType();
                        String required = requestParam.getRequired();
                        return !StringUtils.isAnyBlank(paramName, description, type, required);
                    }).collect(Collectors.toList());
            interfaceInfo.setRequestParams(GSON.toJson(requestParams));
        }
        if (CollUtil.isNotEmpty(interfaceInfoUpdateRequest.getResponseParams())) {
            List<ResponseParams> responseParams = interfaceInfoUpdateRequest.getResponseParams()
                    .stream().filter(requestParam -> {
                        String paramName = requestParam.getParamName();
                        String description = requestParam.getDescription();
                        String type = requestParam.getType();
                        return !StringUtils.isAnyBlank(paramName, description, type);
                    }).collect(Collectors.toList());
            interfaceInfo.setResponseParams(GSON.toJson(responseParams));
        }
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<UserInterfaceInfoVO> getInterfaceInfoById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        UserInterfaceInfoVO userInterfaceInfoVO = new UserInterfaceInfoVO();
        BeanUtils.copyProperties(interfaceInfo, userInterfaceInfoVO);
        if (loginUser != null) {
            UserInterfaceInfo one = userInterfaceInfoService.lambdaQuery()
                    .eq(UserInterfaceInfo::getUserid, loginUser.getId())
                    .eq(UserInterfaceInfo::getInterfaceinfoid, id)
                    .one();
            if (one != null) {
                Integer userleftinvokes = one.getUserleftinvokes();
                Integer usertotalinvokes = one.getUsertotalinvokes();
                userInterfaceInfoVO.setUsertotalinvokes(usertotalinvokes);
                userInterfaceInfoVO.setUserleftinvokes(userleftinvokes);
            }
        }
        return ResultUtils.success(userInterfaceInfoVO);
    }

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                       HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        Page<InterfaceInfoVO> interfaceInfoVOPage = new Page<>();
        BeanUtils.copyProperties(interfaceInfoPage, interfaceInfoVOPage);
        interfaceInfoVOPage.setRecords(interfaceInfoPage.getRecords().stream().map(record -> {
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(record, interfaceInfoVO);
            String requestParams = record.getRequestParams();
            if (StringUtils.isNotBlank(requestParams)) {
                interfaceInfoVO.setRequestParams(GSON.fromJson(requestParams, new TypeToken<List<RequestParams>>() {
                }.getType()));
            }
            String responseParams = record.getResponseParams();
            if (StringUtils.isNotBlank(responseParams)) {
                interfaceInfoVO.setResponseParams(GSON.fromJson(responseParams, new TypeToken<List<ResponseParams>>() {
                }.getType()));
            }
            return interfaceInfoVO;
        }).collect(Collectors.toList()));

        return ResultUtils.success(interfaceInfoVOPage);
    }

    /**
     * 上线接口
     *
     * @param idRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/online")
    public BaseResponse<Boolean> interfaceOnline(@RequestBody IdRequest idRequest) {
        Long id = idRequest.getId();
        //1.参数校验
        if (idRequest == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.校验接口是否存在
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //3.校验接口是否能够正常调用
        DevRequest devRequest = new DevRequest();
        String method = interfaceInfo.getMethod();
        String requestExample = interfaceInfo.getRequestExample();
        devRequest.setUrl(requestExample);
        devRequest.setBody("null");
        devRequest.setMethod(method);
        if ("GET".equals(method)) {
            String response = kaochangClient.request(devRequest);

            if (StrUtil.isBlank(response)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口验证失败");
            }
        }
        if ("POST".equals(method)) {

        }

        //上线接口
        interfaceInfo.setStatus(InterfaceStatusConstant.ONLINE);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 下线接口
     *
     * @param idRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/offline")
    public BaseResponse<Boolean> interfaceOffline(@RequestBody IdRequest idRequest) {
        Long id = idRequest.getId();
        //1.参数校验
        if (idRequest == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.校验接口是否存在
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        //下线接口
        interfaceInfo.setStatus(InterfaceStatusConstant.OFFLINE);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }


    /**
     * 在线调试调用接口
     *
     * @param interfaceInvokeRequest
     * @param request
     * @return
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> interfaceInvoke(@RequestBody InterfaceInvokeRequest interfaceInvokeRequest, HttpServletRequest request) {
        if (interfaceInvokeRequest == null || interfaceInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long id = interfaceInvokeRequest.getId();
        List<UserRequestParams> requestParams = interfaceInvokeRequest.getRequestParams();
        String url = interfaceInvokeRequest.getUrl();
        // 判断是否存在
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        if (Objects.equals(interfaceInfo.getStatus(), InterfaceStatusConstant.OFFLINE)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口已下线");
        }
        User loginUser = userService.getLoginUser(request);
        UserInterfaceInfo one = userInterfaceInfoService.lambdaQuery()
                .eq(UserInterfaceInfo::getUserid, loginUser.getId())
                .eq(UserInterfaceInfo::getInterfaceinfoid, id)
                .one();
        if (one == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未开通该接口");
        }
        if (one.getUserleftinvokes() <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "无剩余调用次数，请重新开通接口获取次数");
        }
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        KaochangClient tempClient = new KaochangClient(accessKey, secretKey);
        DevRequest devRequest = new DevRequest();
        devRequest.setUrl(url);
        devRequest.setBody("null");
        if ("POST".equals(interfaceInvokeRequest.getMethod())) {
            if (CollUtil.isNotEmpty(requestParams)) {
                Map<String, Object> params = new HashMap<>();
                for (UserRequestParams requestParam : requestParams) {
                    String paramName = requestParam.getParamName();
                    String value = requestParam.getValue().toString();
                    if (StrUtil.isAllNotBlank(paramName, value)) {
                        params.put(paramName, value);
                    }
                }
                devRequest.setBody(params);
            } else {
                return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            }
        }
        if ("GET".equals(interfaceInvokeRequest.getMethod())) {
            if (CollUtil.isNotEmpty(requestParams)) {
                Map<String, Object> params = new HashMap<>();
                for (UserRequestParams requestParam : requestParams) {
                    String paramName = requestParam.getParamName();
                    Object value = requestParam.getValue();
                    params.put(paramName, value);
                }
                devRequest.setBody(params);
            }
        }
        devRequest.setMethod(interfaceInvokeRequest.getMethod());
        String res = tempClient.request(devRequest);
        return ResultUtils.success(res);
    }

    /**
     * 根据url和method查询接口是否存在
     *
     * @param url
     * @param method
     * @return
     */
    @GetMapping("/getInterfaceInfoByUrlAndMethod")
    public InterfaceInfo getInterfaceInfoByUrlAndMethod(@RequestParam String url, @RequestParam String method) {
        //1.参数校验
        if (StringUtils.isAnyBlank(url, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return interfaceInfoService.lambdaQuery()
                .eq(InterfaceInfo::getUrl, url)
                .eq(InterfaceInfo::getMethod, method)
                .one();
    }

    // endregion
}
