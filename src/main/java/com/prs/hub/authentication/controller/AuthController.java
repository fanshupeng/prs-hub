package com.prs.hub.authentication.controller;

import com.alibaba.fastjson.JSON;
import com.prs.hub.authentication.dto.UserReqDTO;
import com.prs.hub.authentication.dto.UserShowReqDTO;
import com.prs.hub.authentication.service.AuthService;
import com.prs.hub.commons.Authorization;
import com.prs.hub.commons.BaseController;
import com.prs.hub.commons.BaseResult;
import com.prs.hub.commons.CurrentUser;
import com.prs.hub.constant.ResultCodeEnum;
import com.prs.hub.practice.entity.User;
import com.prs.hub.utils.MD5Util;
import com.prs.hub.utils.StringUtils;
import com.prs.hub.utils.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author fansp
 * @since 2022-03-09
 */
@Slf4j
@RestController
@RequestMapping(value = "/prs/hub")
public class AuthController extends BaseController {

    @Autowired
    private AuthService authService;

    /**
     * 注册Controller
     * @param req
     * @param res
     * @param userShowReqDTO
     */
    @RequestMapping(value = "/auth", method = RequestMethod.GET)
    @CrossOrigin("*")
    public BaseResult auth(HttpServletRequest req, HttpServletResponse res,UserShowReqDTO userShowReqDTO) {
        log.info("注册auth方法开始，userShowReqDTO="+JSON.toJSONString(userShowReqDTO));
        Map<String,Object> resultMap = new HashMap<>();
        if(userShowReqDTO  == null) {
            log.info("注册auth方法结束，页面传入的用户数据为空");
            resultMap.put("code", ResultCodeEnum.EMPTY.getCode());
            resultMap.put("msg","用户数据为空");
            return BaseResult.setResultMap(BaseResult.SUCCESS,"接口调用成功",resultMap);
        }
        String email = userShowReqDTO.getEmail();
        String password = userShowReqDTO.getPassword();
        if(StringUtils.isEmpty(email) || StringUtils.isEmpty(password)){
            log.info("注册auth方法结束，用户必填数据email或password为空");
            resultMap.put("code",ResultCodeEnum.EMPTY.getCode());
            resultMap.put("msg","用户必填数据email或password为空");
            return BaseResult.setResultMap(BaseResult.SUCCESS,"接口调用成功",resultMap);
        }

        //组装service入参数据
        User user = new User();
        user.setEmail(email);

        /**
         * 校验是否有相同邮箱的用户
         */
        log.info("注册auth方法调用authService的getUserInfo方法开始，user="+JSON.toJSONString(user));
        BaseResult userInfo = authService.getUserInfo(user);
        log.info("注册auth方法调用authService的getUserInfo方法结束，userInfo="+JSON.toJSONString(userInfo));
        if(userInfo.getData() != null){
            log.info("存在相同email，注册失败");
            resultMap.put("code",ResultCodeEnum.EXISTING.getCode());
            //存在相同email，注册失败
            resultMap.put("msg","The same email already exists, please change it!");
            BaseResult  result = BaseResult.setResultMap(BaseResult.SUCCESS,"接口调用成功",resultMap);
            log.info("注册auth方法查询存在相同用户注册失败\n"+JSON.toJSONString(result));
            return result;
        }

        /**
         * 新增用户
         */
        user.setPassword(MD5Util.MD5Encode(password,"utf-8"));
        user.setName(userShowReqDTO.getName());
        log.info("注册auth方法调用authService的saveOrUpdateUser方法开始，user="+JSON.toJSONString(user));
        BaseResult serviceResult = authService.saveOrUpdateUser(user);
        log.info("注册auth方法调用authService的saveOrUpdateUser方法结束，serviceResult="+JSON.toJSONString(serviceResult));

        if (serviceResult.getCode() != 0){
            resultMap.put("code",ResultCodeEnum.FAIL.getCode());
            resultMap.put("msg","注册失败");
            BaseResult  result = BaseResult.setResultMap(BaseResult.SUCCESS,"接口调用成功",resultMap);
            log.info("注册失败"+JSON.toJSONString(result));
            return result;
        }

        /**
         * 生成token
         */
        String accessToken = this.getAccessToken(user);
        log.info("token="+accessToken);
        
        resultMap.put("code",ResultCodeEnum.SUCCESS.getCode());
        resultMap.put("msg","注册成功");
        resultMap.put("token",accessToken);
        BaseResult  result = BaseResult.setResultMap(BaseResult.SUCCESS,"接口调用成功",resultMap);
        log.info("注册成功"+JSON.toJSONString(result));
        return result;
    }

    /**
     * 登录
     * @param req
     * @param res
     * @param userShowReqDTO
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @CrossOrigin("*")
    public BaseResult login(HttpServletRequest req, HttpServletResponse res, UserShowReqDTO userShowReqDTO){
        log.info("执行登录controller开始userShowReqDTO="+userShowReqDTO);
        Map<String,Object> resultMap = new HashMap<>();
        if(userShowReqDTO  == null) {
            log.info("登录login方法结束，页面传入的用户数据为空");
            resultMap.put("code", ResultCodeEnum.EMPTY.getCode());
            resultMap.put("msg","用户数据为空");
            return BaseResult.setResultMap(BaseResult.SUCCESS,"接口调用成功",resultMap);
        }
        String email = userShowReqDTO.getEmail();
        String password = userShowReqDTO.getPassword();
        if(StringUtils.isEmpty(email) || StringUtils.isEmpty(password)){
            log.info("登录login方法结束，用户必填数据email或password为空");
            resultMap.put("code",ResultCodeEnum.EMPTY.getCode());
            resultMap.put("msg","用户必填数据email或password为空");
            return BaseResult.setResultMap(BaseResult.SUCCESS,"接口调用成功",resultMap);
        }
        //组装service入参数据
        User user = new User();
        user.setEmail(email);
        user.setPassword(MD5Util.MD5Encode(password,"utf-8"));
        log.info("登录login方法调用authService的getUserInfo方法开始，user="+JSON.toJSONString(user));
        BaseResult userInfo = authService.getUserInfo(user);
        log.info("登录login方法调用authService的getUserInfo方法结束，userInfo="+JSON.toJSONString(userInfo));
        if(userInfo.getData() == null){
            log.info("该用户不存在");
            resultMap.put("code",ResultCodeEnum.FAIL.getCode());
            //该用户不存在，登录失败
            resultMap.put("msg","The user does not exist or the password is incorrect");
            BaseResult  result = BaseResult.setResultMap(BaseResult.SUCCESS,"接口调用成功",resultMap);
            log.info("登录login失败\n"+JSON.toJSONString(result));
            return result;
        }
        /**
         * 生成token
         */
        String accessToken = this.getAccessToken(user);
        log.info("token="+accessToken);

        resultMap.put("code",ResultCodeEnum.SUCCESS.getCode());
        resultMap.put("msg","登录成功");
        resultMap.put("token",accessToken);
        BaseResult  result = BaseResult.setResultMap(BaseResult.SUCCESS,"接口调用成功",resultMap);
        log.info("登录成功"+JSON.toJSONString(result));
        return result;
    }

    /**
     * 获取用户详情
     * @param req
     * @param res
     * @param userReqDTO
     * @return
     */
    @RequestMapping(value = "/getUserInfo", method = RequestMethod.GET)
    @CrossOrigin("*")
    @Authorization
    public BaseResult getUserInfo(HttpServletRequest req, HttpServletResponse res,@CurrentUser UserReqDTO userReqDTO){
        log.info("获取用户详情controller,userReqDTO="+JSON.toJSON(userReqDTO));
        return BaseResult.ok("成功",userReqDTO);
    }
}
