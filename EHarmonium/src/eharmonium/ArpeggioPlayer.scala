package eharmonium

class ArpeggioPlayer(bellowsLevelObserver: BellowsLevelObserver) extends Player(bellowsLevelObserver) {
	
	private val sequence = Array(
			Array(0, 2),
			Array(0, 1, 2),
			Array(0, 1),
			Array(0, 1, 2)
		)
		
	private var playingState = 0
	
	private var isPlaying = false
	private var isPressed = false

	private var base: Int = 0
	private var tones: List[Int] = null
	

	override protected def handleInit() {
		Sampler.setVolume(chordsChannel, 100)
		Sampler.setVolume(tonesChannel, 100)
		bellowsLevelObserver.setBellowsLevel(1)		
	}
	
	protected def handleShutdown() {
		Sampler.setVolume(chordsChannel, 0)
		Sampler.setVolume(tonesChannel, 0)
		bellowsLevelObserver.setBellowsLevel(0)
	}
	
	override protected def handlePlayChord() {
		tones = currentChord.tones
		base = tones(0)
		
		assert(isPlaying == false)
		isPlaying = true
		isPressed = true

		playingState = 0
		for (note <- sequence(playingState))
			Sampler.noteOn(chordsChannel, tones(note))
	}
	
	protected def handleStopChord() {
		assert(isPlaying) 

		for (note <- sequence(playingState))
			Sampler.noteOff(chordsChannel, tones(note))
		
		isPressed = false
		isPlaying = false				
	}
	
	protected def handleChordPressed() {
		if (!isPressed) {
			playNext()
			isPressed = true
		}
	}
	
	protected def handleChordReleased() {
		if (isPressed) {
			playNext()
			isPressed = false
		}
	}

	protected def handleSpacePressed() {}
	protected def handleSpaceReleased() {}

	private def playNext() {
		for (note <- sequence(playingState) if (note != 0))
			Sampler.noteOff(chordsChannel, tones(note))
		
		playingState = (playingState + 1) % sequence.length
		
		for (note <- sequence(playingState) if (note != 0))
			Sampler.noteOn(chordsChannel, tones(note))
	}
	
}