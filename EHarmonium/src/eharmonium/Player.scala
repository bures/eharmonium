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

	
	private val players = Array(new BellowsPlayer(PlayerMixin.this), new ArpeggioPlayer(PlayerMixin.this))
	private var currentPlayerIdx = 0
	
	def player = players(currentPlayerIdx)
	
	def switchToNextPlayer() {
		player.reset()
		currentPlayerIdx = (currentPlayerIdx + 1) % players.length
	}

	def switchToPrevPlayer() {
		player.reset()
		currentPlayerIdx = (currentPlayerIdx - 1 + players.length) % players.length
	}
}

abstract class Player(val bellowsLevelObserver: BellowsLevelObserver) {
	protected var currentChord: Chord = null 
	
	protected def handlePlay()
	protected def handleStop()
	protected def handleChordPressed()
	protected def handleSpacePressed()
	protected def handleChordReleased()
	protected def handleSpaceReleased()
	protected def handleReset()
	
	/**
	 * Starts playing a given chord. If another chord has been already playing, automatically invokes stop() first.
	 */
	def playChord(chord: Chord) {
		if (currentChord != null && currentChord != chord) {
			stopChord()
		}
		currentChord = chord
		
		handlePlay()
	}
	
	/**
	 * Stops playing the chord. 
	 */
	def stopChord() {
		handleStop()
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
	
	/**
	 * Resets the player (e.g. as a response to pressing Backspace)
	 */
	def reset() {
		handleReset()
		currentChord = null
	}
} 



