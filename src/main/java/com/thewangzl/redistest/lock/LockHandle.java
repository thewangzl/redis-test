package com.thewangzl.redistest.lock;

import com.thewangzl.redistest.util.RandomUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class LockHandle {

    @Autowired
    private RedisLockHelper lockHelper;

    /**
     * 禁止重复提交
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.thewangzl.redistest.lock.RepeatSubmitLock)")
    public Object repeatSubmitLock(ProceedingJoinPoint pjp)throws Throwable{
        String value = RandomUtil.randomString();
        String key = getRepeatSubmitLockKey(pjp);
        try{
            if(lockHelper.lock(key,value)) {
                return pjp.proceed();
            }else{
                throw new RuntimeException("重复提交");
            }
        }finally {
            lockHelper.unlock(key, value, 10000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.thewangzl.redistest.lock.SyncLock)")
    public Object syncLock(ProceedingJoinPoint pjp)throws Throwable{
        String value = RandomUtil.randomString();
        String key = getSyncLockKey(pjp);
        if(StringUtils.isEmpty(key)){
            throw new RuntimeException(((MethodSignature)pjp.getSignature()).getMethod().getName()+" @SyncLock key is null");
        }
        try{
            lockHelper.tryLock(key,value);
            return pjp.proceed();
        }finally {
            lockHelper.unlock(key, value);
        }
    }

    private String getSyncLockKey(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature)pjp.getSignature();
        SyncLock lock = signature.getMethod().getAnnotation(SyncLock.class);
        StringBuilder key = new StringBuilder(lock.key());
        key.append(getParameterKey(signature, pjp.getArgs()));
        return key.toString();
    }

    private StringBuilder getParameterKey(MethodSignature signature,Object[] args) {
        StringBuilder key = new StringBuilder("(");
        Parameter[] parameters = signature.getMethod().getParameters();
        Annotation[][] annotations = signature.getMethod().getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            key.append(parameter.getType().getSimpleName()).append("|");
            LockParam param = parameter.getAnnotation(LockParam.class);
            if (param != null) {
                key.append(args[i]).append(",");
            } else {
                if (args[i] != null) {
                    Field[] fields = args[i].getClass().getDeclaredFields();
                    for (Field field : fields) {
                        param = field.getAnnotation(LockParam.class);
                        if (param == null) {
                            continue;
                        }
                        field.setAccessible(true);
                        key.append(ReflectionUtils.getField(field, args[i])).append(",");
                    }
                }
            }
        }
        if (key.charAt(key.length() - 1) == ',') {
            key.deleteCharAt(key.length() - 1);
        }
        return key.append(")");
    }

    /**
     * 重复提交，针对一个方法，把方法签名和参数共同作为key
     * @param pjp
     * @return
     */
    private String getRepeatSubmitLockKey(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature)pjp.getSignature();
        StringBuilder key = new StringBuilder(signature.getMethod().getDeclaringClass()
                .getSimpleName()).append(".").append(signature.getMethod().getName());
        key.append(getParameterKey(signature, pjp.getArgs()));
        return key.toString();
    }
}
