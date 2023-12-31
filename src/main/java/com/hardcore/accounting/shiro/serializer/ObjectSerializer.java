package com.hardcore.accounting.shiro.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectSerializer implements RedisSerializer<Object> {
    public static final int BYTE_ARRAY_OUTPUT_STREAM_SIZE = 128;

    @Override
    public byte[] serialize(Object o) throws SerializationException {
        byte[] result = new byte[0];
        if (o == null) {
            return result;
        }
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(BYTE_ARRAY_OUTPUT_STREAM_SIZE);
        if (!(o instanceof Serializable)) {
            throw new SerializationException("Object is not serializable" + o.getClass());
        }
        try {

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);
            objectOutputStream.writeObject(o);
            objectOutputStream.flush();
            result = byteStream.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("serialize error" + o, e);
        }

        return result;
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        Object result = null;
        if (bytes == null || bytes.length == 0) {
            return result;
        }
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new MultiClassLoaderObjectInputStream(byteStream);
            result = objectInputStream.readObject();
        } catch (Exception e) {
            throw new SerializationException("deserialize error", e);
        }
        return result;
    }
}
