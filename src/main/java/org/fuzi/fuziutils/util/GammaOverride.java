package org.fuzi.fuziutils.util;

import net.minecraft.client.OptionInstance;
import org.fuzi.fuziutils.FuziUtils;

import java.lang.reflect.Field;

public final class GammaOverride {

    private static Field valueField;
    private static boolean reflectionFailed = false;

    private GammaOverride() {}

    public static void setRaw(OptionInstance<Double> option, double value) {
        if (!reflectionFailed) {
            try {
                if (valueField == null) {
                    valueField = OptionInstance.class.getDeclaredField("value");
                    valueField.setAccessible(true);
                }
                valueField.set(option, value);
                return;
            } catch (ReflectiveOperationException e) {
                reflectionFailed = true;
                FuziUtils.LOGGER.warn("[FuziUtils] Couldn't set gamma directly via reflection, " +
                        "falling back to the clamped vanilla setter (no true fullbright)", e);
            }
        }
        option.set(Math.max(0.0, Math.min(1.0, value)));
    }
}
