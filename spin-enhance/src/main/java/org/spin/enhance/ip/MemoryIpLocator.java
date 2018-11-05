package org.spin.enhance.ip;

import org.spin.core.function.serializable.ExceptionalSupplier;
import org.spin.core.throwable.SimplifiedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * 内存中的IP搜索，线程安全
 */
public class MemoryIpLocator {

    /**
     * db config
     */
    private DbConfig dbConfig;

    /**
     * for memory mode
     * the original db binary string
     */
    private byte[] dbBinStr;


    public MemoryIpLocator(DbConfig dbConfig, String dbFile) {
        init(dbConfig, dbFile);
    }

    public MemoryIpLocator(DbConfig dbConfig, ExceptionalSupplier<InputStream, IOException> dbFile) {
        this.dbConfig = dbConfig;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); InputStream is = dbFile.get()) {
            byte[] tmp = new byte[4096];
            int len = 0;
            do {
                os.write(tmp, 0, len);
                len = is.read(tmp);
            }
            while (len != -1);

            dbBinStr = os.toByteArray();
        } catch (IOException e) {
            throw new SimplifiedException("读取数据库文件失败", e);
        }
    }

    public MemoryIpLocator() {
        this(new DbConfig(), () -> MemoryIpLocator.class.getClassLoader().getResourceAsStream("ipdb/ip.db"));
    }

    private void init(DbConfig dbConfig, String dbFile) {
        this.dbConfig = dbConfig;
        try (RandomAccessFile raf = new RandomAccessFile(dbFile, "r")) {
            dbBinStr = new byte[(int) raf.length()];
            raf.seek(0L);
            raf.readFully(dbBinStr, 0, dbBinStr.length);
        } catch (IOException e) {
            throw new SimplifiedException("读取数据库文件失败", e);
        }
    }

    public DataBlock search(long ip) {
        int blen = IndexBlock.getIndexBlockLength();

        long firstIndexPtr = Util.getIntLong(dbBinStr, 0);
        long lastIndexPtr = Util.getIntLong(dbBinStr, 4);

        //search the index blocks to define the data
        int l = 0, h = (int) ((lastIndexPtr - firstIndexPtr) / blen) + 1;
        long sip, eip, dataptr = 0;
        while (l <= h) {
            int m = (l + h) >> 1;
            int p = (int) (firstIndexPtr + m * blen);

            sip = Util.getIntLong(dbBinStr, p);
            if (ip < sip) {
                h = m - 1;
            } else {
                eip = Util.getIntLong(dbBinStr, p + 4);
                if (ip > eip) {
                    l = m + 1;
                } else {
                    dataptr = Util.getIntLong(dbBinStr, p + 8);
                    break;
                }
            }
        }

        //not matched
        if (dataptr == 0) return null;

        //get the data
        int dataLen = (int) ((dataptr >> 24) & 0xFF);
        int dataPtr = (int) ((dataptr & 0x00FFFFFF));
        int city_id = (int) Util.getIntLong(dbBinStr, dataPtr);
        String region = new String(dbBinStr, dataPtr + 4, dataLen - 4, StandardCharsets.UTF_8);

        return new DataBlock(city_id, region, dataPtr);
    }

    public DataBlock search(String ip) {
        return search(Util.ip2long(ip));
    }

    public DataBlock getByIndexPtr(int ptr) {
        byte[] buffer = new byte[12];
        System.arraycopy(dbBinStr, ptr, buffer, 0, 12);
        //long startIp = Util.getIntLong(buffer, 0);
        //long endIp = Util.getIntLong(buffer, 4);
        long extra = Util.getIntLong(buffer, 8);

        int dataLen = (int) ((extra >> 24) & 0xFF);
        int dataPtr = (int) ((extra & 0x00FFFFFF));

        byte[] data = new byte[dataLen];
        System.arraycopy(dbBinStr, dataPtr, data, 0, dataLen);

        int city_id = (int) Util.getIntLong(data, 0);
        String region = new String(data, 4, data.length - 4, StandardCharsets.UTF_8);

        return new DataBlock(city_id, region, dataPtr);
    }


    /**
     * get the db config
     *
     * @return DbConfig
     */
    public DbConfig getDbConfig() {
        return dbConfig;
    }

    public void close() {
        dbBinStr = null;
    }
}
