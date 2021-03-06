package org.cyclops.everlastingabilities.ability;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.everlastingabilities.GeneralConfig;
import org.cyclops.everlastingabilities.api.Ability;
import org.cyclops.everlastingabilities.api.IAbilityType;
import org.cyclops.everlastingabilities.api.IAbilityTypeRegistry;
import org.cyclops.everlastingabilities.api.capability.IAbilityStore;
import org.cyclops.everlastingabilities.capability.MutableAbilityStoreConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default ability type registry.
 * @author rubensworks
 */
public class AbilityTypeRegistry implements IAbilityTypeRegistry {

    private static final AbilityTypeRegistry INSTANCE = new AbilityTypeRegistry();

    private final Map<String, IAbilityType> abilities = Maps.newHashMap();
    private final Map<EnumRarity, List<IAbilityType>> rarityAbilities = Maps.newIdentityHashMap(); // An EnumMap
    // would be better, but unfortunately doesn't work well with other mods that register new rarity types.

    private AbilityTypeRegistry() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static AbilityTypeRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public <A extends IAbilityType> A register(A abilityType) {
        abilities.put(abilityType.getTranslationKey(), abilityType);
        rarityAbilities.computeIfAbsent(abilityType.getRarity(), (rarity) -> Lists.newArrayList()).add(abilityType);
        return abilityType;
    }

    @Override
    public IAbilityType getAbilityType(String unlocalizedName) {
        return abilities.get(unlocalizedName);
    }

    @Override
    public Collection<IAbilityType> getAbilityTypes() {
        return Collections.unmodifiableCollection(abilities.values());
    }

    @Override
    public List<IAbilityType> getAbilityTypes(EnumRarity rarity) {
        return rarityAbilities.getOrDefault(rarity, Collections.emptyList());
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            IAbilityStore abilityStore = player.getCapability(MutableAbilityStoreConfig.CAPABILITY, null);
            for (Ability ability : abilityStore.getAbilities()) {
                if (event.getEntity().world.getTotalWorldTime() % 20 == 0 && GeneralConfig.exhaustionPerAbilityTick > 0) {
                    player.addExhaustion((float) GeneralConfig.exhaustionPerAbilityTick);
                }
                ability.getAbilityType().onTick(player, ability.getLevel());
            }
        }
    }
}
