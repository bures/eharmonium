package eharmonium

import javafx.fxml.FXML
import javafx.application.Platform
import javafx.scene.shape.Rectangle
import javafx.scene.paint.Color

trait BellowsLevelObserver {
	def setBellowsLevel(level: Double)
}

trait PlayerMixin extends BellowsLevelObserver {
	@FXML val bellowsLevel: Rectangle = null
	
	private object BellowsLevelUpdater extends Runnable {
		private val bellowsLevelColorBottom = Color.web("#2060ff")
		private val bellowsLevelColorTop = Color.web("#e02080")
	
		var level: Int = 0
		
		override def run() {
			val height = level * 105 / 1000
			
			bellowsLevel.setHeight(height)
			bellowsLevel.setY(105 - height)
			bellowsLevel.setFill(bellowsLevelColorBottom.interpolate(bellowsLevelColorTop, level / 1000.0))			
		}
	}
	
	override def setBellowsLevel(level: Double) {
		BellowsLevelUpdater.level = (level * 1000).floor.toInt
		Platform.runLater(BellowsLevelUpdater)
	}
	
	private val players = Array(new BellowsPlayer(this), new ConstantVolumePlayer(this), new ArpeggioPlayer(this))
	private var currentPlayerIdx = 0
	
	def player = players(currentPlayerIdx)
	player.init()
	
	def switchToNextPlayer() {
		player.shutdown()
		currentPlayerIdx = (currentPlayerIdx + 1) % players.length
		player.init()
	}

	def switchToPrevPlayer() {
		player.shutdown()
		currentPlayerIdx = (currentPlayerIdx - 1 + players.length) % players.length
		player.init()
	}
}

abstract class Player(val bellowsLevelObserver: BellowsLevelObserver) {
	protected val chordsChannel = 0
	protected val tonesChannel = 1
	
	protected var currentChord: Chord = null
	protected var currentChordKeyController: ChordKeyController = null
	
	protected def handlePlayChord()
	protected def handleStopChord()
	protected def handleChordPressed()
	protected def handleSpacePressed()
	protected def handleChordReleased()
	protected def handleSpaceReleased()
	protected def handleInit()
	protected def handleShutdown()
	
	def playTone(tone: Tone) {
		if (!tone.isEmpty)
			Sampler.noteOn(tonesChannel, tone.toneNo)
	}
	
	def stopTone(tone: Tone) {
		if (!tone.isEmpty)
			Sampler.noteOff(tonesChannel, tone.toneNo)
	}
	
	def stopAllTones() {
		Sampler.allNotesOff(tonesChannel)
	}
	
	/**
	 * Starts playing a given chord. If another chord has been already playing, automatically invokes stop() first.
	 */
	def playChord(chord: Chord, chordKeyController: ChordKeyController) {
		assert(currentChord == null)

		currentChord = chord
		currentChordKeyController = chordKeyController
		
		handlePlayChord()
	}
	
	/**
	 * Stops playing the chord. 
	 */
	def stopChord() {
		assert(currentChord != null)
		handleStopChord()
		
		currentChord = null
		currentChordKeyController = null
	}
	
	/**
	 * Chord key has been pressed (after being in release state).
	 */
	def chordPressed() {
		handleChordPressed()
	}
	
	/**
	 * Chord key has been release (after being in start or pressed state).
	 */
	def chordReleased() {
		handleChordReleased()
	}
	
	/**
	 * Space has been pressed
	 */
	def spacePressed() {
		handleSpacePressed()
	}
	
	/**
	 * Space has been released
	 */
	def spaceReleased() {
		handleSpaceReleased()
	}
	
	def init() {
		handleInit()
		currentChord = null
	}
	
	/**
	 * Resets the player (e.g. as a response to pressing Backspace)
	 */
	def shutdown() {
		if (currentChord != null) 
			stopChord()
			
		stopAllTones()
		
		handleShutdown()
	}
} 
