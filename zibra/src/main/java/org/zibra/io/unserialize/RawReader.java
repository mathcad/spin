package org.zibra.io.unserialize;

import org.zibra.common.ZibraException;

import static org.zibra.io.Tags.TagBytes;
import static org.zibra.io.Tags.TagClass;
import static org.zibra.io.Tags.TagClosebrace;
import static org.zibra.io.Tags.TagDate;
import static org.zibra.io.Tags.TagDouble;
import static org.zibra.io.Tags.TagEmpty;
import static org.zibra.io.Tags.TagError;
import static org.zibra.io.Tags.TagFalse;
import static org.zibra.io.Tags.TagGuid;
import static org.zibra.io.Tags.TagInfinity;
import static org.zibra.io.Tags.TagInteger;
import static org.zibra.io.Tags.TagList;
import static org.zibra.io.Tags.TagLong;
import static org.zibra.io.Tags.TagMap;
import static org.zibra.io.Tags.TagNaN;
import static org.zibra.io.Tags.TagNull;
import static org.zibra.io.Tags.TagObject;
import static org.zibra.io.Tags.TagOpenbrace;
import static org.zibra.io.Tags.TagQuote;
import static org.zibra.io.Tags.TagRef;
import static org.zibra.io.Tags.TagSemicolon;
import static org.zibra.io.Tags.TagString;
import static org.zibra.io.Tags.TagTime;
import static org.zibra.io.Tags.TagTrue;
import static org.zibra.io.Tags.TagUTC;
import static org.zibra.io.Tags.TagUTF8Char;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class RawReader {

    private static void readBytesRaw(InputStream stream, OutputStream ostream) throws IOException {
        int len = 0;
        int tag = '0';
        do {
            len = len * 10 + (tag - '0');
            tag = stream.read();
            ostream.write(tag);
        } while (tag != TagQuote);
        int off = 0;
        byte[] b = new byte[len];
        while (off < len) {
            off += stream.read(b, off, len - off);
        }
        ostream.write(b);
        ostream.write(stream.read());
    }

    private static void readGuidRaw(InputStream stream, OutputStream ostream) throws IOException {
        int len = 38;
        int off = 0;
        byte[] b = new byte[len];
        while (off < len) {
            off += stream.read(b, off, len - off);
        }
        ostream.write(b);
    }

    private static void readUTF8CharRaw(InputStream stream, OutputStream ostream) throws IOException {
        int tag = stream.read();
        switch (tag >>> 4) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                ostream.write(tag);
                break;
            case 12:
            case 13:
                ostream.write(tag);
                ostream.write(stream.read());
                break;
            case 14:
                ostream.write(tag);
                ostream.write(stream.read());
                ostream.write(stream.read());
                break;
            default:
                throw ValueReader.badEncoding(tag);
        }
    }

    private static void readComplexRaw(InputStream stream, OutputStream ostream) throws IOException {
        int tag;
        do {
            tag = stream.read();
            ostream.write(tag);
        } while (tag != TagOpenbrace);
        while ((tag = stream.read()) != TagClosebrace) {
            readRaw(stream, ostream, tag);
        }
        ostream.write(tag);
    }

    private static void readNumberRaw(InputStream stream, OutputStream ostream) throws IOException {
        int tag;
        do {
            tag = stream.read();
            ostream.write(tag);
        } while (tag != TagSemicolon);
    }

    private static void readDateTimeRaw(InputStream stream, OutputStream ostream) throws IOException {
        int tag;
        do {
            tag = stream.read();
            ostream.write(tag);
        } while (tag != TagSemicolon && tag != TagUTC);
    }

    private static void readStringRaw(InputStream stream, OutputStream ostream) throws IOException {
        int count = 0;
        int tag = '0';
        do {
            count = count * 10 + (tag - '0');
            tag = stream.read();
            ostream.write(tag);
        } while (tag != TagQuote);
        for (int i = 0; i < count; ++i) {
            tag = stream.read();
            switch (tag >>> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    ostream.write(tag);
                    break;
                case 12:
                case 13:
                    ostream.write(tag);
                    ostream.write(stream.read());
                    break;
                case 14:
                    ostream.write(tag);
                    ostream.write(stream.read());
                    ostream.write(stream.read());
                    break;
                case 15:
                    if ((tag & 0x0f) <= 4) {
                        ostream.write(tag);
                        ostream.write(stream.read());
                        ostream.write(stream.read());
                        ostream.write(stream.read());
                        ++i;
                        break;
                    }
                default:
                    throw ValueReader.badEncoding(tag);
            }
        }
        ostream.write(stream.read());
    }

    private static void readRaw(InputStream stream, OutputStream ostream) throws IOException {
        readRaw(stream, ostream, stream.read());
    }

    static void readRaw(InputStream stream, OutputStream ostream, int tag) throws IOException {
        ostream.write(tag);
        switch (tag) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case TagNull:
            case TagEmpty:
            case TagTrue:
            case TagFalse:
            case TagNaN:
                break;
            case TagInfinity:
                ostream.write(stream.read());
                break;
            case TagInteger:
            case TagLong:
            case TagDouble:
            case TagRef:
                readNumberRaw(stream, ostream);
                break;
            case TagDate:
            case TagTime:
                readDateTimeRaw(stream, ostream);
                break;
            case TagUTF8Char:
                readUTF8CharRaw(stream, ostream);
                break;
            case TagBytes:
                readBytesRaw(stream, ostream);
                break;
            case TagString:
                readStringRaw(stream, ostream);
                break;
            case TagGuid:
                readGuidRaw(stream, ostream);
                break;
            case TagList:
            case TagMap:
            case TagObject:
                readComplexRaw(stream, ostream);
                break;
            case TagClass:
                readComplexRaw(stream, ostream);
                readRaw(stream, ostream);
                break;
            case TagError:
                readRaw(stream, ostream);
                break;
            case -1:
                throw new ZibraException("No byte found in stream");
            default:
                throw new ZibraException("Unexpected serialize tag '" + (char) tag + "' in stream");
        }
    }
}
