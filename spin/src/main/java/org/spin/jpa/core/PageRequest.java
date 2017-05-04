package org.spin.jpa.core;

/**
 * <p>Created by xuweinan on 2017/5/4.</p>
 *
 * @author xuweinan
 */
public class PageRequest {

    private final int page;
    private final int size;

    /**
     * Creates a new {@link PageRequest}. Pages are zero indexed, thus providing 0 for {@code page} will return the first
     * page.
     *
     * @param page zero-based page index.
     * @param size the size of the page to be returned.
     */
    public PageRequest(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero!");
        }

        if (size < 1) {
            throw new IllegalArgumentException("Page size must not be less than one!");
        }

        this.page = page;
        this.size = size;
    }

    public int getOffset() {
        return page * size;
    }

    public int getPageSize() {
        return size;
    }
}
