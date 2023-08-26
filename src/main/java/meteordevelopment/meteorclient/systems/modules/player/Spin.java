package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;


public class Spin extends Module {
    private final SettingGroup speed = settings.createGroup("Speed");
    private final Setting<Integer> Speed = speed.add(new IntSetting.Builder()
        .name("speed")
        .description("Speed of Rotation.")
        .defaultValue(20)
        .range(-1000, 1000)
        .sliderRange(-100, 100)
        .build()
    );
    public Spin() {
        super(Categories.Player, "im-only-human", "Im only human after all.");
    }

    @Override
    public void onActivate() {
        onTick(null);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        currentYaw += (Speed.get().floatValue() / 10f);

        if (currentYaw >= 360.0f) {
            currentYaw = 0.0f;
        }
            setYawAngle(currentYaw);
    }

    private void setYawAngle(float yawAngle) {
        mc.player.setYaw(yawAngle);
        mc.player.headYaw = yawAngle;
        mc.player.bodyYaw = yawAngle;
    }

    private float currentYaw = 0.0f;
}
