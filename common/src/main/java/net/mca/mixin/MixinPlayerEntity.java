package net.mca.mixin;

import net.mca.item.BabyItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
abstract class MixinPlayerEntity extends LivingEntity {
    @Shadow
    public abstract PlayerInventory getInventory();

    private MixinPlayerEntity() {
        super(null, null);
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"))
    private void onDropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> info) {
        if (stack.getItem() instanceof BabyItem baby && !baby.onDropped(stack, (PlayerEntity) (Object) this)) {
            this.getInventory().insertStack(stack);
        }
    }
}
