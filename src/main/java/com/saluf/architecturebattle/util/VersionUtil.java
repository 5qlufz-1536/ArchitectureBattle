package com.saluf.architecturebattle.util;

import net.minecraft.SharedConstants;

public class VersionUtil {

    public static boolean isMinecraft1212() {
        return SharedConstants.getGameVersion().getName().startsWith("1.21.2");
    }

    public static boolean isMinecraft1213() {
        return SharedConstants.getGameVersion().getName().startsWith("1.21.3");
    }

    public static boolean isMinecraft1214() {
        return SharedConstants.getGameVersion().getName().startsWith("1.21.4");
    }

    public static boolean isMinecraft1215() {
        return SharedConstants.getGameVersion().getName().startsWith("1.21.5");
    }

    public static boolean isMinecraft1216() {
        return SharedConstants.getGameVersion().getName().startsWith("1.21.6");
    }
}
