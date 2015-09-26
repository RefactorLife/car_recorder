package com.carrecorder.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
/**
 * to copy content from contest to target file
 * @author AL
 *
 */
public class CopyFileFromAssets {
	public static void copy(Context myContext, String ASSETS_NAME,
			String savePath, String saveName) {
		String filename = savePath + "/" + saveName;

		File dir = new File(savePath);
		// if dir is not exists , then create one
		if (!dir.exists())
			dir.mkdir();
		try {
			if (!(new File(filename)).exists()) {
				InputStream is = myContext.getResources().getAssets()
						.open(ASSETS_NAME);
				FileOutputStream fos = new FileOutputStream(filename);
				byte[] buffer = new byte[7168];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
