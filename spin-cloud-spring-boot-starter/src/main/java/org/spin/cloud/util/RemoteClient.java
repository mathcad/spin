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
import org.spin.cloud.web.interceptor.GrayInterceptor;
import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.collection.Pair;
import org.spin.core.collection.Tuple;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.spin.core.util.http.Http;
import org.spin.web.RestfulResponse;
import org.spin.web.throwable.FeignHttpException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collections;
import java.util.List;

/**
 * 公共远程服务客户端
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/2/20</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@UtilClass
public class RemoteClient {
    private static final Logger logger = LoggerFactory.getLogger(RemoteClient.class);
    private static final String MSG_SERVICE = "BONADE-MESSAGE";
    private static final String ADMIN_SERVICE = "BONADE-ADMIN";
    private static final String UAAC_SERVICE = "BONADE-UAAC";

    private static final TypeToken<RestfulResponse<Void>> VOID_ENTITY = new TypeToken<RestfulResponse<Void>>() {
    };
    private static final TypeToken<RestfulResponse<List<String>>> STRING_LIST_ENTITY = new TypeToken<RestfulResponse<List<String>>>() {
    };

    private static RestTemplate restTemplate;

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

        Pair<Boolean, String> pair = fixUrl(MSG_SERVICE, "v1/sms/internal/constant");
        try {
            RestfulResponse<Void> entity;
            Pair<String, String> header = obtainHeader();
            if (pair.c1) {
                HttpEntity<SmsTemplateVo> requestEntity = createHttpEntity(header.c1, header.c2, smsTemplateVo);
                ResponseEntity<String> response = restTemplate.postForEntity(pair.c2, requestEntity, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    logger.warn("远程调用错误, 短信发送失败:\n{}", response.getBody());
                    throw new FeignHttpException(ErrorCode.NETWORK_EXCEPTION.getCode(), pair.c2, response.getStatusCode().toString(), "远程调用错误, 短信发送失败", null);
                }
                entity = JsonUtils.fromJson(response.getBody(), VOID_ENTITY);

            } else {
                entity = Http.POST.withUrl(pair.c2)
                    .withHead(FeignInterceptor.X_APP_NAME, Env.getAppName(), HttpHeaders.FROM, header.c1, GrayInterceptor.X_GRAY_INFO, header.c2)
                    .withJsonBody(smsTemplateVo).execute(VOID_ENTITY);
            }
            checkResult(pair.c2, entity);
        } catch (SimplifiedException e) {
            throw e;
        } catch (SpinException e) {
            logger.warn("远程调用异常, 短信发送失败", e);
            throw new FeignHttpException(e.getExceptionType().getCode(), pair.c2, e.getStackTrace()[0].toString(), "远程调用异常, 短信发送失败: " + e.getSimpleMessage(), e);
        } catch (Exception e) {
            logger.warn("远程调用异常, 短信发送失败", e);
            throw new FeignHttpException(ErrorCode.OTHER.getCode(), pair.c2, e.getStackTrace()[0].toString(), "远程调用异常, 短信发送失败: " + e.getMessage(), e);
        }
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

        Pair<Boolean, String> pair = fixUrl(MSG_SERVICE, "v1/sms/internal/variable");
        try {
            RestfulResponse<Void> entity;
            Pair<String, String> header = obtainHeader();
            if (pair.c1) {
                HttpEntity<SmsTemplateVariableVo> requestEntity = createHttpEntity(header.c1, header.c2, templateVariableVo);
                ResponseEntity<String> response = restTemplate.postForEntity(pair.c2, requestEntity, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    logger.warn("远程调用错误, 变量短信发送失败:\n{}", response.getBody());
                    throw new FeignHttpException(ErrorCode.NETWORK_EXCEPTION.getCode(), pair.c2, response.getStatusCode().toString(), "远程调用错误, 短信发送失败", null);
                }
                entity = JsonUtils.fromJson(response.getBody(), VOID_ENTITY);

            } else {
                entity = Http.POST.withUrl(pair.c2)
                    .withHead(FeignInterceptor.X_APP_NAME, Env.getAppName(), HttpHeaders.FROM, header.c1, GrayInterceptor.X_GRAY_INFO, header.c2)
                    .withJsonBody(templateVariableVo).execute(VOID_ENTITY);
            }
            checkResult(pair.c2, entity);
        } catch (SimplifiedException e) {
            throw e;
        } catch (SpinException e) {
            logger.warn("远程调用异常, 变量短信发送失败", e);
            throw new FeignHttpException(e.getExceptionType().getCode(), pair.c2, e.getStackTrace()[0].toString(), "远程调用异常, 变量短信发送失败: " + e.getSimpleMessage(), e);
        } catch (Exception e) {
            logger.warn("远程调用异常, 变量短信发送失败", e);
            throw new FeignHttpException(ErrorCode.OTHER.getCode(), pair.c2, e.getStackTrace()[0].toString(), "远程调用异常, 变量短信发送失败: " + e.getMessage(), e);
        }
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

        Pair<Boolean, String> pair = fixUrl(MSG_SERVICE, "v1/mail/add");
        try {
            RestfulResponse<?> entity;
            Pair<String, String> header = obtainHeader();
            if (pair.c1) {
                HttpEntity<MailVo> requestEntity = createHttpEntity(header.c1, header.c2, mailVo);
                ResponseEntity<String> response = restTemplate.postForEntity(pair.c2, requestEntity, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    logger.warn("远程调用错误, 邮件发送失败:\n{}", response.getBody());
                    throw new FeignHttpException(ErrorCode.NETWORK_EXCEPTION.getCode(), pair.c2, response.getStatusCode().toString(), "远程调用错误, 邮件发送失败", null);
                }
                entity = JsonUtils.fromJson(response.getBody(), RestfulResponse.class);

            } else {
                entity = Http.POST.withUrl(pair.c2)
                    .withHead(FeignInterceptor.X_APP_NAME, Env.getAppName(), HttpHeaders.FROM, header.c1, GrayInterceptor.X_GRAY_INFO, header.c2)
                    .withJsonBody(mailVo).execute(RestfulResponse.class);
            }
            checkResult(pair.c2, entity);
        } catch (SimplifiedException e) {
            throw e;
        } catch (SpinException e) {
            logger.warn("远程调用异常, 邮件发送失败", e);
            throw new FeignHttpException(e.getExceptionType().getCode(), pair.c2, e.getStackTrace()[0].toString(), "远程调用异常, 邮件发送失败: " + e.getSimpleMessage(), e);
        } catch (Exception e) {
            logger.warn("远程调用异常, 邮件发送失败", e);
            throw new FeignHttpException(ErrorCode.OTHER.getCode(), pair.c2, e.getStackTrace()[0].toString(), "远程调用异常, 邮件发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据code获取流水号
     *
     * @param code 流水号编码
     * @return 流水号
     */
    public static String fetchSequenceNo(String code) {
        Assert.notEmpty(code, "流水号编码不能为空");

        Pair<Boolean, String> pair = fixUrl(ADMIN_SERVICE, "v1/serialNumber/generate/" + StringUtils.urlEncode(code));
        try {
            RestfulResponse<?> entity;
            Pair<String, String> header = obtainHeader();
            if (pair.c1) {
                HttpEntity<String> requestEntity = createHttpEntity(header.c1, header.c2, null);
                ResponseEntity<String> response = restTemplate.exchange(pair.c2, HttpMethod.GET, requestEntity, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    logger.warn("远程调用错误, 流水号获取失败:\n{}", response.getBody());
                    throw new FeignHttpException(ErrorCode.NETWORK_EXCEPTION.getCode(), pair.c2, response.getStatusCode().toString(), "远程调用错误, 流水号获取失败", null);
                }
                entity = JsonUtils.fromJson(response.getBody(), RestfulResponse.class);

            } else {
                entity = Http.GET.withUrl(pair.c2)
                    .withHead(FeignInterceptor.X_APP_NAME, Env.getAppName(), HttpHeaders.FROM, header.c1, GrayInterceptor.X_GRAY_INFO, header.c2)
                    .execute(RestfulResponse.class);
            }
            checkResult(pair.c2, entity);
            return StringUtils.toString(entity.getData());
        } catch (SimplifiedException e) {
            throw e;
        } catch (SpinException e) {
            logger.warn("远程调用异常, 流水号获取失败", e);
            throw new FeignHttpException(e.getExceptionType().getCode(), pair.c2, e.getStackTrace()[0].toString(), "远程调用异常, 流水号获取失败: " + e.getSimpleMessage(), e);
        } catch (Exception e) {
            logger.warn("远程调用异常, 流水号获取失败", e);
            throw new FeignHttpException(ErrorCode.OTHER.getCode(), pair.c2, e.getStackTrace()[0].toString(), "远程调用异常, 流水号获取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密
     *
     * @param cipher 流水号编码
     * @return 流水号
     */
    public static List<String> decryptInfo(String... cipher) {
        if (CollectionUtils.isEmpty(cipher)) {
            return Collections.emptyList();
        }

        Pair<Boolean, String> pair = fixUrl(UAAC_SERVICE, "v1/auth/decrypt");
        try {
            RestfulResponse<List<String>> entity;
            Pair<String, String> header = obtainHeader();
            if (pair.c1) {
                HttpEntity<String[]> requestEntity = createHttpEntity(header.c1, header.c2, cipher);
                ResponseEntity<String> response = restTemplate.postForEntity(pair.c2, requestEntity, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    logger.warn("远程调用错误, 数据解密失败:\n{}", response.getBody());
                    throw new FeignHttpException(ErrorCode.NETWORK_EXCEPTION.getCode(), pair.c2, response.getStatusCode().toString(), "远程调用错误, 数据解密失败", null);
                }
                entity = JsonUtils.fromJson(response.getBody(), STRING_LIST_ENTITY);

            } else {
                entity = Http.POST.withUrl(pair.c2)
                    .withHead(FeignInterceptor.X_APP_NAME, Env.getAppName(), HttpHeaders.FROM, header.c1, GrayInterceptor.X_GRAY_INFO, header.c2)
                    .withJsonBody(cipher).execute(STRING_LIST_ENTITY);
            }
            checkResult(pair.c2, entity);
            List<String> data = entity.getData();
            if (data.size() != cipher.length) {
                logger.warn("解密后的数据不完整, 数据解密失败:\n{}", StringUtils.join(data, "\n"));
                throw new FeignHttpException(ErrorCode.OTHER.getCode(), pair.c2, null, "解密后的数据不完整, 数据解密失败", null);
            }
            return data;
        } catch (SimplifiedException e) {
            throw e;
        } catch (SpinException e) {
            logger.warn("远程调用异常, 数据解密失败", e);
            throw new FeignHttpException(e.getExceptionType().getCode(), pair.c2, e.getStackTrace()[0].toString(), "远程调用异常, 数据解密失败: " + e.getSimpleMessage(), e);
        } catch (Exception e) {
            logger.warn("远程调用异常, 数据解密失败", e);
            throw new FeignHttpException(ErrorCode.OTHER.getCode(), pair.c2, e.getStackTrace()[0].toString(), "远程调用异常, 数据解密失败: " + e.getMessage(), e);
        }
    }

    private static void init(RestTemplate restTemplate) {
        RemoteClient.restTemplate = restTemplate;
    }

    private static Pair<Boolean, String> fixUrl(String service, String path) {
        String defUrl = FeignResolver.getUrl(service);
        return Tuple.of(null == defUrl, null == defUrl ? ("http://" + service + "/" + path) : (defUrl.endsWith("/") ? (defUrl + path) : (defUrl + "/" + path)));
    }

    private static Pair<String, String> obtainHeader() {
        String from = null;
        String grayInfo = null;
        if (null != CurrentUser.getCurrent()) {
            from = CurrentUser.getCurrent().toString();
        }
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (null != requestAttributes) {
            grayInfo = (String) requestAttributes.getAttribute(GrayInterceptor.X_GRAY_INFO_STR, RequestAttributes.SCOPE_REQUEST);
        }

        return Tuple.of(from, grayInfo);
    }

    private static <T> HttpEntity<T> createHttpEntity(String from, String grayInfo, T body) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add(FeignInterceptor.X_APP_NAME, Env.getAppName());
        if (null != from) {
            requestHeaders.add(HttpHeaders.FROM, from);
        }
        if (null != grayInfo) {
            requestHeaders.add(GrayInterceptor.X_GRAY_INFO, grayInfo);
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
}
