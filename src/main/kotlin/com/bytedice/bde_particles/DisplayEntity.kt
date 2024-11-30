package com.bytedice.bde_particles

import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtFloat
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.world.ServerWorld

class DisplayEntity(private var properties: DisplayEntityProperties?) {
  private var hasSpawned = false
  private var entity: ItemDisplayEntity? = null

  fun spawn(world: ServerWorld) {
    if (hasSpawned || properties == null) { return }

    entity = ItemDisplayEntity(EntityType.ITEM_DISPLAY, world)
    updateProperties(properties!!)

    world.spawnEntity(entity)

    this.hasSpawned = true
  }


  fun updateProperties(properties: DisplayEntityProperties) {
    val customModelRegex = "^([^/]+)(?:/([0-9]+))?$".toRegex()
    val matchResult = customModelRegex.find(properties.model)

    val modelName: String
    val customModelNum: Int

    if (matchResult != null) {
      modelName = matchResult.groupValues[1]
      customModelNum = matchResult.groupValues[2].toIntOrNull() ?: 0
    }
    else {
      modelName = properties.model
      customModelNum = 0
    }

    val nbt = NbtCompound().apply {
      val tagList = NbtList()

      properties.tags.forEach {
        tagList.add(NbtString.of(it))
      }

      put("Tags", tagList)

      put("item", NbtCompound().apply {
        putString("id", modelName)
        putInt("count", 1)
        put("components", NbtCompound().apply { putInt("minecraft:custom_model_data", customModelNum) })
      })

      put("transformation", NbtCompound().apply {
        val offsetList = NbtList().apply {
          add(NbtFloat.of(properties.translation.x))
          add(NbtFloat.of(properties.translation.y))
          add(NbtFloat.of(properties.translation.z))
        }
        val rotList = NbtList().apply {
          add(NbtFloat.of(properties.leftRotation.x))
          add(NbtFloat.of(properties.leftRotation.y))
          add(NbtFloat.of(properties.leftRotation.z))
          add(NbtFloat.of(properties.leftRotation.w))
        }
        val scaleList = NbtList().apply {
          add(NbtFloat.of(properties.scale.x))
          add(NbtFloat.of(properties.scale.y))
          add(NbtFloat.of(properties.scale.z))
        }
        val rightRotList = NbtList().apply {
          add(NbtFloat.of(properties.rightRotation.x))
          add(NbtFloat.of(properties.rightRotation.y))
          add(NbtFloat.of(properties.rightRotation.z))
          add(NbtFloat.of(properties.rightRotation.w))
        }

        put("translation", offsetList)
        put("left_rotation", rotList)
        put("scale", scaleList)
        put("right_rotation", rightRotList)
      })

      put("view_range", NbtCompound().apply { properties.viewRange })

      if (properties.name != null) {
        putString("CustomName", properties.name)
        putByte("CustomNameVisible", 1)
      }
    }

    entity?.readNbt(nbt)
    entity?.refreshPositionAndAngles(properties.pos.x, properties.pos.y, properties.pos.z, properties.rot.x, properties.rot.y)

    this.properties = properties
  }


  fun kill() {
    if (entity == null) { println("BPS - Failed to kill BDE. Entity is null!"); return }
    entity?.kill()
  }
}