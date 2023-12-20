package com.vyomlabs.util;

import java.text.DecimalFormat;

public class FileSizeCalculator {

	public static String getFileSize(long size) {
		DecimalFormat df = new DecimalFormat("0.00");
		float sizeKb = 1024.0f;
		float sizeMb = sizeKb * sizeKb;
		float sizeGb = sizeMb * sizeKb;
		float sizeTerra = sizeGb * sizeKb;

		if (size < sizeKb)
			return df.format(size) + " B";
		else if (size < sizeMb)
			return df.format(size / sizeKb) + " KB";
		else if (size < sizeGb)
			return df.format(size / sizeMb) + " MB";
		else if (size < sizeTerra)
			return df.format(size / sizeGb) + " GB";
		else
			return df.format(size);
	}
}
