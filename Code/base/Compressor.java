package base;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface Compressor
{
	abstract public void Compress(String[] input_names, String[] output_names) throws FileNotFoundException, IOException;
	abstract public void Decompress(String[] input_names, String[] output_names) throws ClassNotFoundException, IOException;

	abstract public byte[] CompressWithArray(String[] input_names, String[] output_names);
	abstract public byte[] DecompressWithArray(String[] input_names, String[] output_names);
}
