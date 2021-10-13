package org.spin.enhance.pinyin.multipinyin;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Trie {

    private final ConcurrentHashMap<String, Trie> values = new ConcurrentHashMap<>();//本节点包含的值

    private String pinyin;//本节点的拼音

    private Trie nextTire;//下一个节点,也就是匹配下一个字符

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public Trie getNextTire() {
        return nextTire;
    }

    public void setNextTire(Trie nextTire) {
        this.nextTire = nextTire;
    }

    /**
     * 加载拼音
     *
     * @param inStream 拼音文件输入流
     * @throws IOException io异常
     */
    public synchronized void load(InputStream inStream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream))) {
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                int idx = s.indexOf(' ');
                if (s.length() == 0 || idx < 0 || idx != s.lastIndexOf(' ')) {
                    continue;
                }
                Trie trie = new Trie();
                trie.pinyin = s.substring(idx + 1);
                put(s.substring(0, idx), trie);
            }
        }
    }

    /**
     * 加载多音字拼音词典
     *
     * @param inStream 拼音文件输入流
     * @throws IOException io异常
     */
    public synchronized void loadMultiPinyin(InputStream inStream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream))) {
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                int idx = s.indexOf(' ');
                if (s.length() == 0 || idx < 0 || idx != s.lastIndexOf(' ')) {
                    continue;
                }

                String key = s.substring(0, idx);//多于一个字的字符串
                String value = s.substring(idx + 1);//字符串的拼音
                char[] keys = key.toCharArray();

                Trie currentTrie = this;
                for (int i = 0; i < keys.length; i++) {
                    String hexString = Integer.toHexString(keys[i]).toUpperCase();

                    Trie trieParent = currentTrie.get(hexString);
                    if (trieParent == null) {//如果没有此值,直接put进去一个空对象
                        currentTrie.put(hexString, new Trie());
                        trieParent = currentTrie.get(hexString);
                    }
                    Trie trie = trieParent.getNextTire();//获取此对象的下一个

                    if (keys.length - 1 == i) {//最后一个字了,需要把拼音写进去
                        trieParent.pinyin = value;
                        break;//此行其实并没有意义
                    }

                    if (trie == null) {
                        if (keys.length - 1 != i) {
                            //不是最后一个字,写入这个字的nextTrie,并匹配下一个
                            Trie subTrie = new Trie();
                            trieParent.setNextTire(subTrie);
                            subTrie.put(Integer.toHexString(keys[i + 1]).toUpperCase(), new Trie());
                            currentTrie = subTrie;
                        }
                    } else {
                        currentTrie = trie;
                    }

                }
            }
        }
    }

    /**
     * 加载用户自定义的扩展词库
     *
     * @throws IOException io异常
     */
    public void loadMultiPinyinExtend() throws IOException {
        for (Map.Entry<String, Boolean> entry : MultiPinyinConfig.getMultiPinyinPath().entrySet()) {
            if (!entry.getValue()) {
                String path = entry.getKey();
                if (path != null && path.length() > 0) {
                    InputStream inputStream = null;
                    if (path.startsWith("classpath://")) {
                        inputStream = Trie.class.getResourceAsStream(path.substring(12));
                    } else {
                        if (path.startsWith("file://")) {
                            path = path.substring(7);
                        }
                        File userMultiPinyinFile = new File(path);
                        if (userMultiPinyinFile.exists()) {
                            inputStream = new FileInputStream(userMultiPinyinFile);
                        }
                    }
                    if (null != inputStream) {
                        try {
                            loadMultiPinyin(inputStream);
                        } finally {
                            try {
                                inputStream.close();
                            } catch (IOException ignore) {
                                // do nothing
                            }
                        }
                    }
                }
                entry.setValue(true);
            }
        }
    }

    public Trie get(String hexString) {
        return values.get(hexString);
    }

    public void put(String s, Trie trie) {
        values.put(s, trie);
    }
}
