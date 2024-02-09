package net.mca.fabric.datagen;

import java.util.Optional;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.mca.MCA;
import net.mca.entity.CribWoodType;
import net.mca.item.CribItem;
import net.mca.item.ItemsMCA;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class CribItemModelProvider extends FabricModelProvider {

	public CribItemModelProvider(FabricDataOutput output) { super(output); }

	@Override
	public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {}

	@Override
	public void generateItemModels(ItemModelGenerator itemModelGenerator)
	{
		for(CribWoodType wood : CribWoodType.values())
		{
			for(DyeColor color : DyeColor.values())
			{
				Item item = ItemsMCA.CRIBS.stream().filter(c ->
				{
					CribItem crib = (CribItem) c.get();
					return crib.getColor() == color && crib.getWood() == wood;
				}).findFirst().get().get();
				
				Model cribModel = new Model(Optional.of(new Identifier("minecraft", "item/generated")), Optional.empty(), TextureKey.LAYER0, TextureKey.LAYER1);
				
				cribModel.upload(ModelIds.getItemModelId(item), TextureMap.layered(MCA.locate("item/crib/beds/" + color.getName()),
					MCA.locate("item/crib/frames/" + wood.toString().toLowerCase())), itemModelGenerator.writer);
			}
		}
	}
}
