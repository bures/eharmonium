package eharmonium

trait BellowsLevelObserver {
	def setBellowsLevel(level: Double)
}

trait ChordPlayerMixin extends BellowsLevelObserver {
	private val chordPlayers = Array(new BellowsChordPlayer(this), new ArpeggioChordPlayer(this))
	private var currentChordPlayerIdx = 0
	
	def chordPlayer = chordPlayers(currentChordPlayerIdx)
	
	def switchToNextChordPlayer() {
		chordPlayer.reset()
		currentChordPlayerIdx = (currentChordPlayerIdx + 1) % chordPlayers.length
	}

	def switchToPrevChordPlayer() {
		chordPlayer.reset()
		currentChordPlayerIdx = (currentChordPlayerIdx - 1 + chordPlayers.length) % chordPlayers.length
	}
}

abstract class ChordPlayer(val bellowsLevelObserver: BellowsLevelObserver) {
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
	def play(chord: Chord) {
		if (currentChord != null && currentChord != chord) {
			stop()
		}
		currentChord = chord
		
		handlePlay()
	}
	
	/**
	 * Stops playing the chord. 
	 */
	def stop() {
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



