package snownee.nimble;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Nimble.MODID)
public class Nimble
{
    public static final String MODID = "nimble";
    public static final String NAME = "Nimble";

    // KEY_F4
    private static final KeyBinding kbFrontView = new KeyBinding(Nimble.MODID + ".keybind.frontView", 293, Nimble.MODID + ".gui.keygroup");
    private static boolean useFront = false;
    private static boolean flag = false;

    public Nimble()
    {
        if (EffectiveSide.get() == LogicalSide.CLIENT)
        {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, NimbleConfig.spec, MODID + ".toml");
            final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            MinecraftForge.EVENT_BUS.register(Nimble.class);
            modEventBus.addListener(this::preInit);
            modEventBus.addListener(this::loadComplete);
        }
    }

    public void preInit(FMLClientSetupEvent event)
    {
        ClientRegistry.registerKeyBinding(kbFrontView);
    }

    public void loadComplete(FMLLoadCompleteEvent event)
    {
        NimbleConfig.refresh();
    }

    static int actualCameraMode = 0;
    static float distance = 0;
    static boolean elytraFlying = false;

    @SubscribeEvent
    public static void cameraSetup(CameraSetup event)
    {
        if (!NimbleConfig.enable)
            return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.isGamePaused())
            return;
        if (mc.player == null)
            return;

        if (NimbleConfig.nimbleElytra || NimbleConfig.elytraRollScreen)
        {
            if (mc.player.isElytraFlying())
            {
                if (NimbleConfig.elytraRollScreen)
                {
                    Vec3d look = mc.player.getLookVec();
                    look = new Vec3d(look.x, 0, look.z);
                    Vec3d move = new Vec3d(mc.player.getMotion().x, 0, mc.player.getMotion().z).normalize();
                    event.setRoll((float) look.crossProduct(move).y * 10);
                }

                if (NimbleConfig.nimbleElytra && mc.player.getTicksElytraFlying() == NimbleConfig.elytraTickDelay)
                {
                    elytraFlying = true;
                    setCameraMode(1);
                    actualCameraMode = 1;
                }
            }
            else if (NimbleConfig.nimbleElytra && elytraFlying)
            {
                actualCameraMode = 0;
                elytraFlying = false;
            }
        }

        if (kbFrontView.isKeyDown())
        {
            return;
        }

        if (getCameraMode() == 1)
        {
            float ptick = mc.getRenderPartialTicks();
            float delta = 0.05F + (float) Math.sin(distance / 3 * Math.PI) * 0.15F * ptick;
            distance += actualCameraMode == 1 ? delta : -delta;
        }
        else
        {
            distance = 0;
            return;
        }
        if (distance < 0)
        {
            setCameraMode(0);
        }
        distance = Math.min(distance, 3);
        if (distance < 3)
        {
            GlStateManager.translatef(0, 0, 3 - distance);
            resetView();
        }
    }

    @SubscribeEvent
    public static void onFrame(TickEvent.RenderTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START)
            return;
        if (!NimbleConfig.enable)
            return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.isGamePaused())
            return;
        if (mc.player == null)
            return;

        if (!NimbleConfig.frontKeyToggleMode && kbFrontView.isKeyDown())
        {
            setCameraMode(2);
            return;
        }
        if (NimbleConfig.frontKeyToggleMode && kbFrontView.isPressed())
        {
            useFront = !useFront;
            if (useFront)
            {
                setCameraMode(2);
            }
        }
        if (getCameraMode() == 2 && !useFront)
        {
            setCameraMode(0);
        }

        if (getCameraMode() == 0)
        {
            actualCameraMode = 0;
            if (distance > 0)
            {
                setCameraMode(1);
            }
        }
        else if (distance == 0)
        {
            actualCameraMode = 1;
        }
    }

    @SubscribeEvent
    public static void mountEvent(EntityMountEvent event)
    {
        if (NimbleConfig.nimbleMounting)
        {
            Minecraft mc = Minecraft.getInstance();
            if (event.getEntity() == mc.player)
            {
                setCameraMode(event.isMounting() ? 1 : 0);
            }
        }
    }

    private static void setCameraMode(int mode)
    {
        Minecraft.getInstance().gameSettings.thirdPersonView = mode;
        resetView();
    }

    private static void resetView()
    {
        // horrible hack to let global render reset states
        flag = !flag;
        Minecraft.getInstance().player.rotationPitch += flag ? 0.000001 : -0.000001;
    }

    private static int getCameraMode()
    {
        return Minecraft.getInstance().gameSettings.thirdPersonView;
    }
}
