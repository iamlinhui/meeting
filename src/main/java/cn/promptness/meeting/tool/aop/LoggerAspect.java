package cn.promptness.meeting.tool.aop;

import cn.promptness.httpclient.HttpResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Aspect
public class LoggerAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggerAspect.class);

    @Pointcut(value = "execution(* cn.promptness.httpclient.HttpClientUtil.doGet(..))&& args(url,cookies)", argNames = "url,cookies")
    private void cutOne(String url, List<Cookie> cookies) {
    }

    @Pointcut(value = "execution(* cn.promptness.httpclient.HttpClientUtil.doGet(..))&& args(url,params,cookies)", argNames = "url,params,cookies")
    private void cutTwo(String url, Map<String, String> params, List<Cookie> cookies) {

    }

    @Around(value = "cutOne(url,cookies)", argNames = "joinPoint,url,cookies")
    public Object around(ProceedingJoinPoint joinPoint, String url, List<Cookie> cookies) throws Throwable {
        return around(joinPoint, url, new HashMap<>(0), cookies);
    }

    @Around(value = "cutTwo(url,params,cookies)", argNames = "joinPoint,url,params,cookies")
    public Object around(ProceedingJoinPoint joinPoint, String url, Map<String, String> params, List<Cookie> cookies) throws Throwable {
        Object result = joinPoint.proceed();
        logger(url, params, cookies, result);
        return result;
    }

    private void logger(String url, Map<String, String> params, List<Cookie> cookies, Object result) {
        HashMap<String, String> cookieMap = new HashMap<>(16);
        for (Cookie cookie : cookies) {
            cookieMap.put(cookie.getName(), cookie.getValue());
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String cookie = gson.toJson(cookieMap);
        String param = gson.toJson(params);
        if (result instanceof HttpResult) {
            HashMap<String, String> headerMap = new HashMap<>(16);
            HttpResult httpResult = (HttpResult) result;
            List<Header> headerList = httpResult.getHeaderList("Set-Cookie");
            for (Header header : headerList) {
                String value = header.getValue();
                String cookieString = value.split(";")[0];
                headerMap.put(cookieString.split("=")[0], cookieString.split("=")[1]);
            }
            String setCookie = gson.toJson(headerMap);
            log.info("请求路径:{},入参:{},Cookie:{},出参:{},Set-Cookie:{}", url, param, cookie, httpResult.getMessage(), setCookie);
        }
    }

}
