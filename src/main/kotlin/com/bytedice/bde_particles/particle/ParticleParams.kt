package com.bytedice.bde_particles.particle

import org.joml.Vector3f

// sizeRandom: set y and z on both vectors to -1 for uniform scale // not implemented
// lifeTime:   any value below 1 will result in a particle not spawning
// minVel      values 0 or less mean the particle can travel however slow it wants
data class ParticleParams (
  //val shape: Shape, // add later, start with single point
  val blockCurve:   Array<String>            = arrayOf("minecraft:purple_concrete", "minecraft:purple_stained_glass"),
  val rotRandom:    Pair<Vector3f, Vector3f> = Pair(Vector3f(0.0f, 0.0f, 0.0f), Vector3f(360.0f, 360.0f, 360.0f)),
  val rotVelRandom: Pair<Vector3f, Vector3f> = Pair(Vector3f(-0.2f, -0.2f, -0.2f), Vector3f(0.2f, 0.2f, 0.2f)),
  val rotVelCurve:  Array<Vector3f>          = arrayOf(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, 0.0f, 0.0f)),
  val sizeRandom:   Pair<Vector3f, Vector3f> = Pair(Vector3f(0.5f, 0.5f, 0.5f), Vector3f(1.0f, 1.0f, 1.0f)),
  val sizeCurve:    Array<Vector3f>          = arrayOf(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, 0.0f, 0.0f)),
  val velRandom:    Pair<Vector3f, Vector3f> = Pair(Vector3f(-0.3f, 0.3f, -0.3f), Vector3f(0.3f, 0.6f, 0.3f)),
  val forceFields:  ForceField               = ForceField(),
  val gravity:      Vector3f                 = Vector3f(0.0f, -0.025f, 0.0f),
  val drag:         Float                    = 0.075f,
  val minVel:       Float                    = 0.0f,
  val lifeTime:     Int                      = 20
)