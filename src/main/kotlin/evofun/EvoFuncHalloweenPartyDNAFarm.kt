package evofun

import evofun.bio.DNA
import evofun.bio.DNAFarm
import evofun.bio.Genetic
import evofun.bio.Organism
import evofun.image.Entropy
import evofun.image.ImageToGrayScaleBuffer
import evofun.random.Dice
import evofun.sound.BeatDetector
import evofun.sound.LiveBeatDetector
import java.awt.Color
import java.awt.Graphics
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Thread.sleep
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants
import kotlin.random.Random

fun main(args: Array<String>) {
    EvoFuncHalloweenPartyDNAFarm().run()
}

class EvoFuncHalloweenPartyDNAFarm {
    // 16:9 512x288
    private val worldWidth = 640
    private val worldHeight = 360
    private val canvas = BufferedImage(worldWidth, worldHeight, BufferedImage.TYPE_INT_ARGB)
    private val canvasGraphics = canvas.graphics
    private var turnsEntropyIsBelowThreshold = 0
    private var turnsSinceEntropyAboveThreshold = 0
    private var mutationRate = 0.05
    private val saveImage = false
    private val imageFolderBase = "/tmp/evofunc_${System.currentTimeMillis() / 1000}/"
    private val dnaFarm = DNAFarm()
    private val genesCount = 50
    private var organism = buildOrganism()

    fun run() {
        var i = 0
        val panel = object : JPanel() {
            init {
                isFocusable = true
                requestFocusInWindow()

                addKeyListener(object : KeyAdapter() {
                    override fun keyPressed(e: KeyEvent) {
                        println("key pressed")
                        if (e.keyCode == KeyEvent.VK_ENTER) {
                            saveCanvasAsImage()
                        }
                        if (e.keyCode == KeyEvent.VK_N) {
                            println("new dna")
                            organism = buildOrganism()
                        }
                        if (e.keyCode == KeyEvent.VK_S) {
                            println("save dna")
                            dnaFarm.writeDNA(organism.dna)
                        }
                    }
                })
            }

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)

                canvasGraphics.color = Color.BLACK
                canvasGraphics.clearRect(0, 0, width, height)

                //println(organism.dna)
                organism.step(1000000)
//                ImageToGrayScaleBuffer.addToBuffer(targetImage, 0.4, organism.buffer)

                organism.express(canvasGraphics)
                organism.entropy = Entropy.calculateNormalizedEntropy(canvas)

                if (organism.entropy <= 0.015) {
                    turnsEntropyIsBelowThreshold++
                    turnsSinceEntropyAboveThreshold = 0
                } else {
                    turnsEntropyIsBelowThreshold = 0
                    turnsSinceEntropyAboveThreshold++
                }

                if (turnsEntropyIsBelowThreshold > 5) {
                    println("reset because boring")
                    organism = buildOrganism()
                } else if (turnsSinceEntropyAboveThreshold >= 100) {
                    println("reset because pattern hit 100 iterations")
                    organism = buildOrganism()
                } else {
                    mutationRate = getMutationRate(organism.entropy, minRate = 0.01, maxRate = 0.05)
                    Genetic.mutateDna(organism.dna, probability = mutationRate)
                }

                println("$i, ${organism.entropy}, $mutationRate")
                g.drawImage(canvas, 0, 0, width, height, this)
                if (saveImage) saveCanvasAsImage()
                i++
                // println(organism.dna)
            }

            /*
             * normalized entropy between 0.0 and 1.0
             * Scale mutation rate based on normalized entropy
             * Higher entropy means lower mutation rate (more interesting images need less mutation)
             *
             */
            private fun getMutationRate(normalizedEntropy: Double, minRate: Double = 0.01, maxRate: Double = 1.0): Double {
                return minRate + ((1.0 - normalizedEntropy) * (maxRate - minRate) / 10.0)
            }

            private fun saveCanvasAsImage() {
                val file = File(imageFolderBase)
                if (!file.exists()) {
                    file.mkdirs()  // Create the directory if it does not exist
                }
                val fileName = "$imageFolderBase$i.png"
                ImageIO.write(canvas, "png", File(fileName))
                // println("saved image to $fileName")
            }
        }

        val frame = JFrame()
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.setSize(worldWidth, worldHeight + 18)
        frame.isVisible = true
        frame.add(panel)
        panel.revalidate()

        while (true) {
            panel.repaint()
            sleep(20)
        }
    }

    private fun buildOrganism(): Organism {
        val dnaFormFarm = dnaFarm.readRandomDNA()
        dnaFormFarm ?: return Organism(dna = Genetic.buildDNA(genesCount), worldWidth, worldHeight)
        println("read dna from farm")
        return Organism(dna = dnaFormFarm, worldWidth, worldHeight)
    }

}