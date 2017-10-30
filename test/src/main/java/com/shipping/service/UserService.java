package com.shipping.service;

import com.shipping.domain.dto.LoginInfo;
import com.shipping.domain.enums.FunctionTypeE;
import com.shipping.domain.enums.UserTypeE;
import com.shipping.domain.sys.File;
import com.shipping.domain.sys.Function;
import com.shipping.domain.sys.Permission;
import com.shipping.domain.sys.Role;
import com.shipping.domain.sys.User;
import com.shipping.internal.InfoCache;
import com.shipping.repository.sys.FileRepository;
import com.shipping.repository.sys.FunctionRepository;
import com.shipping.repository.sys.UserRepository;
import org.hibernate.criterion.Restrictions;
import org.spin.core.ErrorCode;
import org.spin.core.auth.Authenticator;
import org.spin.core.auth.KeyInfo;
import org.spin.core.auth.RolePermission;
import org.spin.core.auth.SecretManager;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.BooleanExt;
import org.spin.core.util.DigestUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.extend.RepositoryContext;
import org.spin.data.query.CriteriaBuilder;
import org.spin.core.session.SessionManager;
import org.spin.web.FileOperator;
import org.spin.wx.AccessToken;
import org.spin.wx.WxHelper;
import org.spin.wx.base.WxUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户服务
 * <p>Created by xuweinan on 2017/4/20.</p>
 *
 * @author xuweinan
 */
@Service
public class UserService implements Authenticator<User> {

    @Autowired
    private UserRepository userDao;

    @Autowired
    private FileRepository fileDao;

    @Autowired
    private FunctionRepository functionDao;

    @Autowired
    private SecretManager secretManager;

    @Autowired
    private RepositoryContext repositoryContext;

    @Override
    public User getSubject(Object identity) {
        return repositoryContext.getRepo(User.class).get(Long.parseLong(identity.toString()));
    }

    @Override
    public void preCheck(User user) {
        // do nothing
    }

    @Override
    public RolePermission getRolePermissionList(Object identity) {
        // 角色权限
        Long id = (Long) identity;
        RolePermission rolePermission = InfoCache.permissionCache.get(id);
        if (Objects.isNull(rolePermission)) {
            rolePermission = new RolePermission();
            User user = userDao.get(id);
            rolePermission.setUserIdentifier(id);
            rolePermission.setPermissions(user.getPermissions().stream().map(Permission::getCode).collect(Collectors.toList()));
            rolePermission.setRoles(user.getRoles().stream().map(Role::getCode).collect(Collectors.toList()));
            InfoCache.permissionCache.put(id, rolePermission);
        }
        return rolePermission;
    }

    @Override
    public boolean authenticate(Object id, String password) {
        if (null == id || StringUtils.isEmpty(password))
            return false;
        User user;
        try {
            user = userDao.get(Long.parseLong(id.toString()));
        } catch (NumberFormatException e) {
            user = userDao.findOne(CriteriaBuilder.newInstance().eq("userName", id.toString()));
        }
        return user != null && !StringUtils.isEmpty(user.getPassword()) && DigestUtils.sha256Hex(password + user.getSalt()).equals(user.getPassword());
    }

    @Override
    public boolean checkAuthorities(Object id, String authRouter) {
        // 权限验证
        return BooleanExt.of(StringUtils.isEmpty(authRouter), boolean.class).yes(() -> true).otherwise(() -> {
            List<Function> apis = getUserFunctions((Long) id).get(FunctionTypeE.API);
            return apis.stream().anyMatch(a -> authRouter.equals(a.getCode()));
        });
    }

    @Override
    public void logAccess(Object subject, LocalDateTime accessTime, String msg) {
        // 不记录日志
    }

    @Transactional
    public User saveUser(User user) {
        return userDao.save(user);
    }

    public User getUser(Long id) {
        return userDao.get(id);
    }

    public User getWxUser(String openid) {
        return userDao.findOne("openid", openid);
    }

    @Transactional
    public User createUserFromWx(String openId) {
        AccessToken accessToken = AccessToken.getDefaultOAuthInstance();
        WxUserInfo wxUserInfo = WxHelper.getUserInfo(accessToken.getToken(), openId);
        User user = new User();
        user.setOpenId(openId);
        user.setNickname(wxUserInfo.getNickname());
        user.setUserType(UserTypeE.普通用户);

        FileOperator.UploadResult rs = FileOperator.saveFileFromUrl(wxUserInfo.getHeadimgurl());
        File file = new File();
        file.setGuid(UUID.randomUUID().toString());
        file.setOriginName(rs.getOriginName());
        file.setFileName(rs.getStoreName().substring(rs.getStoreName().lastIndexOf('/') + 1));
        file.setFilePath(rs.getStoreName());
        file.setExtension(rs.getExtention());
        file.setSize(rs.getSize());
        file = fileDao.save(file);

        user.setHeadImg(file);
        return userDao.save(user);
    }

    /**
     * 常规登录
     */
    public LoginInfo login(String identity, String password) {
        return checkUser(identity, password, null, true);
    }

    /**
     * 注销
     */
    public void logout(String key) {
        secretManager.invalidKeyByKeyStr(key, true);
    }

    /**
     * 密钥登录
     */
    public LoginInfo keyLogin(String key) {
        KeyInfo info;
        try {
            info = secretManager.getKeyInfo(key);
            secretManager.invalidKeyByKeyStr(key, false);
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.SECRET_INVALID);
        }
        User user = userDao.get(Long.parseLong(info.getIdentifier()));
        if (null != user) {
            LoginInfo loginInfo;
            if (StringUtils.isNotEmpty(user.getPassword())) {
                loginInfo = checkUser(info.getIdentifier(), info.getSecret(), user, true);
                SessionManager.extendSession(DigestUtils.md5Hex(key), DigestUtils.md5Hex(loginInfo.getKeyInfo().get("key").toString()));
                return loginInfo;
            } else if (StringUtils.isNotEmpty(user.getOpenId())) {
                loginInfo = checkUser(info.getIdentifier(), info.getSecret(), user, false);
                SessionManager.extendSession(DigestUtils.md5Hex(key), DigestUtils.md5Hex(loginInfo.getKeyInfo().get("key").toString()));
                return loginInfo;
            }
        }
        throw new SimplifiedException(ErrorCode.SECRET_INVALID);
    }

    /**
     * 微信授权登录
     */
    public LoginInfo wxLogin(String code) {
        AccessToken accessToken;
        try {
            accessToken = AccessToken.getDefaultOAuthInstance(code);
        } catch (Exception e) {
            throw new SimplifiedException(ErrorCode.INVALID_PARAM, "非法的code");
        }

        User user = getWxUser(accessToken.getOpenId());
        if (null == user) {
            // 创建新用户并关联微信
            user = createUserFromWx(accessToken.getOpenId());
        }
        LoginInfo dto = new LoginInfo();
        dto.setUserId(user.getId());
        dto.setUserInfo(user);
        KeyInfo keyInfo = secretManager.generateKey(user.getId().toString(), user.getOpenId(), "openId");
        dto.setKeyInfo(keyInfo, secretManager.getKeyExpiredIn());
        dto.setTokenInfo(secretManager.generateToken(keyInfo.getKey()), secretManager.getTokenExpiredIn());
        return dto;
    }

    private LoginInfo checkUser(String identity, String secret, User user, boolean isPassword) {
        boolean authenticated;
        if (isPassword)
            authenticated = authenticate(identity, secret);
        else
            authenticated = user.getOpenId().equals(secret);
        if (authenticated) {
            LoginInfo dto = new LoginInfo();
            if (null == user) {
                try {
                    user = userDao.get(Long.parseLong(identity));
                } catch (NumberFormatException ignore) {
                    user = userDao.findOne(CriteriaBuilder.newInstance().eq("userName", identity));
                }
            }
            dto.setUserId(user.getId());
            dto.setUserInfo(user);
            KeyInfo keyInfo = secretManager.generateKey(user.getId().toString(), secret, "password");
            dto.setKeyInfo(keyInfo, secretManager.getKeyExpiredIn());
            dto.setTokenInfo(secretManager.generateToken(keyInfo.getKey()), secretManager.getTokenExpiredIn());
            return dto;
        } else {
            throw new SimplifiedException("登录凭据无效");
        }
    }

    public Map<String, Object> getCurrentUser() {
        User user = userDao.get(SessionManager.getCurrentUser().getId());
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", user.getId());
        userMap.put("nickName", user.getNickname());
        userMap.put("headImg", null == user.getHeadImg() ? null : user.getHeadImg().getFilePath());
        userMap.put("createTime", user.getCreateTime());
        return userMap;
    }

    public List<User> userList() {
        return userDao.list(CriteriaBuilder.newInstance().notEq("userType", UserTypeE.微信用户));
    }

    public Map<FunctionTypeE, List<Function>> getUserFunctions(Long id) {
        Map<FunctionTypeE, List<Function>> result = InfoCache.functionCache.get(id);
        if (Objects.isNull(result)) {
            RolePermission rolePermission = getRolePermissionList(SessionManager.getCurrentUser().getId());
            CriteriaBuilder cb = CriteriaBuilder.newInstance();
            if (rolePermission.getPermissions().isEmpty()) {
                cb.isNull("permission");
            } else {
                cb.or(Restrictions.isNull("permission"),
                    Restrictions.in("permission.code", rolePermission.getPermissions()));
            }
            List<Function> functions = functionDao.list(cb);
            result = functions.stream().collect(Collectors.groupingBy(Function::getType));
            if (result.isEmpty()) {
                result.put(FunctionTypeE.API, new ArrayList<>());
                result.put(FunctionTypeE.MEMU, new ArrayList<>());
            }
            InfoCache.functionCache.put(id, result);
        }
        return result;
    }
}
