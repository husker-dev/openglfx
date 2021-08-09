package com.husker.openglfx.utils;

import java.io.*;

public class LibUtils {

    public static void loadInternalLib(String name) {
        name = name + ".dll";
        try {
            InputStream in = LibUtils.class.getResourceAsStream("/" + name);
            File fileOut = new File(System.getProperty("java.io.tmpdir") + "/" + name);

            FileOutputStream out = new FileOutputStream(fileOut);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0)
                out.write(buffer, 0, length);

            in.close();
            out.close();
            System.load(fileOut.toString());
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to load library", e);
        }
    }
}
