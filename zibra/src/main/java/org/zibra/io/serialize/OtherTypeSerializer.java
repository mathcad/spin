package org.zibra.io.serialize;

import org.zibra.io.ZibraMode;
import static org.zibra.io.Tags.TagClass;
import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagObject;
import static org.zibra.io.Tags.TagOpenbrace;
import static org.zibra.io.Tags.TagString;
import org.zibra.io.access.Accessors;
import org.zibra.io.access.MemberAccessor;
import org.zibra.util.ClassUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OtherTypeSerializer extends ReferenceSerializer {

    public final static OtherTypeSerializer instance = new OtherTypeSerializer();

    private final static EnumMap<ZibraMode, ConcurrentHashMap<Class<?>, SerializeCache>> memberCache = new EnumMap<>(ZibraMode.class);

    static {
        memberCache.put(ZibraMode.FieldMode, new ConcurrentHashMap<>());
        memberCache.put(ZibraMode.PropertyMode, new ConcurrentHashMap<>());
        memberCache.put(ZibraMode.MemberMode, new ConcurrentHashMap<>());
    }

    final static class SerializeCache {
        byte[] data;
        int refcount;
    }

    private static void writeObject(Writer writer, Object object, Class<?> type) throws IOException {
        Map<String, MemberAccessor> members = Accessors.getMembers(type, writer.mode);
        for (Map.Entry<String, MemberAccessor> entry : members.entrySet()) {
            MemberAccessor member = entry.getValue();
            member.serialize(writer, object);
        }
    }

    private static int writeClass(Writer writer, Class<?> type) throws IOException {
        SerializeCache cache = memberCache.get(writer.mode).get(type);
        if (cache == null) {
            cache = new SerializeCache();
            ByteArrayOutputStream cachestream = new ByteArrayOutputStream();
            Map<String, MemberAccessor> members = Accessors.getMembers(type, writer.mode);
            int count = members.size();
            cachestream.write(TagClass);
            ValueWriter.write(cachestream, ClassUtil.getClassAlias(type));
            if (count > 0) {
                ValueWriter.writeInt(cachestream, count);
            }
            cachestream.write(TagOpenbrace);
            for (Map.Entry<String, MemberAccessor> member : members.entrySet()) {
                cachestream.write(TagString);
                ValueWriter.write(cachestream, member.getKey());
                ++cache.refcount;
            }
            cachestream.write(TagClosebrace);
            cache.data = cachestream.toByteArray();
            memberCache.get(writer.mode).put(type, cache);
        }
        writer.stream.write(cache.data);
        if (writer.refer != null) {
            writer.refer.addCount(cache.refcount);
        }
        int cr = writer.lastclassref++;
        writer.classref.put(type, cr);
        return cr;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public final void serialize(Writer writer, Object object) throws IOException {
        OutputStream stream = writer.stream;
        Class<?> type = object.getClass();
        Integer cr = writer.classref.get(type);
        if (cr == null) {
            cr = writeClass(writer, type);
        }
        super.serialize(writer, object);
        stream.write(TagObject);
        ValueWriter.writeInt(stream, cr);
        stream.write(TagOpenbrace);
        writeObject(writer, object, type);
        stream.write(TagClosebrace);
    }
}
