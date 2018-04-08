package org.spin.core.util;

import org.spin.core.function.Handler;

import java.util.function.Supplier;

/**
 * Boolean流式扩展
 * <p>Created by xuweinan on 2017/9/3.</p>
 *
 * @author xuweinan
 */
public interface BooleanExt {

    /**
     * 从一个Bool值创建具有返回值的操作链
     *
     * @param value bool值
     * @return Boolean操作链
     */
    static ExtAny ofAny(boolean value) {
        ExtAny instance = new ExtAny();
        instance.value = value;
        return instance;
    }

    /**
     * 从一个Bool值创建没有返回值的操作链
     *
     * @param value bool值
     * @return Boolean操作链
     */
    static ExtNothing of(boolean value) {
        ExtNothing instance = new ExtNothing();
        instance.value = value;
        return instance;
    }


    /**
     * 带有返回值的bool处理逻辑
     */
    class ExtAny {
        private boolean value;

        /**
         * 当bool值为true时的操作
         *
         * @param body 逻辑
         * @param <T>  结果类型
         * @return 执行结果
         */
        public <T> NoMoreThen<T> yes(Supplier<T> body) {
            T result = null;
            if (value) {
                result = body.get();
            }
            return new NoMoreThen<>(result, value);
        }

        /**
         * 当bool值为false时的操作
         *
         * @param body 逻辑
         * @param <T>  结果类型
         * @return 执行结果
         */
        public <T> YesMoreThen<T> no(Supplier<T> body) {
            T result = null;
            if (!value) {
                result = body.get();
            }
            return new YesMoreThen<>(result, value);
        }
    }

    /**
     * 没有返回值的bool处理逻辑
     */
    class ExtNothing {
        private boolean value;

        /**
         * 当bool值为true时的操作
         *
         * @param body 逻辑
         * @return otherwise操作
         */
        public NoThen yes(Handler body) {
            if (value) {
                body.handle();
            }
            return new NoThen(value);
        }

        /**
         * 当bool值为false时的操作
         *
         * @param body 逻辑
         * @return otherwise操作
         */
        public YesThen no(Handler body) {
            if (!value) {
                body.handle();
            }
            return new YesThen(value);
        }
    }

    interface OtherwiseMore<T> {
        T otherwise(Supplier<T> body);

        /**
         * 获取结果
         *
         * @return 结果
         */
        T get();
    }

    interface Otherwise {
        void otherwise(Handler body);
    }

    class YesMoreThen<E> implements OtherwiseMore<E> {
        private boolean value;
        private E result;

        public YesMoreThen(E result, boolean value) {
            this.result = result;
            this.value = value;
        }

        @Override
        public E otherwise(Supplier<E> body) {
            if (value) {
                result = body.get();
            }
            return result;
        }

        @Override
        public E get() {
            return result;
        }
    }

    class YesThen implements Otherwise {
        private boolean value;

        public YesThen(boolean value) {
            this.value = value;
        }

        @Override
        public void otherwise(Handler body) {
            if (value) {
                body.handle();
            }
        }
    }

    class NoMoreThen<E> implements OtherwiseMore<E> {
        private boolean value;
        private E result;

        public NoMoreThen(E result, boolean value) {
            this.result = result;
            this.value = value;
        }

        @Override
        public E otherwise(Supplier<E> body) {
            if (!value) {
                result = body.get();
            }
            return result;
        }

        @Override
        public E get() {
            return result;
        }
    }

    class NoThen implements Otherwise {
        private boolean value;

        public NoThen(boolean value) {
            this.value = value;
        }

        @Override
        public void otherwise(Handler body) {
            if (!value) {
                body.handle();
            }
        }
    }
}
