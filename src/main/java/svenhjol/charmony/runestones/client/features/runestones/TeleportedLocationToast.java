package svenhjol.charmony.runestones.client.features.runestones;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import svenhjol.charmony.api.RunestoneLocation;
import svenhjol.charmony.core.client.BaseToast;
import svenhjol.charmony.runestones.common.features.runestones.RunestoneHelper;

public class TeleportedLocationToast extends BaseToast {
    public static final Component TITLE = 
        Component.translatable("toast.charmony-runestones.teleported_location.title");
    
    public final Component description;
    
    public TeleportedLocationToast(RunestoneLocation location) {
        description = Component.translatable(
            "toast.charmony-runestones.teleported_location.description",
            Component.translatable(RunestoneHelper.localeKey(location)));
    }
    
    @Override
    protected Component title() {
        return TITLE;
    }

    @Override
    protected Component description() {
        return description;
    }

    @Override
    protected ItemStack icon() {
        return new ItemStack(Runestones.feature().common.get().registers.stoneBlock.get());
    }

    @Override
    protected long duration() {
        return 10000L; // A bit longer to give the player a chance to read the location name.
    }

    @Override
    protected int color() {
        return 0x909090;
    }
}
