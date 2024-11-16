package com.bytedice.bde_particles

import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtFloat
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.world.ServerWorld

class DisplayEntity(private var properties: DisplayEntityProperties?) {
  private var hasSpawned = false
  private var entity: BlockDisplayEntity? = null

  fun spawn(world: ServerWorld) {
    if (hasSpawned || properties == null) { return }

    entity = BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world)
    updateProperties(properties!!)

    world.spawnEntity(entity)

    this.hasSpawned = true
  }

  fun updateProperties(properties: DisplayEntityProperties) {
    val nbt = NbtCompound().apply {
      val tagList = NbtList()

      properties.tags.forEach { tag ->
        tagList.add(NbtString.of(tag))
      }

      put("Tags", tagList)

      put("block_state", NbtCompound().apply { putString("Name", properties.blockType) })

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
    }

    entity?.readNbt(nbt)
    entity?.refreshPositionAndAngles(properties.pos.x, properties.pos.y, properties.pos.z, properties.rot.x, properties.rot.y)

    this.properties = properties
  }

  fun kill() {
    if (entity == null) { println("BPS - Particle entity is null!"); return }
    entity?.kill()
  }
}