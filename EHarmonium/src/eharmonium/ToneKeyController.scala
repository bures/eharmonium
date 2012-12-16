package eharmonium

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.shape.Rectangle
import javafx.scene.input.KeyCode

class ToneKeyController(val mCtrl: MainController, val index: Int, val keyCode: KeyCode, val label: String) {
	@FXML private val actualTone: Label = null;
	@FXML private val keyboardKey: Label = null;
	@FXML private val rectangle: Rectangle = null;

	var isPlaying: Boolean = false;
	
	private def getToneNo = mCtrl.key + index - mCtrl.toneRootPos
	private def getTone = Tone(getToneNo)
	
	def updateLabels() {
		assert(!isPlaying)
		
		val tone = getTone
		actualTone.setText(tone.name)
		// abstractTone.setText(tone.abstractName(mCtrl.key))
		keyboardKey.setText(label)
	}
	
	def play() {
		if (isPlaying) return		

		isPlaying = true
		
		Sampler.noteOn(1, getToneNo)
			
		rectangle.getStyleClass.add("toneKeyRectanglePressed");
	}
	
	def stop() {
		if (!isPlaying) return
		
		isPlaying = false

		Sampler.noteOff(1, getToneNo)

		rectangle.getStyleClass.remove("toneKeyRectanglePressed");
	}
		
	private def initialize() {
		updateLabels()
	}
}

