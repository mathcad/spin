package org.spin.core.inspection;

import org.spin.core.Assert;
import org.spin.core.util.JsonUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 描述方法调用参数信息
 * <p>Created by xuweinan on 2017/9/15.</p>
 *
 * @author xuweinan
 */
public class ArgumentsDescriptor {
    private final MethodDescriptor methodDescriptor;
    private final Object[] args;
    private final boolean[] needed;
    private int neededNullsCnt;
    private final List<Integer> neededNulls;
    private int nullCnt;
    private final int rank;

    /**
     * 构造方法
     *
     * @param descriptor  方法描述器
     * @param args        实参数组
     * @param checkNeeded 实参可空性判断器
     */
    public ArgumentsDescriptor(MethodDescriptor descriptor, Object[] args, Function<Parameter, Boolean> checkNeeded) {
        Parameter[] parameters = Assert.notNull(descriptor, "方法描述器不能为空").getMethod().getParameters();
        Assert.isTrue(parameters.length == args.length, "实参个数与方法形参声明必须一致");
        this.methodDescriptor = descriptor;
        this.args = args;
        neededNullsCnt = 0;
        nullCnt = 0;
        needed = new boolean[args.length];
        neededNulls = new ArrayList<>(args.length);
        boolean check = Objects.nonNull(checkNeeded);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (Objects.isNull(args[i])) {
                ++nullCnt;
            }
            needed[i] = check && checkNeeded.apply(parameter) || parameter.getType().isPrimitive();
            if (needed[i] && Objects.isNull(args[i])) {
                ++neededNullsCnt;
                neededNulls.add(i);
            }
        }
        rank = neededNullsCnt * 100 + nullCnt;
    }

    public MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    public Object invoke() throws InvocationTargetException, IllegalAccessException {
        Assert.isTrue(rank < 100, "索引为" + JsonUtils.toJson(neededNulls) + "的参数不能为空");
        return methodDescriptor.invoke(args);
    }

    /**
     * 获取实参数组
     *
     * @return 实参数组
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * 获取形参列表中的可空性数组
     * <p>下标i为true表示索引为i的形参不允许为空</p>
     *
     * @return 形参可空性数组
     */
    public boolean[] getNeeded() {
        return needed;
    }

    /**
     * 获取方法禁止为空的形参中，null实参的个数
     *
     * @return 违反非空约束的实参个数
     */
    public int getNeededNullsCnt() {
        return neededNullsCnt;
    }

    /**
     * 获取方法禁止为空的形参中，为null实参的位置索引列表
     *
     * @return 违反非空约束的实参索引列表
     */
    public List<Integer> getNeededNulls() {
        return neededNulls;
    }

    /**
     * 获取方法参数中为null实参的个数
     *
     * @return null实参个数
     */
    public int getNullCnt() {
        return nullCnt;
    }

    /**
     * 匹配度分值，越低匹配程度越高，为0表示完全匹配，超过100表示存在必需参数为空的情况
     * <p>必须参数为空会导致调用方法时的运行异常</p>
     *
     * @return 分值
     */
    public int getRank() {
        return rank;
    }
}
