package serialize;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class Serializer {

	public enum Type {
		CLASSIC, GZIP;
	}

	public static <T> void save(Type type, T object, String path) throws IOException {
		try (ObjectOutputStream oos = (type == Type.GZIP)
				? new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(path)))
				: new ObjectOutputStream(new FileOutputStream(path));) {
			oos.writeObject(object);
			oos.flush();
		}
	}

	public static <T> T load(Type type, Class<T> c, String path) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = (type == Type.GZIP)
				? new ObjectInputStream(new GZIPInputStream(new FileInputStream(path)))
				: new ObjectInputStream(new FileInputStream(path));) {
			return c.cast(ois.readObject());
		}
	}
}