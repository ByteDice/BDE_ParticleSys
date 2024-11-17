package com.bytedice.bde_particles.particle

import org.joml.Vector3f

// TODO: order these correctly, explain all
// sizeRandom: if uniformSize is true then sizeRandom will be uniform (cubic) and the values are taken from X
// lifeTime:   any value below 1 will result in a particle not spawning
// minVel:     values 0 or less mean the particle can travel however slow it wants
// gravity:    Y being -0.025 is close to regular gravity
data class ParticleParams (
  //val shape: Shape, // add later, start with single point
  val blockCurve:   Array<String>            = arrayOf("minecraft:shroomlight", "minecraft:orange_concrete", "minecraft:orange_stained_glass", "minecraft:gray_stained_glass", "minecraft:light_gray_stained_glass"),
  val rotRandom:    Pair<Vector3f, Vector3f> = Pair(Vector3f(0.0f, 0.0f, 0.0f), Vector3f(360.0f, 360.0f, 360.0f)),
  val rotVelRandom: Pair<Vector3f, Vector3f> = Pair(Vector3f(-0.2f, -0.2f, -0.2f), Vector3f(0.2f, 0.2f, 0.2f)),
  val rotVelCurve:  Array<Vector3f>          = arrayOf(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, 0.0f, 0.0f)),
  val sizeRandom:   Pair<Vector3f, Vector3f> = Pair(Vector3f(0.5f, 0.5f, 0.5f), Vector3f(1.0f, 1.0f, 1.0f)),
  val uniformSize:  Boolean                  = true,
  val sizeCurve:    Array<Vector3f>          = arrayOf(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.5f, 0.5f, 0.5f)),
  val velRandom:    Pair<Vector3f, Vector3f> = Pair(Vector3f(-0.1f, 0.4f, -0.1f), Vector3f(0.1f, 0.8f, 0.1f)),
  val forceFields:  ForceField               = ForceField(),
  val gravity:      Vector3f                 = Vector3f(0.0f, -0.01f, 0.0f),
  val drag:         Float                    = 0.075f,
  val minVel:       Float                    = 0.0f,
  val lifeTime:     Int                      = 30
)