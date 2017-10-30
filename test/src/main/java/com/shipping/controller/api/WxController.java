package com.shipping.controller.api;

import com.shipping.domain.sys.File;
import com.shipping.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.RandomStringUtils;
import org.spin.core.util.StringUtils;
import org.spin.web.FileOperator;
import org.spin.web.RestfulResponse;
import org.spin.web.annotation.Needed;
import org.spin.web.annotation.RestfulApi;
import org.spin.wx.AccessToken;
import org.spin.wx.MessageEntity;
import org.spin.wx.WxConfig;
import org.spin.wx.WxHelper;
import org.spin.wx.aes.AesException;
import org.spin.wx.aes.WXBizMsgCrypt;
import org.spin.wx.base.MessageType;
import org.spin.wx.base.PropertyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

/**
 * 微信相关接口
 * Created by xuweinan on 2016/9/29.
 *
 * @author xuweinan
 */
@RestController
@RequestMapping("/api/wx")
public class WxController {
    private static final Logger logger = LoggerFactory.getLogger(WxController.class);
    private static final String ERROR = "ERROR";

    @Autowired
    private FileService fileService;

    /**
     * 公众号验证接口
     */
    @RequestMapping(method = RequestMethod.GET)
    public String verify(String signature, String timestamp, String nonce, String echostr) {
        if (StringUtils.isNotEmpty(echostr) && WxHelper.verifySign(signature, WxConfig.getConfig("default").getToken(), timestamp, nonce)) {
            return echostr;
        }
        return ERROR;
    }

    /**
     * 消息通知回调接口
     */
    @RequestMapping(method = RequestMethod.POST)
    public String recive(String signature, String timestamp, String nonce, @RequestBody byte[] postData) {
        String reqData;
        try {
            reqData = new String(postData, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Read posted data from request error");
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage());
            return ERROR;
        }
        if (logger.isTraceEnabled())
            logger.trace("Encrypted data from request: {}", reqData);

        boolean isEncrypt = reqData.startsWith("<xml><ToUserName>");
        String reqMsgBody = reqData;
        try {
            if (!isEncrypt)
                reqMsgBody = WXBizMsgCrypt.decryptMsg(signature, timestamp, nonce, reqData, WxConfig.getConfig("default"));
        } catch (AesException e) {
            logger.error("Can not decrypt message from encrypted data");
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage(), e);
            return ERROR;
        }
        if (logger.isDebugEnabled())
            logger.debug("Decrypted message from request: {}", reqMsgBody);
        MessageEntity reqEntity = MessageEntity.configEntity(reqMsgBody);
        if (logger.isTraceEnabled()) {
            logger.trace("Message degist post to Server begin");
            logger.trace("From UserID: {}", reqEntity.getPropValue(PropertyType.FROM_USER_NAME));
            logger.trace("MessageType: {}", reqEntity.getPropValue(PropertyType.MSG_TYPE));
            logger.trace("App id: {}", reqEntity.getPropValue(PropertyType.AGENT_ID));
        }
        MessageEntity resEntity = new MessageEntity();
        resEntity.addProperty(PropertyType.TO_USER_NAME, reqEntity.getPropValue(PropertyType.FROM_USER_NAME));
        resEntity.addProperty(PropertyType.FROM_USER_NAME, reqEntity.getPropValue(PropertyType.TO_USER_NAME));
        resEntity.addProperty(PropertyType.CREATE_TIME, Long.toString(System.currentTimeMillis()));
        resEntity.addProperty(PropertyType.MSG_TYPE, MessageType.TEXT);
        resEntity.addProperty(PropertyType.CONTENT, "欢迎关注公众号");
        try {
            return WXBizMsgCrypt.encryptMsg(resEntity.getXmlProp(), Long.toString(System.currentTimeMillis()),
                RandomStringUtils.randomNumeric(10), WxConfig.getConfig("default"));
        } catch (AesException e) {
            return ERROR;
        }
    }


    @RestfulApi(path = "signature")
    public RestfulResponse getSignature(@Needed String url) {
        return RestfulResponse.ok(WxHelper.signature(url));
    }

    /**
     * 根据mediaId，将多媒体文件从微信服务器保存到本地
     */
    @RestfulApi("saveMediaFile")
    public RestfulResponse saveMediaFileFromWxServer(@Needed String mediaId) {
        if (StringUtils.isEmpty(mediaId))
            throw new SimplifiedException("mediaId不能为空");
        String url = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token="
            + AccessToken.getDefaultInstance().getToken() + "&media_id=" + mediaId;
        FileOperator.UploadResult uploadResult = FileOperator.saveFileFromUrl(url);
        uploadResult.setFullName(null);
        File file = fileService.saveFile(uploadResult);
        File res = new File();
        res.setId(file.getId());
        res.setFileName(file.getFileName());
        res.setFilePath(file.getFilePath());
        return RestfulResponse.ok(res);
    }

    @RestfulApi("getAccessToken")
    public  RestfulResponse xx(){
        AccessToken accessToken = AccessToken.getDefaultInstance();
        return RestfulResponse.ok(accessToken);
    }

}
