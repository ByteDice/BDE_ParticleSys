package com.bytedice.bde_particles.particle

import org.joml.Vector3f

data class ParticleParams (
  //val shape: Shape, // add later, start with single point
  val blockCurve:   Array<String>            = arrayOf("minecraft:purple_concrete", "minecraft:purple_stained_glass"),
  val rotRandom:    Pair<Vector3f, Vector3f> = Pair(Vector3f(0.0f, 0.0f, 0.0f), Vector3f(360.0f, 360.0f, 360.0f)),
  val rotVelRandom: Pair<Vector3f, Vector3f> = Pair(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(5.0f, 5.0f, 5.0f)),
  val rotVelCurve:  Array<Vector3f>          = arrayOf(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, 0.0f, 0.0f)),
  val sizeRandom:   Pair<Vector3f, Vector3f> = Pair(Vector3f(0.5f, 0.5f, 0.5f), Vector3f(1.0f, 1.0f, 1.0f)),
  val sizeCurve:    Array<Vector3f>          = arrayOf(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, 0.0f, 0.0f)),
  val velRandom:    Pair<Vector3f, Vector3f> = Pair(Vector3f(-0.1f, 0.1f, -0.1f), Vector3f(0.1f, 0.2f, 0.1f)),
  val forceFields:  ForceField               = ForceField(),
  val gravity:      Vector3f                 = Vector3f(0.0f, -0.025f, 0.0f),
  val drag:         Float                    = 0.2f,
  val minVel:       Float                    = 0.003f,
  val lifeTime:     Int                      = 20
)