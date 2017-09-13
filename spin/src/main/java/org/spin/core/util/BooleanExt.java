package org.spin.core.util;

import java.util.function.Supplier;

/**
 * Boolean流式扩展
 * <p>Created by xuweinan on 2017/9/3.</p>
 *
 * @author xuweinan
 */
public class BooleanExt<T> {

    private BooleanExt() {
    }

    public static <T> ExtAny of(boolean value, Class<T> rcls) {
        ExtAny instance = new ExtAny();
        instance.value = value;
        return instance;
    }

    public static ExtNothing of(boolean value) {
        ExtNothing instance = new ExtNothing();
        instance.value = value;
        return instance;
    }


    public static class ExtAny {
        private boolean value;

        public <T> NoMoreThen<T> yes(Supplier<T> body) {
            T result = null;
            if (value) {
                result = body.get();
            }
            return new NoMoreThen<>(result, value);
        }

        public <T> YesMoreThen<T> no(Supplier<T> body) {
            T result = null;
            if (!value) {
                result = body.get();
            }
            return new YesMoreThen<>(result, value);
        }
    }

    public static class ExtNothing {
        private boolean value;

        public NoThen yes(Handler body) {
            if (value) {
                body.handle();
            }
            return new NoThen(value);
        }

        public YesThen no(Handler body) {
            if (!value) {
                body.handle();
            }
            return new YesThen(value);
        }
    }

    @FunctionalInterface
    public interface Handler {
        void handle();
    }

    private interface OtherwiseMore<T> {
        T otherwise(Supplier<T> body);
    }

    private interface Otherwise {
        void otherwise(Handler body);
    }

    public static class YesMoreThen<E> implements OtherwiseMore<E> {
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
    }

    public static class YesThen implements Otherwise {
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

    public static class NoMoreThen<E> implements OtherwiseMore<E> {
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
    }

    public static class NoThen implements Otherwise {
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
