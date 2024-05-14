package net.mca.mixin.client;

import net.mca.MCA;
import net.mca.entity.CommonSpeechManager;
import net.mca.entity.ai.DialogueType;
import net.mca.util.localization.PooledTranslationStorage;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.util.Language;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = TranslationStorage.class, priority = 990)
abstract class MixinTranslationStorage extends Language {
    @Shadow
    private @Final Map<String, String> translations;

    @Shadow
    public abstract String get(String key, String fallback);

    @Unique
    private PooledTranslationStorage mca$pool;

    @Unique
    private PooledTranslationStorage mca$getPool() {
        if (mca$pool == null) {
            mca$pool = new PooledTranslationStorage(translations);
            MCA.translations = translations;
        }
        return mca$pool;
    }

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void mca$onGet(String key, String fallback, CallbackInfoReturnable<String> info) {
        String modifiedKey = DialogueType.applyFallback(key);

        Pair<String, String> unpooled = mca$getPool().get(modifiedKey);
        if (unpooled != null) {
            CommonSpeechManager.INSTANCE.lastResolvedKey = unpooled.getLeft();
            info.setReturnValue(get(unpooled.getLeft(), fallback));
        } else {
            CommonSpeechManager.INSTANCE.lastResolvedKey = null;
            if (!key.equals(modifiedKey)) {
                info.setReturnValue(get(modifiedKey, fallback));
            }
        }
    }

    @Inject(method = "hasTranslation(Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true)
    public void mca$onHasTranslation(String key, CallbackInfoReturnable<Boolean> info) {
        if (mca$getPool().contains(key)) {
            info.setReturnValue(true);
        }
    }
}
