package com.bytedice.bde_particles.items

import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import java.awt.Color


object ParticleEmitterTool {
  fun makeData(itemId: Item, name: String, emitterId: String): ItemStack {
    val i = ItemStack(itemId)

    val nbt = NbtCompound()
    nbt.putByte("BPS_particleTool", 1)
    nbt.putString("BPS_emitterID", emitterId)

    val component: NbtComponent = NbtComponent.of(nbt)

    val displayName = Text.literal(name)
      .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(0, 200, 0).rgb)).withItalic(true))

    val loreLines = Text.literal("BPS - Bound to ID \"$emitterId\".")
      .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(0, 200, 0).rgb)).withItalic(true))

    val lore = LoreComponent(listOf(loreLines))

    i.set(DataComponentTypes.ITEM_NAME, displayName)
    i.set(DataComponentTypes.CUSTOM_DATA, component)
    i.set(DataComponentTypes.LORE, lore)

    return i
  }


  fun getToolDetails(item: ItemStack): Pair<Boolean, String> {
    val customData = item.get(DataComponentTypes.CUSTOM_DATA)
    val isParticleEmitterTool = customData?.nbt?.getByte("BPS_particleTool") == 1.toByte()
    val emitterId = customData?.nbt?.getString("BPS_emitterID")

    return if (emitterId == null) {
      Pair(isParticleEmitterTool, "")
    } else {
      Pair(isParticleEmitterTool, emitterId)
    }
  }
}