package com.chcwzzz.project.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chcwzzz.common.common.BaseResponse;
import com.chcwzzz.common.common.ErrorCode;
import com.chcwzzz.common.common.IdRequest;
import com.chcwzzz.common.common.ResultUtils;
import com.chcwzzz.common.model.dto.Interfaceinfo.InterfaceInfoQueryRequest;
import com.chcwzzz.common.model.entity.InterfaceInfo;
import com.chcwzzz.common.model.entity.User;
import com.chcwzzz.common.model.entity.UserInterfaceInfo;
import com.chcwzzz.common.model.vo.UserInterfaceInfoVO;
import com.chcwzzz.project.service.InterfaceInfoService;
import com.chcwzzz.project.service.UserInterfaceInfoService;
import com.chcwzzz.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author 烤肠
 * @Date 2024/3/5 17:06
 */
@Slf4j
@RestController
@RequestMapping("/userInterfaceInfo")
@RequiredArgsConstructor
public class UserInterfaceInfoController {
    private final UserInterfaceInfoService userInterfaceInfoService;
    private final InterfaceInfoService interfaceInfoService;
    private final UserService userService;

    /**
     * 查询用户拥有的接口信息
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<List<UserInterfaceInfoVO>> listInterfaceInfoVOByUserIdPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        String interfaceName = interfaceInfoQueryRequest.getInterfaceName();
        Long current = interfaceInfoQueryRequest.getCurrent();
        Long pageSize = interfaceInfoQueryRequest.getPageSize();
        if (current == null) {
            current = 1L;
        }
        if (pageSize == null) {
            pageSize = 10L;
        }
        Page<UserInterfaceInfo> page = new Page<>(current, pageSize);
        List<UserInterfaceInfo> records = userInterfaceInfoService.page(page).getRecords();
        if (CollUtil.isNotEmpty(records)) {
            List<UserInterfaceInfoVO> result = new ArrayList<>();
            List<Long> userInterfaceInfoId = records.stream().map(UserInterfaceInfo::getInterfaceinfoid).collect(Collectors.toList());
            //根据用户接口的id查询出接口对应的信息，并按照id进行分组
            Map<Long, List<InterfaceInfo>> interfaceInfoMap = interfaceInfoService.lambdaQuery()
                    .like(interfaceName != null, InterfaceInfo::getInterfaceName, interfaceName)
                    .in(InterfaceInfo::getId, userInterfaceInfoId)
                    .list()
                    .stream()
                    .collect(Collectors.groupingBy(InterfaceInfo::getId));
            for (UserInterfaceInfo record : records) {
                List<InterfaceInfo> interfaceInfos = interfaceInfoMap.get(record.getId());
                if (CollUtil.isNotEmpty(interfaceInfos)) {
                    UserInterfaceInfoVO userInterfaceInfoVO = new UserInterfaceInfoVO();
                    BeanUtils.copyProperties(record, userInterfaceInfoVO);
                    userInterfaceInfoVO.setInterfaceName(interfaceInfos.get(0).getInterfaceName());
                    result.add(userInterfaceInfoVO);
                }
            }

            return ResultUtils.success(result);
        }
        return ResultUtils.success(ListUtil.empty());
    }

    /**
     * 根据用户id和接口id查询用户对该接口调用的相关信息
     *
     * @param userId
     * @param interfaceId
     * @return
     */
    @GetMapping("/getUserLeftInvokes")
    public UserInterfaceInfo getUserLeftInvokes(Long userId, Long interfaceId) {
        return userInterfaceInfoService.lambdaQuery()
                .eq(UserInterfaceInfo::getUserid, userId)
                .eq(UserInterfaceInfo::getInterfaceinfoid, interfaceId)
                .one();
    }

    /**
     * 开通接口
     *
     * @param idRequest
     * @param request
     * @return
     */
    @PostMapping("/activateInterface")
    public BaseResponse activateInterface(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Long interfaceinfoid = idRequest.getId();
        UserInterfaceInfo one = userInterfaceInfoService.lambdaQuery()
                .eq(UserInterfaceInfo::getUserid, userId)
                .eq(UserInterfaceInfo::getInterfaceinfoid, interfaceinfoid)
                .one();
        if (one == null) {
            UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserid(userId);
            userInterfaceInfo.setInterfaceinfoid(interfaceinfoid);
            userInterfaceInfo.setUserleftinvokes(200);
            boolean save = userInterfaceInfoService.save(userInterfaceInfo);
            return ResultUtils.success("接口开通成功");
        } else if (one.getUserleftinvokes() <= 0) {
            one.setUserleftinvokes(200);
            userInterfaceInfoService.updateById(one);
            return ResultUtils.success("接口调用次数增加成功");
        } else {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "接口已开通");
        }
    }

    /**
     * 更新接口调用次数
     *
     * @param userInterfaceInfo
     * @return
     */
    @Transactional
    @PostMapping("/invokeUserInterfaceCount")
    public boolean invokeUserInterfaceCount(@RequestBody UserInterfaceInfo userInterfaceInfo) {
        //乐观锁更新调用次数，防止用户恶意刷接口
        boolean updateUserInterfaceInfo = userInterfaceInfoService.lambdaUpdate()
                .eq(UserInterfaceInfo::getUserid, userInterfaceInfo.getUserid())
                .eq(UserInterfaceInfo::getInterfaceinfoid, userInterfaceInfo.getInterfaceinfoid())
                .gt(UserInterfaceInfo::getUserleftinvokes, 0)
                .setSql("userTotalInvokes = userTotalInvokes + 1,userLeftInvokes = userLeftInvokes - 1")
                .update();
        //更新接口调用总次数
        boolean updateInterfaceInfo = interfaceInfoService.lambdaUpdate()
                .eq(InterfaceInfo::getId, userInterfaceInfo.getInterfaceinfoid())
                .setSql("totalInvokes = totalInvokes + 1")
                .update();
        return updateUserInterfaceInfo && updateInterfaceInfo;
    }
}
