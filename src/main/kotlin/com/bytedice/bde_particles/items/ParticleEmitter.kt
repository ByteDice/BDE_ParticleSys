package com.bytedice.bde_particles.items

import com.bytedice.bde_particles.math.raycastFromPlayer
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import java.awt.Color


class ParticleEmitter {
  fun makeData(itemId: Item, name: String, particleId: String): ItemStack {
    val i = ItemStack(itemId)

    val nbt = NbtCompound()
    nbt.putByte("BPS_particleTool", 1)
    nbt.putString("BPS_particleId", particleId)

    val component: NbtComponent = NbtComponent.of(nbt)

    val displayName = Text.literal(name)
      .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(0, 125, 0).rgb)).withItalic(true))

    val loreLines = listOf(
      Text.literal("BPS").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(0, 125, 0).rgb)).withItalic(true)),
      Text.literal("Bound to id \"$particleId\"").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(0, 125, 0).rgb)).withItalic(true))
    )

    val lore = LoreComponent(loreLines)

    i.set(DataComponentTypes.ITEM_NAME, displayName)
    i.set(DataComponentTypes.CUSTOM_DATA, component)
    i.set(DataComponentTypes.LORE, lore)

    return i
  }

  fun onRightCLick(player: ServerPlayerEntity) {
    val hitResult = raycastFromPlayer(player, 4.5) ?: return

    // spawn particle at hitresult
  }
}