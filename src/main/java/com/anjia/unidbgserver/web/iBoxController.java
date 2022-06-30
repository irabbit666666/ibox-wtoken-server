package com.anjia.unidbgserver.web;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.anjia.unidbgserver.service.iBoxServiceWorker;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * MVC控制类
 *
 * @author AnJia
 */
@RestController
@RequestMapping(path = "/api/ibox", produces = MediaType.APPLICATION_JSON_VALUE)
public class iBoxController {

    @Resource(name = "iBoxServiceWorker")
    private iBoxServiceWorker iBoxServiceWorker;

    /**
     * 获取 ibox 计算结果
     *
     * public byte[] ttEncrypt(@RequestParam(required = false) String key1, @RequestBody String body)
     * 这是接收一个url参数，名为key1,接收一个post或者put请求的body参数
     * key1是选填参数，不写也不报错，值为,body只有在请求方法是POST时才有，GET没有
     *
     * @return 结果
     */

    @SneakyThrows @RequestMapping(value = "/do-work", method = {RequestMethod.GET, RequestMethod.POST})
    public Object ibox(@RequestBody String body) {
//        public Object ibox(@RequestParam(required = false) String sign,@RequestBody String body) {
//        boolean encrypt = true;
//        if (encrypt & sign.equals(SecureUtil.md5(body+"!@#ATYRUTYHCVxcv..asdq#45312377766555"))) {
//            // AES加密 秘钥 1234567890666666
//            String key="1234567890666666";
//            AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
//            body = aes.decryptStr(body);
//        }
//        else{
//            return "sign error";
//        }
        String result= (String) iBoxServiceWorker.doWork(body).get();
        result=result.replace("\"","");
        return result;
    }
}
