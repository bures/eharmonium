package eharmonium

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.shape.Rectangle
import javafx.scene.input.KeyCode

object KeyCodeModifier {
	val SHIFT = 1
	val CTRL = 2
}

class ChordKeyController(val mCtrl: MainController, val index: Int, val keyCode: KeyCode, val keyCodeModifier: Int, val label: String, val chordKind: ChordKind) {
	@FXML val abstractChord: Label = null;
	@FXML val actualChord: Label = null;
	@FXML val keyboardKey: Label = null;
	@FXML val rectangle: Rectangle = null;

	var isPressed: Boolean = false;
	var nowPlaying: Chord = null;
	
	def getChord = Chord.nthFifthFrom(mCtrl.key, chordKind, index - mCtrl.chordRootPos + chordKind.relPositionToMajor, mCtrl.chordRelativeHighestNote)
	
	def updateLabels() {
		val chord = getChord
		actualChord.setText(chord.name)
		abstractChord.setText(chord.abstractName(mCtrl.key))
		keyboardKey.setText(label)
	}
	
	def keyPressed() {
		val chord = getChord
		
		if (isPressed) return
		isPressed = true
		
		if (nowPlaying == null || nowPlaying != chord) {
			if (mCtrl.chordNowPlaying != null) {
				mCtrl.chordNowPlaying.stop()
			}
		
			mCtrl.chordNowPlaying = this
			nowPlaying = chord
			chord.play()
			
			rectangle.getStyleClass.add("chordKeyRectanglePlaying");
		} else {
			chord.pressed()
		}

		rectangle.getStyleClass.add("chordKeyRectanglePressed");
	}
	
	def keyReleased() {
		isPressed = false
		
		if (nowPlaying != null) {
			rectangle.getStyleClass.remove("chordKeyRectanglePressed");
			
			nowPlaying.released()			
		}
	}
	
	def stopOrReset(isReset: Boolean) {
		assert(mCtrl.chordNowPlaying == this)
		assert(nowPlaying != null)
		mCtrl.chordNowPlaying = null

		rectangle.getStyleClass.remove("chordKeyRectanglePlaying");
		
		if (isPressed) {
			rectangle.getStyleClass.remove("chordKeyRectanglePressed");
		}

		if (isReset) 
			nowPlaying.reset()
		else 
			nowPlaying.stop()
			
		nowPlaying = null		
	}
	
	def stop() {
		stopOrReset(false)
	}
	
	def reset() {
		stopOrReset(true)
	}
	
	def initialize() {
		updateLabels()
	}
}

