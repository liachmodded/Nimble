package snownee.nimble;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class NimbleConfig
{
    static final ForgeConfigSpec spec;

    public static boolean enable = true;
    public static boolean nimbleMounting = true;
    public static boolean nimbleElytra = true;
    public static boolean elytraRollScreen = true;
    public static int elytraTickDelay = 10;
    public static boolean frontKeyToggleMode = false;

    public static BooleanValue enableV;
    public static BooleanValue nimbleMountingV;
    public static BooleanValue nimbleElytraV;
    public static BooleanValue elytraRollScreenV;
    public static IntValue elytraTickDelayV;
    public static BooleanValue frontKeyToggleModeV;

    static
    {
        final Pair<NimbleConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(NimbleConfig::new);
        spec = specPair.getRight();
    }

    private NimbleConfig(ForgeConfigSpec.Builder builder)
    {
        enableV = builder.define("enable", enable);
        nimbleMountingV = builder.define("nimbleMounting", nimbleMounting);
        nimbleElytraV = builder.define("nimbleElytra", nimbleElytra);
        elytraRollScreenV = builder.define("elytraRollScreen", elytraRollScreen);
        elytraTickDelayV = builder.defineInRange("elytraTickDelay", elytraTickDelay, 0, 10000);
        frontKeyToggleModeV = builder.define("frontKeyToggleMode", frontKeyToggleMode);
    }

    @SubscribeEvent
    public static void onConfigReload(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(Nimble.MODID))
        {
            refresh();
        }
    }

    public static void refresh()
    {
        enable = enableV.get();
        nimbleMounting = nimbleMountingV.get();
        nimbleElytra = nimbleElytraV.get();
        elytraRollScreen = elytraRollScreenV.get();
        elytraTickDelay = elytraTickDelayV.get();
        frontKeyToggleMode = frontKeyToggleModeV.get();
    }
}
