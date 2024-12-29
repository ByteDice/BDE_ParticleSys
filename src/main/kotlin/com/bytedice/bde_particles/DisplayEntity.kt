package com.bytedice.bde_particles

import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtFloat
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.world.ServerWorld

class DisplayEntity(var properties: DisplayEntityProperties?) {
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
    val nbt = NbtCompound().apply {
      val tagList = NbtList()

      properties.tags.forEach {
        tagList.add(NbtString.of(it))
      }

      put("Tags", tagList)

      if (properties.model != "none") {
        put("item", NbtCompound().apply {
          putString("id", properties.model)
          putInt("count", 1)
          put("components", NbtCompound().apply { putInt("minecraft:custom_model_data", properties.customModel) })
        })
      }

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

      putFloat("view_range", properties.viewRange)

      putString("billboard", properties.billboard)

      if (properties.customName != null) {
        putString("CustomName", "{\"text\":\"${properties.customName}\"}")
        putByte("CustomNameVisible", 1)
      }

      if (properties.brightnessOverride != null) {
        put("brightness", NbtCompound().apply {
          putInt("block", properties.brightnessOverride!!)
          putInt("sky", properties.brightnessOverride!!)
        })
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