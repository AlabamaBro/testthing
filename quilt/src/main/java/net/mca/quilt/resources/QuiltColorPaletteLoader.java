package net.mca.quilt.resources;

import net.mca.client.resources.ColorPaletteLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;

public class QuiltColorPaletteLoader extends ColorPaletteLoader implements IdentifiableResourceReloader {
    @Override
    public @NotNull Identifier getQuiltId() {
        return ID;
    }
}
