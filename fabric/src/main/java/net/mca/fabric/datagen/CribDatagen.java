package net.mca.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class CribDatagen implements DataGeneratorEntrypoint
{
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator)
	{
		FabricDataGenerator.Pack pack = generator.createPack();
		
		pack.addProvider(CribRecipeProvider::new);
		pack.addProvider(CribLanguageProvider::new);
		pack.addProvider(CribItemModelProvider::new);
	}
}
