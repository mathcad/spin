package org.spin.cloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.annotation.UtilClass;
import org.spin.cloud.feign.FeignInterceptor;
import org.spin.cloud.feign.FeignResolver;
import org.spin.cloud.throwable.BizException;
import org.spin.cloud.vo.CurrentUser;
import org.spin.cloud.vo.MailVo;
import org.spin.cloud.vo.SmsTemplateVariableVo;
import org.spin.cloud.vo.SmsTemplateVo;
import org.spin.cloud.web.interceptor.CustomizeRouteInterceptor;
import org.spin.cloud.web.interceptor.GrayInterceptor;
import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.collection.Pair;
import org.spin.core.collection.Triple;
import org.spin.core.collection.Tuple;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.ArrayUtils;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.spin.core.util.Util;
import org.spin.core.util.http.Http;
import org.spin.web.RestfulResponse;
import org.spin.web.throwable.FeignHttpException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 公共远程服务客户端
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/2/20</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@UtilClass
public final class RemoteClient extends Util {
    private static final Logger logger = LoggerFactory.getLogger(RemoteClient.class);
    private static final String MSG_SERVICE = "BONADE-MESSAGE";
    private static final String ADMIN_SERVICE = "BONADE-ADMIN";
    private static final String UAAC_SERVICE = "BONADE-UAAC";

    private static final ParameterizedTypeReference<RestfulResponse<Void>> VOID_ENTITY = new ParameterizedTypeReference<RestfulResponse<Void>>() {
    };
    private static final ParameterizedTypeReference<RestfulResponse<Object>> OBJECT_ENTITY = new ParameterizedTypeReference<RestfulResponse<Object>>() {
    };
    private static final ParameterizedTypeReference<RestfulResponse<String>> STRING_ENTITY = new ParameterizedTypeReference<RestfulResponse<String>>() {
    };
    private static final ParameterizedTypeReference<RestfulResponse<List<String>>> STRING_LIST_ENTITY = new ParameterizedTypeReference<RestfulResponse<List<String>>>() {
    };

    private static RestTemplate restTemplate;

    private RemoteClient() {
    }

    static {
        Util.registerLatch(RemoteClient.class);
    }

    static void init(RestTemplate restTemplate) {
        RemoteClient.restTemplate = restTemplate;
        Util.ready(RemoteClient.class);
    }

    /**
     * 发送模板短信(支持批量)
     *
     * @param smsTemplateVo 短信VO
     */
    public static void sendTmplSmsMsg(SmsTemplateVo smsTemplateVo) {
        if (null == smsTemplateVo || StringUtils.isEmpty(smsTemplateVo.getPhone())) {
            throw new BizException("发送的手机号码不能为空");
        }

        if (StringUtils.isEmpty(smsTemplateVo.getTemplateCode())) {
            throw new BizException("发送的短信模板编码不能为空");
        }

        rmi(MSG_SERVICE, "v1/sms/internal/constant", Http.POST, "远程调用异常, 短信发送失败", smsTemplateVo, VOID_ENTITY);
    }

    /**
     * 发送模板变量短信(支持批量)
     *
     * @param templateVariableVo 短信VO
     */
    public static void sendVariableTmplSmsMsg(SmsTemplateVariableVo templateVariableVo) {
        if (null == templateVariableVo || CollectionUtils.isEmpty(templateVariableVo.getParams())) {
            throw new BizException("发送短信的参数列表不能为空");
        }

        if (StringUtils.isEmpty(templateVariableVo.getTemplateCode())) {
            throw new BizException("发送的短信模板编码不能为空");
        }

        rmi(MSG_SERVICE, "v1/sms/internal/variable", Http.POST, "远程调用错误, 变量短信发送失败", templateVariableVo, VOID_ENTITY);
    }


    /**
     * 发送系统内邮件
     *
     * @param mailVo 邮件VO
     */
    public static void sendSystemMail(MailVo mailVo) {
        if (null == mailVo || CollectionUtils.isEmpty(mailVo.getReceivers())) {
            throw new BizException("邮件接收人列表不能为空");
        }

        if (StringUtils.isEmpty(mailVo.getType())) {
            throw new BizException("邮件类型不能为空");
        }

        if (StringUtils.isEmpty(mailVo.getContent())) {
            throw new BizException("邮件内容不能为空");
        }

        rmi(MSG_SERVICE, "v1/mail/add", Http.POST, "远程调用错误, 邮件发送失败", mailVo, OBJECT_ENTITY);
    }

    /**
     * 根据code获取流水号
     *
     * @param code 流水号编码
     * @return 流水号
     */
    public static String fetchSequenceNo(String code) {
        Assert.notEmpty(code, "流水号编码不能为空");

        return rmi(ADMIN_SERVICE, "v1/serialNumber/generate/" + StringUtils.urlEncode(code), Http.GET, "远程调用错误, 流水号获取失败", null, STRING_ENTITY);
    }

    /**
     * 解密
     *
     * @param cipher 流水号编码
     * @return 流水号
     */
    public static List<String> decryptInfo(String... cipher) {
        if (ArrayUtils.isEmpty(cipher)) {
            return Collections.emptyList();
        }

        return rmi(UAAC_SERVICE, "v1/auth/decrypt", Http.POST, "远程调用错误, 数据解密失败", cipher, STRING_LIST_ENTITY);
    }

    // region 内部方法
    private static <T> T rmi(String service, String url, Http<?> http, String errMsg, Object param, ParameterizedTypeReference<RestfulResponse<T>> typeToken) {
        Util.awaitUntilReady(RemoteClient.class);
        Pair<Boolean, String> pair = fixUrl(service, url);
        try {
            RestfulResponse<T> entity;
            Triple<String, String, String> header = obtainHeader();
            if (pair.c1) {
                HttpEntity<Object> requestEntity = createHttpEntity(header.c1, header.c2, header.c3, param);
                ResponseEntity<RestfulResponse<T>> response = restTemplate.exchange(pair.c2, Objects.requireNonNull(HttpMethod.resolve(http.getMethod())), requestEntity, typeToken);
                entity = response.getBody();
                if (!response.getStatusCode().is2xxSuccessful()) {
                    logger.warn(errMsg + ":\n{}", response.getBody());
                    if (null == entity) {
                        entity = new RestfulResponse<>();
                        entity.setCodeAndMsg(new ErrorCode(response.getStatusCodeValue(), errMsg));
                        entity.setPath(pair.c2);
                    }
                    throw new FeignHttpException(entity.getStatus(), entity.getPath(), entity.getError(), entity.getMessage(), null);
                }
            } else {
                entity = http.withUrl(pair.c2).withoutStatusCheck()
                    .withHead(FeignInterceptor.X_APP_NAME, Env.getAppName(), FeignInterceptor.X_APP_PROFILE, Env.getActiveProfiles(), HttpHeaders.FROM, header.c1,
                        GrayInterceptor.X_GRAY_INFO, header.c2, CustomizeRouteInterceptor.CUSTOMIZE_ROUTE, header.c3)
                    .withJsonBody(param).execute(typeToken.getType());
            }
            checkResult(pair.c2, entity);
            return entity.getData();
        } catch (SimplifiedException e) {
            throw e;
        } catch (HttpStatusCodeException e) {
            RestfulResponse<?> restfulResponse;
            try {
                restfulResponse = JsonUtils.fromJson(e.getResponseBodyAsString(), RestfulResponse.class);
            } catch (Exception ignore) {
                logger.warn(errMsg, e);
                restfulResponse = RestfulResponse.ok();
                restfulResponse.setStatus(e.getStatusCode().value());
                restfulResponse.setPath(pair.c2);
                restfulResponse.setError(e.getStackTrace()[0].toString());
                restfulResponse.setMessage(errMsg);
            }
            throw new FeignHttpException(restfulResponse.getStatus(), restfulResponse.getPath(), restfulResponse.getError(), restfulResponse.getMessage(), e);
        } catch (SpinException e) {
            logger.warn(errMsg, e);
            throw new FeignHttpException(e.getExceptionType().getCode(), pair.c2, e.getStackTrace()[0].toString(), errMsg, e);
        } catch (Exception e) {
            logger.warn(errMsg, e);
            throw new FeignHttpException(ErrorCode.OTHER.getCode(), pair.c2, e.getStackTrace()[0].toString(), errMsg, e);
        }
    }

    private static Pair<Boolean, String> fixUrl(String service, String path) {
        String defUrl = null;
        if (null != CloudInfrasContext.getCustomizeRoute()) {
            Map<String, String> customizeRoutes = CloudInfrasContext.getCustomizeRoute().c2;
            if (null != customizeRoutes) {
                defUrl = customizeRoutes.get(service);
            }
        }
        if (null == defUrl) {
            defUrl = FeignResolver.getUrl(service);
        }
        return Tuple.of(null == defUrl, null == defUrl ? ("http://" + service + "/" + path) : (defUrl.endsWith("/") ? (defUrl + path) : (defUrl + "/" + path)));
    }

    private static Triple<String, String, String> obtainHeader() {
        String from = null;
        String grayInfo = null;
        String customizeRoutes = null;
        if (null != CurrentUser.getCurrent()) {
            from = CurrentUser.getCurrent().toString();
        }
        if (null != CloudInfrasContext.getGrayInfo()) {
            grayInfo = CloudInfrasContext.getGrayInfo().c1;
        }

        if (null != CloudInfrasContext.getCustomizeRoute()) {
            customizeRoutes = CloudInfrasContext.getCustomizeRoute().c1;
        }

        return Tuple.of(from, grayInfo, customizeRoutes);
    }

    private static <T> HttpEntity<T> createHttpEntity(String from, String grayInfo, String customizeRoute, T body) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotEmpty(Env.getAppName())) {
            requestHeaders.add(FeignInterceptor.X_APP_NAME, Env.getAppName());
        }
        if (StringUtils.isNotEmpty(Env.getActiveProfiles())) {
            requestHeaders.add(FeignInterceptor.X_APP_PROFILE, Env.getActiveProfiles());
        }

        if (null != from) {
            requestHeaders.add(HttpHeaders.FROM, from);
        }

        if (null != grayInfo) {
            requestHeaders.add(GrayInterceptor.X_GRAY_INFO, grayInfo);
        }

        if (null != customizeRoute) {
            requestHeaders.add(CustomizeRouteInterceptor.CUSTOMIZE_ROUTE, customizeRoute);
        }
        return new HttpEntity<>(body, requestHeaders);
    }

    private static <T> void checkResult(String path, RestfulResponse<T> response) {
        if (null == response) {
            throw new FeignHttpException(ErrorCode.NETWORK_EXCEPTION.getCode(), path, null, "远程调用异常, 未获取到有效的返回结果", null);
        }
        if (response.getStatus() != ErrorCode.OK.getCode()) {
            throw new FeignHttpException(response.getStatus(), response.getPath(), response.getError(), response.getMessage(), null);
        }
    }
    // endregion
}
